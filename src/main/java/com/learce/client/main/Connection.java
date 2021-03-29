package com.learce.client.main;

import com.learce.client.util.ConnectionChannel;
import com.learce.client.util.ConnectionStatus;
import com.learce.client.util.Frame;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Connection {

    /* The channel number is 0 for all frames which are global to the connection and 1-65535 for frames that refer to specific channels. */
    private AtomicInteger channelNumberSequence = new AtomicInteger(1);

    private final Socket socket;
    private volatile ConnectionStatus connectionStatus;
    private final ConnectionChannel connectionChannel;
    private final Map<Integer, Channel> channels = new HashMap<>();

    private final ExecutorService mainLoopsExecutor = Executors.newFixedThreadPool(3);
    private final ExecutorService channelExecutor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<Frame> inputFrames = new LinkedBlockingQueue<>();
    private final BlockingQueue<Frame> outputFrames = new LinkedBlockingQueue<>();

    public Connection(Socket socket) {
        this.socket = socket;
        this.connectionChannel = new ConnectionChannel(this);
        connectionStatus = ConnectionStatus.NEW;
    }

    public synchronized Channel createChannel() {
        int channelNumber = getNextChannelNumber();
        Channel channel = new Channel(this, channelNumber);
        channels.put(channelNumber,channel);
        return channel;
    }

    private synchronized int getNextChannelNumber() {
        return channelNumberSequence.getAndIncrement();
    }

    protected void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public void start() {
        startMainLoops();
        connectionChannel.startConnection();
    }

    private void startMainLoops() {
        Runnable inputFrameHandler = () -> {
            try {
                while (this.connectionStatus != ConnectionStatus.CLOSE_OK) {
                    Frame inputFrame = readFrame(new DataInputStream(socket.getInputStream()));
                    inputFrames.put(inputFrame);
                }
            } catch (IOException | InterruptedException exception) {
                //todo: handle exception
            } finally {
                close();
            }
        };
        Runnable outputFrameHandler = () -> {
            try {
                while (this.connectionStatus != ConnectionStatus.CLOSE_OK) {
                    Frame outputFrame = outputFrames.take();
                    writeFrame(outputFrame);
                }
            } catch (IOException | InterruptedException exception) {
                //todo: handle exception
            } finally {
                close();
            }
        };
        Runnable frameHandler = () -> {
            //todo: ПЕРЕДЕЛАТЬ ЛОГИКУ РАСКИДЫВАНИЯ ФРЕЙМОВ ПО КАНАЛАМ
            //В КАНАЛЫ НУЖНО ДОБАВИТЬ ОЧЕРЕДЬ, ЧТОБЫ ФРЕЙМЫ ОБРАБАТЫВАЛИСЬ ПОСЛЕДОВАТЕЛЬНО
            try {
                while (this.connectionStatus != ConnectionStatus.CLOSE_OK) {
                    Frame inputFrame = inputFrames.take();
                    int channelNumber = inputFrame.getChannel();
                    Channel channel = channels.get(channelNumber);
                    channelExecutor.execute(() -> channel.handleFrame(inputFrame));
                }
            } catch (InterruptedException exception) {
                //todo: handle exception
            } finally {
                close();
            }
        };
        //todo: добавь защелку CountDownLatch
        mainLoopsExecutor.execute(inputFrameHandler);
        mainLoopsExecutor.execute(outputFrameHandler);
        mainLoopsExecutor.execute(frameHandler);
    }

    public void close() {
        connectionChannel.closeConnection();
        connectionStatus = ConnectionStatus.CLOSE_OK;
        try {
            socket.close();
        } catch (IOException exception) {
            //todo: handle exception
        }
    }

    protected void addFrameToSend(Frame frame) {
        try {
            outputFrames.put(frame);
        } catch (InterruptedException exception) {
            //handle exception
        }
    }

    private synchronized Frame readFrame(DataInputStream inputStream) throws IOException {
        //todo: check frame size
        int type = inputStream.readUnsignedByte();
        int channel = inputStream.readUnsignedShort();
        int payloadSize = inputStream.readInt();
        byte[] payload = new byte[payloadSize];
        inputStream.readFully(payload);

        int frameEndMarker = inputStream.readUnsignedByte();
        if (frameEndMarker != AMQP.FRAME_END) {
            //todo: If the frame-end is not valid it MUST treat this as a fatal protocol error and close the
            //connection without sending any further data on it.
        }
        return new Frame(type, channel, payloadSize, payload);
    }

    private synchronized void writeFrame(Frame frame) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.writeByte(frame.getType());
        outputStream.writeShort(frame.getChannel());
        outputStream.writeInt(frame.getSize());
        outputStream.write(frame.getPayload());
        outputStream.writeByte(AMQP.FRAME_END);
    }
}
