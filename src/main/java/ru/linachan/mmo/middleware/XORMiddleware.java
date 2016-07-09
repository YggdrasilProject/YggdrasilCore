package ru.linachan.mmo.middleware;

import ru.linachan.yggdrasil.common.nio.server.ServerMiddleware;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Random;

public class XORMiddleware implements ServerMiddleware {

    private static Random random = new Random();

    private final byte[] BIN_KEY = new byte[] { 0b01010101 };

    private static byte[] xor(byte[] data, byte[] key) {
        ByteBuffer xorBytes = ByteBuffer.allocate(data.length);
        for (int i = 0; i < data.length; i++) {
            xorBytes.put((byte) (0xff & (data[i] ^ key[i % key.length])));
        }

        return xorBytes.array();
    }

    @Override
    public ByteBuffer decode(ByteBuffer inputBuffer, SelectionKey key) {
        byte[] xorKey = new byte[8];
        inputBuffer.get(xorKey);
        xorKey = xor(xorKey, BIN_KEY);

        byte[] xorBytes = new byte[inputBuffer.remaining()];
        inputBuffer.get(xorBytes);

        return ByteBuffer.wrap(xor(xorBytes, xorKey));
    }

    @Override
    public ByteBuffer encode(ByteBuffer outputBuffer, SelectionKey key) {
        byte[] xorKey = new byte[8];
        random.nextBytes(xorKey);

        byte[] xorBytes = outputBuffer.array();

        ByteBuffer dataBuffer = ByteBuffer.allocate(xorBytes.length + 8);

        dataBuffer.put(xor(xorKey, BIN_KEY));
        dataBuffer.put(xor(xorBytes, xorKey));

        dataBuffer.rewind();

        return dataBuffer;
    }
}
