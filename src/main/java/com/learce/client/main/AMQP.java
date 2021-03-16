package com.learce.client.main;

public interface AMQP {

    int CONNECTION_CLASS = 10;

    int METHOD_FRAME = 1;
    int HEADER_FRAME = 2;
    int BODY_FRAME = 3;
    int HEARTBEAT_FRAME = 4;

    int FRAME_END = 206;
}
