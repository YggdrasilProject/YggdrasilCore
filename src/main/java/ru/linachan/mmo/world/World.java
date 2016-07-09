package ru.linachan.mmo.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.mmo.auth.user.Session;
import ru.linachan.mmo.world.interest.InterestManager;
import ru.linachan.yggdrasil.common.vector.Vector2;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class World {

    private final Vector2<Double, Double> dimensions;
    private final Vector2<Integer, Integer> regionCount;

    private final Vector2<Double, Double> regionSize;

    private Map<Vector2<Integer, Integer>, Region> regionMap = new HashMap<>();
    private List<Player> players = new CopyOnWriteArrayList<>();
    private InterestManager interestManager;

    private static Logger logger = LoggerFactory.getLogger(World.class);

    public World(Vector2<Double, Double> size, Vector2<Integer, Integer> regions) {
        dimensions = size;
        regionCount = regions;

        regionSize = new Vector2<>(
            size.getX() / regionCount.getX(),
            size.getY() / regionCount.getY()
        );

        interestManager = new InterestManager(this);

        for (int x = 0; x <= regionCount.getX(); x++) {
            for (int y = 0; y <= regionCount.getY(); y++) {
                Vector2<Integer, Integer> regionPosition = new Vector2<>(x, y);
                regionMap.put(regionPosition, new Region(regionPosition));
            }
        }
    }

    public void updatePlayer(Player player) {
        logger.info("update...");

        Vector2<Double, Double> pos = player.getPosition();
        Vector2<Double, Double> aoi = player.getInterestAreaSize();

        Vector2<Integer, Integer> regionPosition = getRegionByPosition(pos);

        Vector2<Integer, Integer> topLeft = getRegionByPosition(new Vector2<>(pos.getX() - aoi.getX(), pos.getY() - aoi.getY()));
        Vector2<Integer, Integer> bottomRight = getRegionByPosition(new Vector2<>(pos.getX() + aoi.getX(), pos.getY() + aoi.getY()));

        Region region = regionMap.get(regionPosition);
        player.getInterestArea().setArea(topLeft, bottomRight);
        interestManager.onUpdate(player, region);
    }

    private Vector2<Double, Double> securePosition(Vector2<Double, Double> position) {
        return new Vector2<>(
            Math.min(Math.max(0, position.getX()), dimensions.getX()),
            Math.min(Math.max(0, position.getY()), dimensions.getY())
        );
    }

    private Vector2<Integer, Integer> getRegionByPosition(Vector2<Double, Double> position) {
        position = securePosition(position);

        int regionX = (int) (position.getX() / regionSize.getX());
        int regionY = (int) (position.getY() / regionSize.getY());

        return new Vector2<>(regionX, regionY);
    }

    public Player getPlayer(Session session) {
        Player player = session.getPlayer();

        if (player == null) {
            player = new Player(session);

            player.setPosition(new Vector2<>(500.0, 500.0));
            player.setInterestAreaSize(new Vector2<>(10.0, 10.0));

            players.add(player);
            session.setPlayer(player);
        }

        return player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void closeSession(SelectionKey sessionKey) {
        Optional<Player> playerOptional = players.stream()
            .filter(player -> sessionKey.equals(player.getSession().getAttribute("key", null)))
            .findAny();

        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            interestManager.onUpdate(player, null);
            players.remove(player);
        }
    }
}
