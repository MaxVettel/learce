package com.learce.client.main;

import java.util.function.Consumer;

public class Channel {

    private final Connection connection;
    private final int channelNumber;

    public Channel(Connection connection, int channelNumber) {
        this.connection = connection;
        this.channelNumber = channelNumber;
    }

    public void sendMessage(String queueName, String message) {

    }

    public void publishMessageReceiver(String queueName, Consumer<String> messageHandler) {

    }
}
