package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.common.ConsoleColor;
import ru.linachan.yggdrasil.common.ConsoleUtils;
import ru.linachan.yggdrasil.common.InterruptHandler;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PasswordCommand extends YggdrasilShellCommand implements InterruptHandler {

    public static String commandName = "passwd";
    public static String commandDescription = "Change current user password";

    private boolean isChanging = true;

    @Override
    protected void execute(String command, List<String> strings, Map<String, String> args) throws IOException {
        ConsoleUtils console = new ConsoleUtils(input, output, error);

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

                    exit(0);
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
    public void onEOTEvent() {
        isChanging = false;
        exit(1);
    }

    @Override
    public void onETXEvent() {
        isChanging = false;
        exit(1);
    }

    @Override
    public void onSUBEvent() {
        isChanging = false;
        exit(1);
    }
}
