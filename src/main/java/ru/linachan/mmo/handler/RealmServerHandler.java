package ru.linachan.mmo.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.mmo.MMOPlugin;
import ru.linachan.mmo.auth.AuthManager;
import ru.linachan.mmo.auth.SessionManager;
import ru.linachan.mmo.auth.user.Session;
import ru.linachan.mmo.auth.user.Token;
import ru.linachan.mmo.auth.user.User;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.nio.server.ServerHandler;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Base64;

public class RealmServerHandler implements ServerHandler {

    private AuthManager authManager;
    private SessionManager sessionManager;

    private YggdrasilCore core = YggdrasilCore.INSTANCE;

    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Logger logger = LoggerFactory.getLogger(RealmServerHandler.class);

    public RealmServerHandler() {
        authManager = core.getManager(YggdrasilPluginManager.class).get(MMOPlugin.class).getAuthManager();
        sessionManager = core.getManager(YggdrasilPluginManager.class).get(MMOPlugin.class).getSessionManager();
    }

    @Override
    public ByteBuffer onMessageReceived(ByteBuffer input, SelectionKey key) {
        short opCode = input.getShort();

        switch (opCode) {
            case 0x01: // Log In
                return logIn(input);
            case 0x02: // Log Out
                return logOut(input);
            case 0x03: // Get Server port
                return getServerPort();
            case 0x00:
                key.cancel();
                return ByteBuffer.allocate(0);
        }

        return ByteBuffer.allocate(0);
    }

    private ByteBuffer logIn(ByteBuffer logInData) {
        int loginLength = logInData.getInt();
        int passwordLength = logInData.getInt();

        byte[] loginBytes = new byte[loginLength];
        byte[] passwordBytes = new byte[passwordLength];

        logInData.get(loginBytes);
        logInData.get(passwordBytes);

        String login = new String(decoder.decode(loginBytes));
        String password = new String(decoder.decode(passwordBytes));

        ByteBuffer outputBuffer;

        try {
            User user = authManager.login(login, password);

            if (user != null) {
                Session session = sessionManager.createSession(user);

                outputBuffer = ByteBuffer.allocate(130);
                outputBuffer.putShort((short) 1);
                outputBuffer.put(session.getToken().getBytes());
            } else {
                outputBuffer = ByteBuffer.allocate(4);
                outputBuffer.putShort((short) 2);
            }
        } catch (ClassNotFoundException | IOException e) {
            logger.error("Unable to authenticate user: {}", e.getMessage());

            outputBuffer = ByteBuffer.allocate(4);
            outputBuffer.putShort((short) 3);
        }

        return outputBuffer;
    }

    private ByteBuffer logOut(ByteBuffer logOutData) {
        byte[] token = new byte[128];
        logOutData.get(token);

        sessionManager.closeSession(new Token(token));

        return ByteBuffer.allocate(0);
    }

    private ByteBuffer getServerPort() {
        ByteBuffer outputBuffer = ByteBuffer.allocate(4);
        outputBuffer.putInt(core.getConfig().getInt("mmo.world.port", 41597));
        return outputBuffer;
    }

    @Override public void onConnect(SelectionKey key) {}
    @Override public void onDisconnect(SelectionKey key) { sessionManager.closeSession(key); }
    @Override public void onStart(boolean isRestarting) { logger.info("RealmServer {}", (isRestarting) ? "restarted" : "started"); }
    @Override public void onStop() { logger.info("RealmServer stopped"); }
}
