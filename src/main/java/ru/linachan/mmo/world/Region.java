package ru.linachan.mmo.world;

import ru.linachan.mmo.MMOPlugin;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.nio.server.Server;
import ru.linachan.yggdrasil.common.vector.Vector2;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Region {

    private final YggdrasilCore core;
    private List<Player> players = new CopyOnWriteArrayList<>();

    private Vector2<Integer, Integer> position;

    public Region(YggdrasilCore yggdrasilCore, Vector2<Integer, Integer> regionPosition) {
        core = yggdrasilCore;
        position = regionPosition;
    }

    public void unsubscribe(Player player) {
        if (players.contains(player)) {
            players.remove(player);
        }
    }

    public void subscribe(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void sendUpdate(Player playerInfo) {
        Server world = core.getManager(YggdrasilPluginManager.class).get(MMOPlugin.class).getWorldServer();
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Region(%s, %s)", position.getX(), position.getY());
    }
}
