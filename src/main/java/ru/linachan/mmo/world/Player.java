package ru.linachan.mmo.world;

import org.slf4j.LoggerFactory;
import ru.linachan.mmo.auth.user.Session;
import ru.linachan.yggdrasil.common.vector.Vector2;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player {
    private Vector2<Double, Double> position;
    private Vector2<Double, Double> areaOfInterest;

    private UUID playerId = UUID.randomUUID();
    private Session session;

    private List<Region> subscribedRegions  = new CopyOnWriteArrayList<>();

    public Vector2<Double, Double> getPosition() {
        return position;
    }

    public Vector2<Double, Double> getAreaOfInterest() {
        return areaOfInterest;
    }

    public Session getSession() {
        return session;
    }

    public void setPosition(Vector2<Double, Double> position) {
        this.position = position;
    }

    public void setAreaOfInterest(Vector2<Double, Double> areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void subscribe(Region region) {
        if (!subscribedRegions.contains(region)) {
            subscribedRegions.add(region);
            region.subscribe(this);
        }
    }

    public void unsubscribe() {
        subscribedRegions.stream().forEach(region -> region.unsubscribe(this));
        subscribedRegions.clear();
    }

    public void sendUpdate(Vector2<Integer, Integer> region, byte[] data) {
        LoggerFactory.getLogger(Player.class).info("{}: {}", region, new String(data));
    }
}
