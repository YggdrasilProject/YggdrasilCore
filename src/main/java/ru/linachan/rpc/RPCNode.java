package ru.linachan.rpc;

import java.util.UUID;

public class RPCNode {

    private UUID nodeUUID;
    private Long lastSeen;

    private final Long EXPIRATION_TIME = 60000L;

    public RPCNode(String nodeUUID) {
        this.nodeUUID = UUID.fromString(nodeUUID);
        this.lastSeen = System.currentTimeMillis();
    }

    public RPCNode(UUID nodeUUID) {
        this.nodeUUID = nodeUUID;
        this.lastSeen = System.currentTimeMillis();
    }

    public void update() {
        lastSeen = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastSeen > EXPIRATION_TIME;
    }

    public UUID getNodeUUID() {
        return nodeUUID;
    }

    public Long getLastSeen() {
        return System.currentTimeMillis() - lastSeen;
    }
}
