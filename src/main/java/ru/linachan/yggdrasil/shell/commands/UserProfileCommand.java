package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.auth.YggdrasilAuthManager;
import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.common.console.ANSIUtils;
import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.common.console.ConsoleTextStyle;
import ru.linachan.yggdrasil.common.console.tables.Table;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@ShellCommand(command = "users", description = "Manage users settings")
public class UserProfileCommand extends YggdrasilShellCommand {

    private YggdrasilAuthManager authManager;

    @Override
    protected void init() throws IOException {
        authManager = core.getAuthManager();
    }

    @CommandAction("List users")
    public void list() throws IOException {
        List<YggdrasilAuthUser> userList = authManager.listUsers();
        Table users = new Table(
            userList.stream()
                .map(YggdrasilAuthUser::getUserName)
                .collect(Collectors.toList()),
            "Users"
        );
        console.writeTable(users);
    }

    @CommandAction("Show user profile")
    public void show() throws IOException {
        String userName = (args.size() > 0) ? args.get(0) : getEnvironment().getEnv().get("USER");

        YggdrasilAuthUser authUser = authManager.getUser(userName);

        Map<String, String> userAttributes = new HashMap<>();

        authUser.listAttributes().stream()
            .filter(attribute -> !attribute.equals("publicKey"))
            .filter(attribute -> !attribute.equals("passWord"))
            .forEach(attribute -> userAttributes.put(attribute, String.valueOf(authUser.getAttribute(attribute))));

        console.writeTable(new Table(userAttributes, "Attribute", "Value"));
    }

    @CommandAction("Add user")
    public void add() throws IOException, NoSuchAlgorithmException {
        String userName = (args.size() > 0) ? args.get(0) : null;

        if (userName != null) {
            YggdrasilAuthUser user = core.getAuthManager().getUser(userName);

            if (user != null) {
                console.writeLine("User %s already exists!", userName);
                exit(1);
            } else {
                boolean passwordSet = false;
                user = core.getAuthManager().registerUser(userName);

                while (!passwordSet) {
                    String password = console.readPassword("Enter password: ");
                    String passwordConfirmation = console.readPassword("Confirm password: ");

                    if (password.equals(passwordConfirmation)) {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(password.getBytes("UTF-8"));
                        byte[] digest = md.digest();

                        user.setAttribute("passWord", String.format("%064x", new java.math.BigInteger(1, digest)));
                        core.getAuthManager().updateUser(user);

                        console.writeLine("User created");

                        passwordSet = true;
                    } else {
                        console.writeLine("Password mismatch");
                    }
                }
            }
        } else {
            console.writeLine("Username not specified");
            exit(1);
        }
    }

    @CommandAction("Delete user")
    public void delete() throws IOException {
        String userName = (args.size() > 0) ? args.get(0) : null;

        if (userName != null) {
            YggdrasilAuthUser user = core.getAuthManager().getUser(userName);

            if (user != null) {
                if (console.readYesNo("Do you really want to delete this user?")) {
                    core.getAuthManager().deleteUser(user);
                }
            } else {
                console.writeLine("User %s doesn't exists!", userName);
                exit(1);
            }
        } else {
            console.writeLine("Username not specified");
            exit(1);
        }
    }

    @CommandAction("Set SSH public key")
    public void set_key() throws IOException {
        try {
            YggdrasilAuthUser authUser = core.getAuthManager().getUser(getEnvironment().getEnv().get("USER"));
            authUser.addPublicKey(console.readLine());
            core.getAuthManager().updateUser(authUser);
        } catch (GeneralSecurityException e) {
            console.writeLine("Unable to read SSH public key: %s", e.getMessage());
        }
    }

    @CommandAction("Delete SSH public key")
    public void delete_key() {
        if (args.size() > 0) {
            YggdrasilAuthUser authUser = core.getAuthManager().getUser(getEnvironment().getEnv().get("USER"));
            authUser.deletePublicKey(Integer.parseInt(args.get(0)));
            core.getAuthManager().updateUser(authUser);
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
