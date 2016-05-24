package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.common.console.ConsoleUtils;
import ru.linachan.yggdrasil.common.console.InterruptHandler;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@ShellCommand(command = "passwd", description = "Change current user password")
public class PasswordCommand extends YggdrasilShellCommand {

    private boolean isChanging = true;

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException, NoSuchAlgorithmException {
        console.setBright(true);

        while (isChanging) {
            console.setTextColor(ConsoleColor.WHITE);
            String newPassword = console.readPassword("Enter password: ");
            String passwordConfirmation = console.readPassword("Confirm password: ");

            if (newPassword.equals(passwordConfirmation)) {
                if (console.readYesNo("Save this password?")) {
                    YggdrasilAuthUser user = core.getAuthManager().getUser(getEnvironment().getEnv().get("USER"));

                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(newPassword.getBytes("UTF-8"));
                    byte[] digest = md.digest();

                    user.setAttribute("passWord", String.format("%064x", new java.math.BigInteger(1, digest)));
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
