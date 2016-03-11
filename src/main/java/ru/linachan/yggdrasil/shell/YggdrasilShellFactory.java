package ru.linachan.yggdrasil.shell;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import ru.linachan.yggdrasil.YggdrasilCore;

public class YggdrasilShellFactory implements Factory<Command> {

    private YggdrasilCore core;
    private YggdrasilShellCommandManager commandManager;

    public YggdrasilShellFactory(YggdrasilCore yggdrasilCore, YggdrasilShellCommandManager masterCommandManager) {
        core = yggdrasilCore;
        commandManager = masterCommandManager;
    }

    @Override
    public Command create() {
        return new YggdrasilShell(core, commandManager);
    }
}
