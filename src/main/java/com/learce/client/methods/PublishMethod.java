package com.learce.client.methods;

public abstract class PublishMethod extends Method {

    public abstract void publish(String queueName, String message);
}
