package com.example.wheele_commander.backend;

public class Header {
    public static final int HEADER_SIZE = 4;

    private final boolean heartbeatAck;
    private final boolean heartbeat;
    private final int messageType;
    private final int dataLength;

    private Header(boolean heartbeatAck, boolean heartbeat, int messageType, int dataLength) {
        this.heartbeatAck = heartbeatAck;
        this.heartbeat = heartbeat;
        this.messageType = messageType;
        this.dataLength = dataLength;
    }

    public static Header fromBytes(byte[] headerBytes) {
        boolean heartbeatAck = headerBytes[0] != 0;
        boolean heartbeat = headerBytes[1] != 0;
        int messageType = Byte.toUnsignedInt(headerBytes[2]);
        int dataLength = Byte.toUnsignedInt(headerBytes[3]);
        return new Header(heartbeatAck, heartbeat, messageType, dataLength);
    }

    public boolean isHeartbeatAck() {
        return heartbeatAck;
    }

    public boolean isHeartbeat() {
        return heartbeat;
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getMessageType() {
        return messageType;
    }
}
