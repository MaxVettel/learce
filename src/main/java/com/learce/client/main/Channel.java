package com.learce.client.main;

import com.learce.client.classes.AmqpClass;
import com.learce.client.methods.ConsumeMethod;
import com.learce.client.methods.Method;
import com.learce.client.methods.PublishMethod;
import com.learce.client.util.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Channel {

    private final Connection connection;
    private final int channelNumber;
    private Command command;
    private Integer syncMethodId;
    private ChannelStatus channelStatus;
    private final ConcurrentHashMap<String, Consumer<String>> consumerTags;

    public Channel(Connection connection, int channelNumber) {
        this.connection = connection;
        this.channelNumber = channelNumber;
        this.command = new Command();
        this.syncMethodId = null;
        this.consumerTags = new ConcurrentHashMap<>();
    }

    /**
     * Central method to handle incoming frames.
     * @param frame
     */
    //todo: добавь equals и hashcode так как каналы используются в hashmap в классе Connection
    //The channel number MUST be zero for all heartbeat frames, and for method, header and body frames
    //that refer to the Connection class. A peer that receives a non-zero channel number for one of these
    //frames MUST signal a connection exception with reply code 503 (command invalid).
    protected void handleFrame(Frame frame) {
        synchronized (command) {
            try {
                CommandStatus commandStatus = command.getCommandStatus();
                if (commandStatus == CommandStatus.COMPLETE) {
                    handleCompleteCommand(command);
                    command = new Command(); //previous command could be saved in archive
                }

                DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(frame.getPayload()));
                int frameType = frame.getType();
                if (frameType == AMQP.METHOD_FRAME) {
                    if (commandStatus == CommandStatus.EXPECTING_METHOD || commandStatus == CommandStatus.WAITING) {
                        handleMethodFrame(inputStream);
                    }
                } else if (frameType == AMQP.HEADER_FRAME) {
                    if (commandStatus == CommandStatus.EXPECTING_CONTENT_HEADER) {
                        handleHeaderFrame(inputStream);
                    }
                } else if (frameType == AMQP.BODY_FRAME) {
                    if (commandStatus == CommandStatus.EXPECTING_CONTENT_BODY) {
                        handleBodyFrame(inputStream);
                    }
                } else if (frameType == AMQP.HEARTBEAT_FRAME) {
                    handleHeartbeatFrame(inputStream);
                } else {
                    //throw exception
                }
            } catch (IOException e) {
                //handle exception
            }
        }
    }

    protected void handleMethodFrame(DataInputStream payload) throws IOException {
        synchronized (command) {
            int classId = payload.readUnsignedShort();
            int methodId = payload.readUnsignedShort();
            //check that channel doesn't expect any answer from sync method
            if (syncMethodId != null) {
                if (syncMethodId != methodId) {
                    //throw exception
                }
            }
            Method method = AmqpClass.getMethod(classId, methodId);
            method.readArguments(payload);
            command.setMethod(method);
            syncMethodId = method.getSyncMethodId();

            if (method.hasContextHeader()) {
                command.setCommandStatus(CommandStatus.EXPECTING_CONTENT_HEADER);
            } else {
                command.setCommandStatus(CommandStatus.COMPLETE);
                handleCompleteCommand(command);
            }
        }
    }

