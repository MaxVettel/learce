package com.learce.client.util;

public enum  CommandStatus {

    WAITING,
    EXPECTING_METHOD,
    EXPECTING_CONTENT_HEADER,
    EXPECTING_CONTENT_BODY,
    COMPLETE;
}
