package ru.linachan.yggdrasil.shell;


import org.apache.sshd.server.forward.RejectAllForwardingFilter;
import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.service.YggdrasilService;

import org.apache.sshd.server.*;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class YggdrasilShellService extends YggdrasilService {

    private SshServer shellServer;

    @Override
    protected void onInit() {
        YggdrasilShellCommandManager commandManager = new YggdrasilShellCommandManager();
        commandManager.setUpManager(core);

        shellServer = SshServer.setUpDefaultServer();

        shellServer.setPort(core.getConfig().getInt("yggdrasil.ssh.port", 41598));
        shellServer.setHost(core.getConfig().getString("yggdrasil.ssh.host", "0.0.0.0"));

        shellServer.getProperties().put(SshServer.IDLE_TIMEOUT, 86400000L);

        shellServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
            new File(core.getConfig().getString("yggdrasil.ssh.key", "master.ser"))
        ));

        shellServer.setPublickeyAuthenticator((userName, publicKey, serverSession) -> {
            YggdrasilAuthUser authUser = core.getAuthManager().getUser(userName);

            if (authUser != null) {
                return new String(Base64.getEncoder().encode(publicKey.getEncoded())).equals(authUser.getAttribute("publicKey"));
            } else if (core.getConfig().getString("yggdrasil.admin.user", "yggdrasil").equals(userName)) {
                authUser = core.getAuthManager().registerUser(userName);
                authUser.setAttribute("publicKey", new String(Base64.getEncoder().encode(publicKey.getEncoded())));
                core.getAuthManager().updateUser(authUser);

                return true;
            }

            return false;
        });

        shellServer.setPasswordAuthenticator((userName, passWord, serverSession) -> {
            YggdrasilAuthUser authUser = core.getAuthManager().getUser(userName);

            if (authUser != null) {
                return passWord.equals(authUser.getAttribute("passWord"));
            } else if (core.getConfig().getString("yggdrasil.admin.user", "yggdrasil").equals(userName)) {
                authUser = core.getAuthManager().registerUser(userName);
                authUser.setAttribute("passWord", passWord);
                core.getAuthManager().updateUser(authUser);

                return true;
            }

            return false;
        });

        shellServer.setTcpipForwardingFilter(RejectAllForwardingFilter.INSTANCE);

        shellServer.setCommandFactory(new YggdrasilShellCommandFactory(core, commandManager));
        shellServer.setShellFactory(new YggdrasilShellFactory(core, commandManager));

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
