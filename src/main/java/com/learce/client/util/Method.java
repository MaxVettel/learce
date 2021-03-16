package com.learce.client.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class Method {

    public abstract int getClassId();

    public abstract Integer getSyncMethodId();

    public abstract boolean hasContextHeader();

    public abstract void readArguments(DataInputStream inputStream);

    public abstract void writeArguments(DataOutputStream outputStream);
}
