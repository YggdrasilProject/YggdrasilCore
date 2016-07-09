package ru.linachan.mmo;

import ru.linachan.mmo.auth.AuthManager;
import ru.linachan.mmo.auth.SessionManager;
import ru.linachan.mmo.handler.RealmServerHandler;
import ru.linachan.mmo.handler.WorldServerHandler;
import ru.linachan.mmo.middleware.SessionMiddleware;
import ru.linachan.mmo.middleware.LogMiddleware;
import ru.linachan.yggdrasil.common.nio.server.Server;
import ru.linachan.yggdrasil.common.nio.server.ServerBuilder;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.helpers.AutoStart;
import ru.linachan.yggdrasil.plugin.helpers.Plugin;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoStart
@Plugin(name = "MMOServer", description = "MMOServer implementation.")
public class MMOPlugin extends YggdrasilPlugin {

    private Server realmServer;
    private Server worldServer;

    private AuthManager authManager;
    private SessionManager sessionManager;

    private ExecutorService serverPool = Executors.newScheduledThreadPool(10);

    @Override
    protected void onInit() {
        authManager = new AuthManager(core);
        sessionManager = new SessionManager();

        try { authManager.register("velovec", "qwerty"); } catch (IOException ignored) {}

        realmServer = new ServerBuilder(core.getConfig().getInt("mmo.realm.port", 41596))
            // .registerMiddleware(new LogMiddleware(core))
            .setHandler(new RealmServerHandler(core))
            .build();

        worldServer = new ServerBuilder(core.getConfig().getInt("mmo.world.port", 41597))
            // .registerMiddleware(new LogMiddleware(core))
            .registerMiddleware(new SessionMiddleware(core))
            .setHandler(new WorldServerHandler(core))
            .build();

        serverPool.submit(realmServer);
        serverPool.submit(worldServer);
    }

    @Override
    protected void onShutdown() {
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
