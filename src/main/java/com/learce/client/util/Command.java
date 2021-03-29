package com.learce.client.util;

import com.learce.client.methods.Method;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Command {

    private volatile CommandStatus commandStatus;

    private Method method;
    private ContentHeader contentHeader;


    public Command() {
        this.commandStatus = CommandStatus.WAITING;
    }
}
