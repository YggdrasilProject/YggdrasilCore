package ru.linachan.mmo.middleware;

import ru.linachan.mmo.MMOPlugin;
import ru.linachan.mmo.auth.user.Session;
import ru.linachan.mmo.auth.user.Token;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.nio.server.ServerMiddleware;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class SessionMiddleware implements ServerMiddleware {

    private YggdrasilCore core;

    public SessionMiddleware(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;
    }

    @Override
    public ByteBuffer decode(ByteBuffer inputBuffer, SelectionKey key) {
        if (inputBuffer.capacity() >= 128) {
            byte[] token = new byte[128];
            inputBuffer.get(token);

            Session session = core.getManager(YggdrasilPluginManager.class)
                .get(MMOPlugin.class)
                .getSessionManager()
                .getSession(new Token(token));

            if (session != null) {
                session.setAttribute("key", key);
                session.update();
            }

            byte[] inputBytes = new byte[inputBuffer.remaining()];
            inputBuffer.get(inputBytes);

            return ByteBuffer.wrap(inputBytes);
        }

        return ByteBuffer.allocate(0);
    }

    @Override
    public ByteBuffer encode(ByteBuffer outputBuffer, SelectionKey key) {
        return outputBuffer;
    }
}
