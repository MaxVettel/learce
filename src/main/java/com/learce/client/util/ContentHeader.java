package com.learce.client.util;

import lombok.Getter;

import java.io.DataInputStream;

@Getter
public class ContentHeader {

    private final long bodySize;
    private byte[] contentBody; //The body size is a 64-bit value that defines the total size of the content body

    public ContentHeader(long bodySize) {
        this.bodySize = bodySize;
        this.contentBody = new byte[(int) bodySize];
    }

    public void readProperties(DataInputStream inputStream) {

    }

    public void readContentBody(DataInputStream inputStream) {

    }

    public byte[] writeProperties() {
        return null;
    }
}
