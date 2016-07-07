package ru.linachan.yggdrasil.common.nio;

public final class ByteMessageLength implements MessageLength {

    private final int NUM_BYTES = 1;
    private final long MAX_LENGTH = 255;

    @Override
    public int byteLength() {
        return NUM_BYTES;
    }

    @Override
    public long maxLength() {
        return MAX_LENGTH;
    }

    @Override
    public long bytesToLength(byte[] bytes) {
        if (bytes.length!=NUM_BYTES) {
            throw new IllegalStateException("Wrong number of bytes, must be " + NUM_BYTES);
        }
        return (long) (bytes[0] & 0xff);
    }

    @Override
    public byte[] lengthToBytes(long len) {
        if (len<0 || len>MAX_LENGTH) {
            throw new IllegalStateException("Illegal size: less than 0 or greater than " + MAX_LENGTH);
        }
        return new byte[] {(byte) (len & 0xff)};
    }
}
