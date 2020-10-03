package com.learce.test_server.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
public class SendService {

    private static final String QUEUE_NAME = "new_queue";

    Logger logger = LoggerFactory.getLogger(SendService.class);

    @Autowired
    Connection connection;

    public void send(String message) throws IOException {
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        logger.info(" [x] Sent '" + message + "'");
    }
}
