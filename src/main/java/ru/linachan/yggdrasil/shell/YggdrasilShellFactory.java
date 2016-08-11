package ru.linachan.yggdrasil.shell;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;

public class YggdrasilShellFactory implements Factory<Command> {

    private final YggdrasilShellCommandManager commandManager;

    public YggdrasilShellFactory(YggdrasilShellCommandManager masterCommandManager) {
        commandManager = masterCommandManager;
    }

    @Override
    public Command create() {
        return new YggdrasilShell(commandManager);
    }
}
