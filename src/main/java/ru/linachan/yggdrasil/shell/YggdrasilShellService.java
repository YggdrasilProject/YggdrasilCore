package ru.linachan.yggdrasil.shell;


import org.apache.sshd.server.forward.RejectAllForwardingFilter;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.service.YggdrasilService;

import org.apache.sshd.server.*;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class YggdrasilShellService extends YggdrasilService {

    private SshServer shellServer;

    @Override
    @SuppressWarnings("unchecked")
    protected void onInit() {
        YggdrasilShellCommandManager commandManager = new YggdrasilShellCommandManager();
        commandManager.setUpManager();

        shellServer = SshServer.setUpDefaultServer();

        shellServer.setPort(core.getConfig().getInt("yggdrasil.ssh.port", 41598));
        shellServer.setHost(core.getConfig().getString("yggdrasil.ssh.host", "0.0.0.0"));

        shellServer.getProperties().put(SshServer.IDLE_TIMEOUT, 86400000L);

        AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider(
            new File(core.getConfig().getString("yggdrasil.ssh.key", "master.ser"))
        );

        hostKeyProvider.setAlgorithm("RSA");

        shellServer.setKeyPairProvider(hostKeyProvider);

        shellServer.setPublickeyAuthenticator((userName, publicKey, serverSession) -> {
            YggdrasilAuthUser authUser = core.getAuthManager().getUser(userName);

            if (authUser != null) {
                String publicKeyString = new String(Base64.getEncoder().encode(publicKey.getEncoded()));

                for (String publicKeyData: ((List<String>) authUser.getAttribute("publicKey"))) {
                    if (publicKeyString.equals(publicKeyData)) {
                        return true;
                    }
                }

                authUser.setAttribute("tmpPublicKey", publicKeyString);
            } else if (core.getConfig().getString("yggdrasil.admin.user", "yggdrasil").equals(userName)) {
                authUser = core.getAuthManager().registerUser(userName);

                List<String> publicKeyList = new ArrayList<>();
                publicKeyList.add(new String(Base64.getEncoder().encode(publicKey.getEncoded())));
                authUser.setAttribute("publicKey", publicKeyList);

                core.getAuthManager().updateUser(authUser);

                return true;
            }

            return false;
        });

        shellServer.setPasswordAuthenticator((userName, passWord, serverSession) -> {
            YggdrasilAuthUser authUser = core.getAuthManager().getUser(userName);

            if (authUser != null) {
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                    md.update(passWord.getBytes("UTF-8"));
                    byte[] digest = md.digest();
                    return String.format("%064x", new java.math.BigInteger(1, digest))
                        .equals(authUser.getAttribute("passWord"));
                } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                    logger.error("Unable to calculate SHA-256: {}", e.getMessage());
                }

                return false;
            } else if (core.getConfig().getString("yggdrasil.admin.user", "yggdrasil").equals(userName)) {
                authUser = core.getAuthManager().registerUser(userName);
                authUser.setAttribute("passWord", passWord);
                core.getAuthManager().updateUser(authUser);

                return true;
            }

            return false;
        });

        shellServer.setTcpipForwardingFilter(RejectAllForwardingFilter.INSTANCE);

        shellServer.setCommandFactory(new YggdrasilShellCommandFactory(commandManager));
        shellServer.setShellFactory(new YggdrasilShellFactory(commandManager));

        try {
            shellServer.start();
        } catch (IOException e) {
            logger.error("Unable to start MasterPlugin SSH server", e);
        }
    }

    @Override
    protected void onShutdown() {
        try {
            shellServer.stop();
        } catch (IOException e) {
            logger.error("Unable to stop MasterPlugin SSH server properly", e);
        }
    }

    @Override
    public void run() {
        // do nothing, service already working
    }
}
