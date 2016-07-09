package ru.linachan.mmo.world.interest;

import ru.linachan.mmo.utils.DataWriter;
import ru.linachan.yggdrasil.common.vector.Vector2;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InterestArea {

    private Vector2<Integer, Integer> topLeft;
    private Vector2<Integer, Integer> bottomRight;

    private List<InterestObject> interestTargets = new CopyOnWriteArrayList<>();
    private DataWriter dataWriter;

    public InterestArea(DataWriter playerWriter) {
        dataWriter = playerWriter;
    }

    public void setArea(Vector2<Integer, Integer> topLeftCorner, Vector2<Integer, Integer> bottomRightCorner) {
        topLeft = topLeftCorner;
        bottomRight = bottomRightCorner;
    }

    public void onEvent(InterestEvent event) {
        InterestObject interestTarget = event.getTarget();
        byte[] eventData = event.getData().array();

        ByteBuffer eventBuffer = ByteBuffer.allocate(0);
        switch (event.getType()) {
            case ENTER:
                interestTargets.add(event.getTarget());
                eventBuffer = ByteBuffer.allocate(interestTarget.getName().length() + eventData.length + 43);

                eventBuffer.putShort((short) 1);
                eventBuffer.put((byte) 1);

                eventBuffer.putLong(interestTarget.getUUID().getLeastSignificantBits());
                eventBuffer.putLong(interestTarget.getUUID().getMostSignificantBits());

                eventBuffer.putDouble(interestTarget.getPosition().getX());
                eventBuffer.putDouble(interestTarget.getPosition().getY());

                eventBuffer.putInt(interestTarget.getName().length());
                eventBuffer.put(interestTarget.getName().getBytes());

                eventBuffer.putInt(eventData.length);
                eventBuffer.put(eventData);
                break;
            case LEAVE:
                interestTargets.remove(event.getTarget());
                eventBuffer = ByteBuffer.allocate(19);

                eventBuffer.putShort((short) 1);
                eventBuffer.put((byte) 3);

                eventBuffer.putLong(interestTarget.getUUID().getLeastSignificantBits());
                eventBuffer.putLong(interestTarget.getUUID().getMostSignificantBits());
                break;
            case UPDATE:
                eventBuffer = ByteBuffer.allocate(eventData.length + 39);

                eventBuffer.putShort((short) 1);
                eventBuffer.put((byte) 2);

                eventBuffer.putLong(interestTarget.getUUID().getLeastSignificantBits());
                eventBuffer.putLong(interestTarget.getUUID().getMostSignificantBits());

                eventBuffer.putDouble(interestTarget.getPosition().getX());
                eventBuffer.putDouble(interestTarget.getPosition().getY());

                eventBuffer.putInt(eventData.length);
                eventBuffer.put(eventData);
                break;
        }

        try { dataWriter.write(eventBuffer.array()); } catch (InterruptedException ignored) {}
    }

    public boolean isSubscribed(InterestObject interestTarget) {
        return interestTargets.contains(interestTarget);
    }

    public boolean isInterestedIn(Vector2<Integer, Integer> targetRegion) {
        boolean xInside = (topLeft.getX() <= targetRegion.getX()) && (targetRegion.getX() <= bottomRight.getX());
        boolean yInside = (topLeft.getY() <= targetRegion.getY()) && (targetRegion.getY() <= bottomRight.getY());

        return xInside && yInside;
    }

}
