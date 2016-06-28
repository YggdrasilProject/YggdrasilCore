package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.common.console.ANSIUtils;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "clear", description = "Erases the screen with the background colour")
public class ClearCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        console.write(ANSIUtils.CursorPosition(0, 0));
        console.write(ANSIUtils.EraseData(2));
    }

    @Override
    protected void onInterrupt() {}
}
