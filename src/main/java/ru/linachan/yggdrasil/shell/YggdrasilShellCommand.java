package ru.linachan.yggdrasil.shell;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.console.CommandLineUtils;
import ru.linachan.yggdrasil.common.console.ConsoleUtils;
import ru.linachan.yggdrasil.common.console.InterruptHandler;
import ru.linachan.yggdrasil.common.console.tables.Table;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class YggdrasilShellCommand implements Command, Runnable, InterruptHandler {

    protected static YggdrasilCore core = YggdrasilCore.INSTANCE;

    protected InputStream input;
    protected OutputStream output;
    protected OutputStream error;

    protected InputStreamReader inputReader;
    protected OutputStreamWriter outputWriter;
    protected OutputStreamWriter errorWriter;

    protected ConsoleUtils console;

    private ExitCallback exitCallback;
    private Environment environment;

    private Thread commandThread;

    protected String command;
    protected List<String> args;
    protected Map<String, String> kwargs;

    private boolean isRunning = true;
    private boolean exited = false;

    protected YggdrasilShellCommandManager commandManager;

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilShellCommand.class);

    public void setUpCommand(YggdrasilShellCommandManager cmdManager, CommandLineUtils.CommandLine commandObject) {
        args = commandObject.getArgs();
        kwargs = commandObject.getKeywordArgs();
        command = commandObject.getCmd();

        commandManager = cmdManager;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        input = inputStream;
        inputReader = new InputStreamReader(inputStream);
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        output = outputStream;
        outputWriter = new OutputStreamWriter(outputStream);
    }

    @Override
    public void setErrorStream(OutputStream errorStream) {
        error = errorStream;
        errorWriter = new OutputStreamWriter(errorStream);
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
        onInterrupt();
        commandThread.interrupt();
    }

    @Override
    public void run() {
        try {
            console = new ConsoleUtils(input, output, error);

            init();

            String action = null;
            if (args.size() > 0) {
                action = args.get(0);
                args.remove(0);
            }

            dispatch(action);
        } catch (IOException e) {
            logger.error("Unable to execute command", e);
            exit(1);
        }

        if (!exited) {
            exited = true;
            exit(0);
        }

        isRunning = false;
    }

    protected abstract void init() throws IOException;
    protected abstract void onInterrupt();

    protected void exit(Integer exitCode) {
        exitCallback.onExit(exitCode);
        exited = true;
    }

    protected void exit(Integer exitCode, String exitMessage) {
        exitCallback.onExit(exitCode, exitMessage);
        exited = true;
    }

    protected Environment getEnvironment() {
        return environment;
    }

    protected boolean isRunning() {
        return isRunning;
    }

    private void dispatch(String methodName) throws IOException {
        if (methodName != null) {
            try {
                try {
                    Method handler = getClass().getDeclaredMethod(methodName);

                    handler.invoke(this);
                } catch (NoSuchMethodException e) {
                    Method handler = getClass().getDeclaredMethod("execute");

                    handler.invoke(this);
                }
            } catch (NoSuchMethodException e) {
                console.writeLine("Unknown method: %s", methodName);
            } catch (InvocationTargetException e) {
                console.writeException(e.getTargetException());
            } catch (IllegalAccessException e) {
                console.writeLine("Unable to perform action '%s': %s", methodName, e.getClass().getSimpleName(), e.getMessage());
            }
        } else {
            try {
                Method handler = getClass().getDeclaredMethod("execute");

                handler.invoke(this);
            } catch (NoSuchMethodException e) {
                Table commandMethods = new Table("Action", "Description");
                for (Method method: getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(CommandAction.class)) {
                        commandMethods.addRow(method.getName(), method.getAnnotation(CommandAction.class).value());
                    }
                }
                console.writeTable(commandMethods);
            } catch (IllegalAccessException | InvocationTargetException e) {
                console.writeLine("Unable to execute command: %s", e.getMessage());
            }
        }
    }

    @Override
    public void onEOTEvent() {
        destroy();
    }

    @Override
    public void onETXEvent() {
        destroy();
    }

    @Override
    public void onSUBEvent() {
        destroy();
    }
}
