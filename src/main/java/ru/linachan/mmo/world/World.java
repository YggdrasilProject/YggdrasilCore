package ru.linachan.mmo.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.mmo.auth.user.Session;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.vector.Vector2;

import java.util.HashMap;
import java.util.Map;

public class World {

    private final YggdrasilCore core;

    private final Vector2<Double, Double> dimensions;
    private final Vector2<Integer, Integer> regionCount;

    private final Vector2<Double, Double> regionSize;

    private Map<Vector2<Integer, Integer>, Region> regionMap = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(World.class);

    public World(YggdrasilCore yggdrasilCore, Vector2<Double, Double> size, Vector2<Integer, Integer> regions) {
        core = yggdrasilCore;

        dimensions = size;
        regionCount = regions;

        regionSize = new Vector2<>(
            size.getX() / regionCount.getX(),
            size.getY() / regionCount.getY()
        );

        for (int x = 0; x < regionCount.getX(); x++) {
            for (int y = 0; y < regionCount.getY(); y++) {
                Vector2<Integer, Integer> regionPosition = new Vector2<>(x, y);
                regionMap.put(regionPosition, new Region(core, regionPosition));
            }
        }
    }

    public void updatePlayer(Player player) {
        Vector2<Double, Double> pos = player.getPosition();
        Vector2<Double, Double> aoi = player.getAreaOfInterest();

        Vector2<Integer, Integer> regionPosition = getRegionByPosition(pos);

        Vector2<Integer, Integer> topLeft = getRegionByPosition(new Vector2<>(pos.getX() - aoi.getX(), pos.getY() - aoi.getY()));
        Vector2<Integer, Integer> topRight = getRegionByPosition(new Vector2<>(pos.getX() + aoi.getX(), pos.getY() - aoi.getY()));
        Vector2<Integer, Integer> bottomLeft = getRegionByPosition(new Vector2<>(pos.getX() - aoi.getX(), pos.getY() + aoi.getY()));
        Vector2<Integer, Integer> bottomRight = getRegionByPosition(new Vector2<>(pos.getX() + aoi.getX(), pos.getY() + aoi.getY()));

        Region region = regionMap.get(regionPosition);
        region.sendUpdate(player);

        player.unsubscribe();
        for (int x = Math.max(0, topLeft.getX()); x < Math.min(bottomRight.getX(), regionCount.getX()); x++) {
            for (int y = Math.max(0, topRight.getY()); y < Math.min(bottomLeft.getY(), regionCount.getY()); y++) {
                player.subscribe(regionMap.get(new Vector2<>(x, y)));
            }
        }
    }

    private Vector2<Integer, Integer> getRegionByPosition(Vector2<Double, Double> position) {
        int regionX = (int) (position.getX() / regionSize.getX());
        int regionY = (int) (position.getY() / regionSize.getY());

        return new Vector2<>(regionX, regionY);
    }

    public Player getPlayer(Session session) {
        Player player = (Player) session.getAttribute("player", null);

        if (player == null) {
            player = new Player();

            player.setPosition(new Vector2<>(500.0, 500.0));
            player.setAreaOfInterest(new Vector2<>(10.0, 10.0));

            session.setAttribute("player", player);
        }

        return player;
    }
}
