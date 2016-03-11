package ru.linachan.yggdrasil.shell;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.CommandLineUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

public abstract class YggdrasilShellCommand implements Command, Runnable {

    protected YggdrasilCore core;

    protected InputStreamReader input;
    protected OutputStreamWriter output;
    protected OutputStreamWriter error;

    private ExitCallback exitCallback;
    private Environment environment;

    private Thread commandThread;

    private String command;
    private List<String> args;
    private Map<String, String> kwargs;

    private boolean isRunning = true;

    protected YggdrasilShellCommandManager commandManager;

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilShellCommand.class);

    public static String commandName = null;
    public static String commandDescription = null;

    public void setUpCommand(
        YggdrasilCore yggdrasilCore,
        YggdrasilShellCommandManager cmdManager,
        CommandLineUtils.CommandLine commandObject
    ) {
        core = yggdrasilCore;

        args = commandObject.getArgs();
        kwargs = commandObject.getKeywordArgs();
        command = commandObject.getCmd();

        commandManager = cmdManager;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        input = new InputStreamReader(inputStream);
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        output = new OutputStreamWriter(outputStream);
    }

    @Override
    public void setErrorStream(OutputStream errorStream) {
        error = new OutputStreamWriter(errorStream);
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        exitCallback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        environment = env;

        commandThread = new Thread(this);
        commandThread.start();

        isRunning = true;
    }

    @Override
    public void destroy() {
        isRunning = false;
        commandThread.interrupt();
    }

    @Override
    public void run() {
        try {
            execute(command, args, kwargs);
        } catch (IOException e) {
            logger.error("Unable to execute command", e);
            exit(1);
        }
        isRunning = false;
    }

    protected abstract void execute(String command, List<String> strings, Map<String, String> args) throws IOException;

    protected void exit(Integer exitCode) {
        exitCallback.onExit(exitCode);
    }

    protected void exit(Integer exitCode, String exitMessage) {
        exitCallback.onExit(exitCode, exitMessage);
    }

    protected Environment getEnvironment() {
        return environment;
    }

    protected boolean isRunning() {
        return isRunning;
    }
}
