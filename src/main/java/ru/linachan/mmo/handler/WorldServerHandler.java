package ru.linachan.mmo.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.mmo.MMOPlugin;
import ru.linachan.mmo.auth.SessionManager;
import ru.linachan.mmo.auth.user.Session;
import ru.linachan.mmo.auth.user.User;
import ru.linachan.mmo.world.Player;
import ru.linachan.mmo.world.World;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.nio.server.ServerHandler;
import ru.linachan.yggdrasil.common.vector.Vector2;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class WorldServerHandler implements ServerHandler {

    private YggdrasilCore core;
    private SessionManager sessionManager;

    private World world;

    private static Logger logger = LoggerFactory.getLogger(WorldServerHandler.class);

    public WorldServerHandler(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;
        sessionManager = core.getManager(YggdrasilPluginManager.class)
            .get(MMOPlugin.class)
            .getSessionManager();

        world = new World(core, new Vector2<>(1000.0, 1000.0), new Vector2<>(100, 100));
    }

    @Override
    public ByteBuffer onMessageReceived(ByteBuffer input, SelectionKey key) {
        Session userSession = sessionManager.getSession(key);

        ByteBuffer output = ByteBuffer.allocate(0);

        if (userSession != null) {
            Player player = world.getPlayer(userSession);

            short opCode = input.getShort();
            switch (opCode) {
                case 0:
                    player.setPosition(new Vector2<>(
                        input.getDouble(), input.getDouble()
                    ));
                    world.updatePlayer(player);
                    break;
                case 1:
                    player.setAreaOfInterest(new Vector2<>(
                        input.getDouble(), input.getDouble()
                    ));
                    world.updatePlayer(player);
                    break;
            }
        } else {
            output = ByteBuffer.allocate(2);
            output.putShort((short) 0);
        }

        return output;
    }

    @Override public void onConnect(SelectionKey key) {}
    @Override public void onDisconnect(SelectionKey key) {}
    @Override public void onStart() {}
    @Override public void onStop() {}
}
