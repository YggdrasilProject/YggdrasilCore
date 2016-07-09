package ru.linachan.mmo.world;

import ru.linachan.mmo.auth.user.Session;
import ru.linachan.mmo.world.interest.InterestArea;
import ru.linachan.mmo.world.interest.InterestObject;
import ru.linachan.yggdrasil.common.vector.Vector2;

import java.util.UUID;

public class Player implements InterestObject {
    private Vector2<Double, Double> position;
    private Vector2<Double, Double> interestAreaSize;

    private UUID playerId = UUID.randomUUID();
    private Session session;

    private InterestArea interestArea;

    public Player(Session userSession) {
        session = userSession;
        interestArea = new InterestArea(session.getWriter());
    }

    @Override
    public Vector2<Double, Double> getPosition() {
        return position;
    }

    public Vector2<Double, Double> getInterestAreaSize() {
        return interestAreaSize;
    }

    public Session getSession() {
        return session;
    }

    public void setPosition(Vector2<Double, Double> position) {
        this.position = position;
    }

    public void setInterestAreaSize(Vector2<Double, Double> interestAreaSize) {
        this.interestAreaSize = interestAreaSize;
    }

    @Override
    public UUID getUUID() {
        return playerId;
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }

    @Override
    public boolean equals(Object target) {
        if (target == null) return false;
        if (target == this) return true;
        if (!(target instanceof Player)) return false;

        UUID targetId = ((Player) target).getUUID();
        return playerId.equals(targetId);
    }

    public InterestArea getInterestArea() {
        return interestArea;
    }

    @Override
    public String getName() {
        return session.getUser().getLogin();
    }
}
