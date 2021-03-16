package com.learce.client.main;

import com.learce.client.classes.AmqpClass;
import com.learce.client.util.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class Channel {

    private final Connection connection;
    private final int channelNumber;
    private Command command;
    private Integer syncMethodId;
    private ChannelStatus channelStatus;

    public Channel(Connection connection, int channelNumber) {
        this.connection = connection;
        this.channelNumber = channelNumber;
        this.command = new Command();
        this.syncMethodId = null;
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
                if (methodId == syncMethodId) {
                    syncMethodId = null;
                } else {
                    //throw exception
                }
            }
            Method method = getMethod(classId, methodId);
            method.readArguments(payload);
            command.setMethod(method);

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
        }
    }

    protected void handleBodyFrame(DataInputStream payload) throws IOException {
        synchronized (command) {
            ContentHeader contentHeader = command.getContentHeader();
            if (contentHeader != null) {
                contentHeader.readContentBody(payload);
            } else {
                //throw exception
            }
        }
    }

    protected void handleHeartbeatFrame(DataInputStream payload) throws IOException {
        int classId = payload.readUnsignedShort();
        if (channelNumber != 0 && classId == AMQP.CONNECTION_CLASS) {
            //throw exception
        }
    }

    private Method getMethod(int classId, int methodId) {
        return AmqpClass.getMethod(classId, methodId);
    }

    private void handleCompleteCommand(Command command) {
        sendCommand(command);
    }

    private Frame commandToFrame(Command command) {
        return  null;
    }

    private void sendCommand(Command command) {
        try {
            Frame frame = commandToFrame(command);
            connection.addFrameToSend(frame);
        } catch (InterruptedException e) {
            //handle exception
            //this point could be potential extention for recovery channel
        }
    }

    public void sendMessage(String queueName, String message) {

    }

    public void publishMessageReceiver(String queueName, Consumer<String> messageHandler) {

    }
}
