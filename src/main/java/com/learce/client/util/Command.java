package com.learce.client.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Command {

    private CommandStatus commandStatus;

    private Method method;
    private ContentHeader contentHeader;


    public Command() {
        this.commandStatus = CommandStatus.WAITING;
    }
}
