package ru.linachan.yggdrasil.shell;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.common.console.InterruptHandler;
import ru.linachan.yggdrasil.shell.commands.EmptyCommand;
import ru.linachan.yggdrasil.shell.commands.InvalidCommand;
import ru.linachan.yggdrasil.shell.commands.UnknownCommand;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.console.CommandLineUtils;
import ru.linachan.yggdrasil.common.console.ConsoleUtils;

import java.io.*;

public class YggdrasilShell implements Command, Runnable, ExitCallback, InterruptHandler {

    private YggdrasilCore core;
    private YggdrasilShellCommandManager commandManager;

    private InputStream input;
    private OutputStream output;
    private OutputStream error;

    private ConsoleUtils console;

    private ExitCallback exitCallback;
    private Environment environment;

    private Thread commandThread;
    private boolean isRunning = true;

    private YggdrasilShellCommand subCommand;
    private boolean subCommandIsRunning = false;
    private Integer subCommandExitCode = 0;

    private static Logger logger = LoggerFactory.getLogger(YggdrasilShell.class);

    public YggdrasilShell(YggdrasilCore yggdrasilCore, YggdrasilShellCommandManager yggdrasilShellCommandManager) {
        core = yggdrasilCore;
        commandManager = yggdrasilShellCommandManager;
    }

    @Override
    public void setInputStream(InputStream inputStreamObject) {
        input = inputStreamObject;
    }

    @Override
    public void setOutputStream(OutputStream outputStreamObject) {
        output = outputStreamObject;
    }

    @Override
    public void setErrorStream(OutputStream errorStreamObject) {
        error = errorStreamObject;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        exitCallback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        environment = env;

        console = new ConsoleUtils(input, output, error);

        console.addCompletions(commandManager.listCommands());
        console.setInterruptHandler(this);

        console.setBright(true);

        commandThread = new Thread(this);
        commandThread.start();

        isRunning = true;
    }

    @Override
    public void destroy() {
        isRunning = false;
        if (subCommand != null) {
            subCommand.destroy();
        }
        commandThread.interrupt();
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                String commandLine = console.read(String.format(
                    "%s%s",
                    console.format(environment.getEnv().get("USER"), ConsoleColor.CYAN, null, null, true),
                    console.format(" # ", (subCommandExitCode == 0) ? ConsoleColor.GREEN : ConsoleColor.RED, null, null, true)
                ));

                console.addHistoryItem(commandLine);

                CommandLineUtils.CommandLine command = CommandLineUtils.parse(commandLine);

                subCommand = routeCommand(command.getCmd());
                subCommand.setUpCommand(core, commandManager, command);

                subCommand.setExitCallback(this);

                subCommand.setInputStream(input);
                subCommand.setOutputStream(output);
                subCommand.setErrorStream(error);

                subCommandIsRunning = true;

                subCommand.start(environment);

                try {
                    while (subCommandIsRunning||subCommand.isRunning()) {
                        Thread.sleep(10);
                    }

                    subCommand.destroy();
                } catch (InterruptedException ignored) {}

                output.flush();
                error.flush();

            } catch (IOException e) {
                logger.error("Unable to process ShellRequest: [{}] {}", e.getClass().getSimpleName(), e.getMessage());
            }
        }

        exit(0);
    }

    private YggdrasilShellCommand routeCommand(String command) {
        if (command == null) {
            return new InvalidCommand();
        }

        if (command.equals("")) {
            return new EmptyCommand();
        }

        YggdrasilShellCommand commandObject = commandManager.getCommand(command);
        if (commandObject != null) {
            return commandObject;
        }

        return new UnknownCommand();
    }

    private void exit(Integer exitCode) {
        exitCallback.onExit(exitCode);
    }

    private boolean isRunning() {
        return isRunning;
    }

    public void onExit(int exitCode) {
        subCommandIsRunning = false;
        subCommandExitCode = exitCode;
    }

    public void onExit(int exitCode, String exitMessage) {
        subCommandIsRunning = false;
        subCommandExitCode = exitCode;

        if (exitMessage.equals("logout")) {
            isRunning = false;
        }
    }

    @Override
    public void onEOTEvent() {
        isRunning = false;
    }

    @Override
    public void onETXEvent() {
        isRunning = false;
    }

    @Override
    public void onSUBEvent() {
        isRunning = false;
    }
}
