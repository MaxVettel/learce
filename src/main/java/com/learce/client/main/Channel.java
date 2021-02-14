package com.learce.client.main;

import com.learce.client.util.Frame;

import java.util.function.Consumer;

public class Channel {

    private final Connection connection;
    private final int channelNumber;

    public Channel(Connection connection, int channelNumber) {
        this.connection = connection;
        this.channelNumber = channelNumber;
    }

    //todo: добавь equals и hashcode так как каналы используются в hashmap в классе Connection
    protected void handleFrame(Frame frame) {

    }

    public void sendMessage(String queueName, String message) {

    }

    public void publishMessageReceiver(String queueName, Consumer<String> messageHandler) {

    }
}
