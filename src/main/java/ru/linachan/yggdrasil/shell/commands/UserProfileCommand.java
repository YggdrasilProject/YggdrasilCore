package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.auth.YggdrasilAuthManager;
import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.common.SSHUtils;
import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
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
        console.writeList(
            userList.stream()
                .map(YggdrasilAuthUser::getUserName)
                .collect(Collectors.toList()),
            "Users"
        );
    }

    @CommandAction("Show user profile")
    public void show() throws IOException {
        String userName = (args.size() > 0) ? args.get(0) : getEnvironment().getEnv().get("USER");

        YggdrasilAuthUser authUser = authManager.getUser(userName);

        Map<String, String> userAttributes = new HashMap<>();

        authUser.listAttributes().stream()
            .forEach(attribute -> userAttributes.put(attribute, String.valueOf(authUser.getAttribute(attribute))));

        console.writeMap(userAttributes, "Attribute", "Value");
    }

    @CommandAction("Add user")
    public void add() throws IOException {
        console.writeLine(console.format("Not implemented", ConsoleColor.RED, null, null, true));
    }

    @CommandAction("Delete user")
    public void delete() throws IOException {
        console.writeLine(console.format("Not implemented", ConsoleColor.RED, null, null, true));
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
    public void delete_key() throws IOException {
        console.writeLine(console.format("Not implemented", ConsoleColor.RED, null, null, true));
    }

    @Override
    protected void onInterrupt() {

    }
}
