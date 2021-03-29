package com.learce.client.util;

import lombok.Getter;

@Getter
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
}
