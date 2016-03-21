package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.common.console.ConsoleUtils;
import ru.linachan.yggdrasil.common.console.InterruptHandler;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PasswordCommand extends YggdrasilShellCommand {

    public static String commandName = "passwd";
    public static String commandDescription = "Change current user password";

    private boolean isChanging = true;

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        console.setBright(true);

        while (isChanging) {
            console.setTextColor(ConsoleColor.WHITE);
            String newPassword = console.readPassword("Enter password: ");
            String passwordConfirmation = console.readPassword("Confirm password: ");

            if (newPassword.equals(passwordConfirmation)) {
                if (console.readYesNo("Save this password?")) {
                    YggdrasilAuthUser user = core.getAuthManager().getUser(getEnvironment().getEnv().get("USER"));
                    user.setAttribute("passWord", newPassword);
                    core.getAuthManager().updateUser(user);

                    console.setTextColor(ConsoleColor.GREEN);
                    console.writeLine("Password changed");
                    console.setTextColor(null);
                } else {
                    console.setTextColor(ConsoleColor.RED);
                    console.writeLine("Canceled");
                    console.setTextColor(null);

                    exit(1);
                }

                isChanging = false;
            } else {
                console.setTextColor(ConsoleColor.RED);
                console.writeLine("Password mismatch");
                console.setTextColor(null);
            }
        }
    }

    @Override
    protected void onInterrupt() {
        isChanging = false;
    }
}