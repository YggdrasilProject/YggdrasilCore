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
        while (isChanging) {
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

                    console.writeLine("Password changed");
                } else {
                    console.writeLine("Canceled");

                    exit(1);
                }

                isChanging = false;
            } else {
                console.writeLine("Password mismatch");
            }
        }
    }

    @Override
    protected void onInterrupt() {
        isChanging = false;
    }
}
