package com.learce.client.util;

import java.io.DataInputStream;

public class ContentHeader {

    private final long bodySize;
    private byte[] contentBody;

    public ContentHeader(long bodySize) {
        this.bodySize = bodySize;
    }

    public void readProperties(DataInputStream inputStream) {

    }

    public void writeProperties(DataInputStream inputStream) {

    }

    public void readContentBody(DataInputStream inputStream) {

    }
}
