package ru.linachan.mmo;

import org.bouncycastle.util.encoders.Hex;
import ru.linachan.mmo.auth.AuthManager;
import ru.linachan.mmo.auth.SessionManager;
import ru.linachan.mmo.handler.RealmServerHandler;
import ru.linachan.mmo.handler.WorldServerHandler;
import ru.linachan.mmo.middleware.SessionMiddleware;
import ru.linachan.mmo.middleware.XORMiddleware;
import ru.linachan.yggdrasil.common.nio.server.Server;
import ru.linachan.yggdrasil.common.nio.server.ServerBuilder;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.helpers.AutoStart;
import ru.linachan.yggdrasil.plugin.helpers.Plugin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoStart
@Plugin(name = "MMOServer", description = "MMOServer implementation.")
public class MMOPlugin implements YggdrasilPlugin {

    private Server realmServer;
    private Server worldServer;

    private AuthManager authManager;
    private SessionManager sessionManager;

    private ExecutorService serverPool = Executors.newScheduledThreadPool(10);

    @Override
    public void onInit() {
        authManager = new AuthManager();
        sessionManager = new SessionManager();

        try { authManager.register("velovec", "qwerty"); } catch (IOException ignored) {}

        for (int i = 1; i <= 100; i++) {
            try { authManager.register("user" + i, "pass" + i); } catch (IOException ignored) {}
        }

        realmServer = new ServerBuilder(core.getConfig().getInt("mmo.realm.port", 41596))
            .registerMiddleware(new XORMiddleware())
            // .registerMiddleware(new LogMiddleware())
            .setHandler(new RealmServerHandler())
            .build();

        worldServer = new ServerBuilder(core.getConfig().getInt("mmo.world.port", 41597))
            .registerMiddleware(new XORMiddleware())
            .registerMiddleware(new SessionMiddleware())
            // .registerMiddleware(new LogMiddleware())
            .setHandler(new WorldServerHandler())
            .build();

        realmServer.setKeepAlive(true);
        worldServer.setKeepAlive(true);

        serverPool.submit(realmServer);
        serverPool.submit(worldServer);
    }

    @Override
    public void onShutdown() {
        realmServer.setKeepAlive(false);
        worldServer.setKeepAlive(false);

        realmServer.stop();
        worldServer.stop();

        authManager.shutdown();
    }

    public Server getWorldServer() {
        return worldServer;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