//    A peer that receives an incomplete or badly-formatted content MUST raise a connection exception
//    with reply code 505 (unexpected frame). This includes missing content headers, wrong class IDs in
//    content headers, missing content body frames, etc.
    protected void handleHeaderFrame(DataInputStream payload) throws IOException {
        synchronized (command) {
            int classId = payload.readUnsignedShort();
            if (classId != command.getMethod().getClassId()) {
                //throw exception
            }
            int weight = payload.readUnsignedShort();
            if (weight != 0) {
                //throw exception
            }
            long bodySize = payload.readLong();
            ContentHeader contentHeader = new ContentHeader(bodySize);
            contentHeader.readProperties(payload);
            command.setContentHeader(contentHeader);
            if (bodySize != 0) {
                command.setCommandStatus(CommandStatus.EXPECTING_CONTENT_BODY);
            } else {
                command.setCommandStatus(CommandStatus.COMPLETE);
                handleCompleteCommand(command);
            }
        }
    }

    protected void handleBodyFrame(DataInputStream payload) throws IOException {
        synchronized (command) {
            ContentHeader contentHeader = command.getContentHeader();
            long bodySize = contentHeader.getBodySize();
            //todo: ДОБАВИТЬ ОБРАБОТКУ НЕСКОЛЬКИХ ПОСЛЕДОВАТЕЛЬНЫХ ФРЕЙМОВ
            if (contentHeader != null) {
                contentHeader.readContentBody(payload);
            } else {
                //throw exception
            }
            //todo: ДОБАВИТЬ ОБНОВЛЕНИЕ СТАТУСА КОМАНДЫ
        }
    }

    protected void handleHeartbeatFrame(DataInputStream payload) throws IOException {
        int classId = payload.readUnsignedShort();
        if (channelNumber != 0 && classId == AMQP.CONNECTION_CLASS) {
            //throw exception
        }
        //todo: обработка фрейма
    }

    private void handleCompleteCommand(Command command) {
        //no synchronization because not only channel command could be handled
        //but also client command (publish or consume)
        if (command.getCommandStatus() == CommandStatus.COMPLETE) {
            List<Frame> frames = commandToFrames(command);
            frames.forEach(frame -> connection.addFrameToSend(frame));
        } else {
            //throw exception
        }
    }

    private List<Frame> commandToFrames(Command command) {
        ArrayList<Frame> frames = new ArrayList<>();
        Method method = command.getMethod();
        byte[] methodPayload = method.writeArguments();
        Frame methodFrame = new Frame(AMQP.METHOD_FRAME, channelNumber, methodPayload.length, methodPayload);
        frames.add(methodFrame);
        if (method.hasContextHeader()) {
            ContentHeader contentHeader = command.getContentHeader();
            byte[] headerPayload = contentHeader.writeProperties();
            Frame headerFrame = new Frame(AMQP.HEADER_FRAME, channelNumber, headerPayload.length, headerPayload);
            frames.add(headerFrame);
            long bodySize = contentHeader.getBodySize();
            //Если в bodySize есть контент, то его нужно записать в ContentBody фреймы
            if (bodySize != 0) {
                byte[] contentPayload = contentHeader.getContentBody();
                //Сначала ищем количество полноразмерный фреймов, так как минимальный размер фрейма задан
                int contentFramesNumber = (int) bodySize/AMQP.FRAME_PAYLOAD_MIN_SIZE;
                for (int i = 0; i < contentFramesNumber; i++) {
                    int rangeFrom = i * AMQP.FRAME_PAYLOAD_MIN_SIZE;
                    int rangeTo = rangeFrom + AMQP.FRAME_PAYLOAD_MIN_SIZE;
                    byte[] contentPayloadPart = Arrays.copyOfRange(contentPayload, rangeFrom, rangeTo);
                    Frame contentFrame = new Frame(AMQP.BODY_FRAME, channelNumber, contentPayloadPart.length, contentPayloadPart);
                    frames.add(contentFrame);
                }
                //Если bodySize подразумевает в конце обрезанный фрейм, то обрабатываем его
                int contentPayloadRest = (int) bodySize - contentFramesNumber * AMQP.FRAME_PAYLOAD_MIN_SIZE;
                if (contentPayloadRest != 0) {
                    int rangeFrom = contentFramesNumber * AMQP.FRAME_PAYLOAD_MIN_SIZE;
                    int rangeTo = rangeFrom + contentPayloadRest;
                    byte[] contentPayloadPart = Arrays.copyOfRange(contentPayload, rangeFrom, rangeTo);
                    Frame contentFrame = new Frame(AMQP.BODY_FRAME, channelNumber, contentPayloadPart.length, contentPayloadPart);
                    frames.add(contentFrame);
                }
            }
        }
        return frames;
    }

    public void sendMessage(String queueName, String message) {
        PublishMethod publishMethod = AmqpClass.getPublishMethod();
        Command command = new Command();
        publishMethod.publish(queueName, message);
        handleCompleteCommand(command);
    }

    public void publishMessageReceiver(String queueName, Consumer<String> messageHandler) {
        publishMessageReceiver(queueName, messageHandler, "");
    }

    public void publishMessageReceiver(String queueName, Consumer<String> messageHandler, String consumerTag) {
        if (consumerTag == null || consumerTag.isEmpty()) {

        }
        ConsumeMethod consumeMethod = AmqpClass.getConsumeMethod();
        String message = consumeMethod.consume(queueName);
        messageHandler.accept(message);
    }
}
