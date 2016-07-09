package ru.linachan.mmo.world.interest;

import ru.linachan.yggdrasil.common.vector.Vector2;

import java.util.UUID;

public interface InterestObject {

    String getName();
    UUID getUUID();
    Vector2<Double, Double> getPosition();
}
