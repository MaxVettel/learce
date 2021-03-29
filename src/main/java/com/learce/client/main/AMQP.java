package com.learce.client.main;

public interface AMQP {

    int CONNECTION_CLASS = 10;

    int METHOD_FRAME = 1;
    int HEADER_FRAME = 2;
    int BODY_FRAME = 3;
    int HEARTBEAT_FRAME = 4;

    int FRAME_END = 206;
    int FRAME_MIN_SIZE = 4096;
    int FRAME_PAYLOAD_MIN_SIZE = 4032;
}
