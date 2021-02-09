package com.learce.client.util;

import com.learce.client.main.Channel;
import com.learce.client.main.Connection;

public class ConnectionChannel extends Channel {

    public static final String HEADER = "AMQP 0.0.9.1";

    public ConnectionChannel(Connection connection) {
        super(connection);
    }

    public void startConnection() {

    }

    public void closeConnection() {

    }
}
