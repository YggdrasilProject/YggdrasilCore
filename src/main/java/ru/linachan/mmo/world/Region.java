package ru.linachan.mmo.world;

import ru.linachan.yggdrasil.common.vector.Vector2;

public class Region {

    private Vector2<Integer, Integer> position;

    public Region(Vector2<Integer, Integer> regionPosition) {
        position = regionPosition;
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Region(%s, %s)", position.getX(), position.getY());
    }

    public Vector2<Integer, Integer> getPosition() {
        return position;
    }
}
