package com.learce.client.util;

public class Frame {

    private final int type;
    private final int channel;
    private final int size;
    private final byte[] payload;

    public Frame(int type, int channel, int size, byte[] payload) {
        this.type = type;
        this.channel = channel;
        this.size = size;
        this.payload = payload;
    }

    //todo: add lombok
    public int getType() {
        return type;
    }

    public int getChannel() {
        return channel;
    }

    public int getSize() {
        return size;
    }

    public byte[] getPayload() {
        return payload;
    }
}
