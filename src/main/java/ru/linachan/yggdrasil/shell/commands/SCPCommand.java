package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.common.SSHUtils;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@ShellCommand(command = "scp", description = "Copy files over SSH")
public class SCPCommand extends YggdrasilShellCommand {

    @Override
    protected void init() throws IOException {

    }

    public void execute() throws IOException {
        if (kwargs.containsKey("t")) {
            String targetName = kwargs.get("t");

            console.write(new byte[] {0});

            char[] op = new char[1];
            console.read(op);

            switch (op[0]) {
                case 'T':
                    console.readLine();
                    break;
                case 'C':
                    String[] fileHeader = console.readLine().split(" ");

                    console.write(new byte[] {0});

                    int fileSize = Integer.parseInt(fileHeader[1]);

                    ByteArrayOutputStream fileContents = new ByteArrayOutputStream();

                    int totalRead = 0;

                    int bytesRead;
                    byte[] buffer = new byte[1024];

                    while (totalRead < fileSize) {
                        bytesRead = console.read(buffer);
                        fileContents.write(buffer);
                        totalRead += bytesRead;
                    }

                    console.write(new byte[] {0});

                    processFile(targetName, fileContents.toByteArray());

                    break;
                case 'D':
                    // No directory copying support required
                    exit(1);
                    break;
                case 'E':
                    // No directory copying support required
                    exit(1);
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processFile(String targetName, byte[] rawData) throws IOException {
        logger.info("Processing file: '{}'", targetName);
        switch (targetName) {
            case "publicKey":
                try {
                    YggdrasilAuthUser authUser = core.getAuthManager().getUser(getEnvironment().getEnv().get("USER"));
                    authUser.addPublicKey(new String(rawData));
                    core.getAuthManager().updateUser(authUser);
                } catch (GeneralSecurityException e) {
                    console.writeLine("Unable to read SSH public key: %s", e.getMessage());
                }
                break;
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
