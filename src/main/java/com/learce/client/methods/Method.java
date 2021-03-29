package com.learce.client.methods;

import java.io.DataInputStream;

public abstract class Method {

    public abstract int getClassId();

    public abstract Integer getSyncMethodId();

    public abstract boolean hasContextHeader();

    public abstract void readArguments(DataInputStream inputStream);

    public abstract byte[] writeArguments();
}
