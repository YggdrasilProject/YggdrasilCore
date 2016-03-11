package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetEnvCommand extends YggdrasilShellCommand {

    public static String commandName = "getenv";
    public static String commandDescription = "List environment variables";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        int maximalLength = 0;

        Map<String, String> env = getEnvironment().getEnv();

        for (String variableName: env.keySet()) {
            maximalLength = (variableName.length() > maximalLength) ? variableName.length() : maximalLength;
        }

        for (String variableName: env.keySet()) {
            output.write(String.format(
                String.format("%%%ds : %%s\r\n", maximalLength),
                variableName, env.get(variableName)
            ));
        }
        output.flush();

        exit(0);
    }
}
