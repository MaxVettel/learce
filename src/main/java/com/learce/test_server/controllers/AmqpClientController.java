package com.learce.test_server.controllers;

import com.learce.client.main.Channel;
import com.learce.client.main.Connection;
import com.learce.client.main.ConnectionFactory;
import com.learce.test_server.services.SendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@RestController
public class AmqpClientController {

    Logger logger = LoggerFactory.getLogger(AmqpClientController.class);

    @Autowired
    private SendService sendService;

    /**
     *
     * Endpoint to test with RabbitMQ client
     *
     */
    @GetMapping("/{message}")
    public void send(@PathVariable("message") String message) throws IOException, TimeoutException {
        logger.info("Starting send...");
        sendService.send(message);
    }

    /**
     *
     * Endpoint to test with Learce client
     *
     */
    @GetMapping("/")
    public void sendLearce() {
        logger.info("Start...");
        Connection connection = ConnectionFactory.createConnection("localhost", 5672);
        Channel channel = connection.createChannel();

        Consumer<String> messageHandler = (String message) -> {
            logger.info("Receive message: " + message);
        };
        channel.publishMessageReceiver("testQueue", messageHandler);

        channel.sendMessage("testQueue", "Little message to test sending");
    }
}
