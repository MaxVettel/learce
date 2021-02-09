package com.learce.client.main;

import java.lang.reflect.Executable;
import java.util.function.Consumer;

public class Channel {

    private Connection connection;

    public Channel(Connection connection) {
        this.connection = connection;
    }

    public void sendMessage(String queueName, String message) {

    }

    public void publishMessageReceiver(String queueName, Consumer<String> messageHandler) {

    }
}
