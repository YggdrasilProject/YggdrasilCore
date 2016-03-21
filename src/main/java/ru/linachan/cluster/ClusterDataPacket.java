package ru.linachan.cluster;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

public class ClusterDataPacket {

    private UUID sourceNode;
    private UUID targetNode = null;

    private byte[] opCode;
    private byte[] rawData;

    private final Integer MAGIC = 0x59436c73;

    public ClusterDataPacket(ByteBuffer channelBuffer) {
        Integer magic = channelBuffer.getInt();

        if (!Objects.equals(magic, MAGIC)) {
            throw new IllegalArgumentException("Invalid ClusterDataPacket headers");
        }

        sourceNode = new UUID(channelBuffer.getLong(), channelBuffer.getLong());
        targetNode = new UUID(channelBuffer.getLong(), channelBuffer.getLong());

        targetNode = (targetNode == new UUID(0L, 0L)) ? null : targetNode;

        opCode = new byte[4];
        channelBuffer.get(opCode);

        rawData = new byte[channelBuffer.getInt()];
        channelBuffer.get(rawData);
    }

    public ClusterDataPacket(byte opCodeAL, byte opCodeAH, byte opCodeBL, byte opCodeBH, byte[] rawData) {
        setOpCode(opCodeAL, opCodeAH, opCodeBL, opCodeBH);
        setRawData(rawData);
    }

    public ClusterDataPacket(int opCodeAL, int opCodeAH, int opCodeBL, int opCodeBH, byte[] rawData) {
        setOpCode(opCodeAL, opCodeAH, opCodeBL, opCodeBH);
        setRawData(rawData);
    }

    public void setOpCode(int opCodeAL, int opCodeAH, int opCodeBL, int opCodeBH) {
        opCode = new byte[] {(byte) opCodeAL, (byte) opCodeAH, (byte) opCodeBL, (byte) opCodeBH};
    }

    public void setOpCode(byte opCodeAL, byte opCodeAH, byte opCodeBL, byte opCodeBH) {
        opCode = new byte[] { opCodeAL, opCodeAH, opCodeBL, opCodeBH };
    }

    public void setRawData(byte[] data) {
        if (data.length > 1176) {
            throw new IllegalArgumentException("ClusterDataPacket maximal data size is 1176 bytes");
        }

        rawData = data.clone();
    }

    public void setSourceNode(UUID sourceNode) {
        this.sourceNode = sourceNode;
    }

    public void setTargetNode(UUID targetNode) {
        this.targetNode = targetNode;
    }

    public UUID getSourceNode() {
        return sourceNode;
    }

    public UUID getTargetNode() {
        return targetNode;
    }

    public byte[] getOpCode() {
        return opCode;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer packetBuffer = ByteBuffer.allocate(324 + rawData.length);

        packetBuffer.putInt(MAGIC);

        packetBuffer.putLong(sourceNode.getMostSignificantBits());
        packetBuffer.putLong(sourceNode.getLeastSignificantBits());

        if (targetNode != null) {
            packetBuffer.putLong(targetNode.getMostSignificantBits());
            packetBuffer.putLong(targetNode.getLeastSignificantBits());
        } else {
            packetBuffer.putLong(0x0L);
            packetBuffer.putLong(0x0L);
        }

        packetBuffer.put(opCode);
        packetBuffer.putInt(rawData.length);
        packetBuffer.put(rawData);

        return packetBuffer;
    }
}
