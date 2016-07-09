package ru.linachan.mmo.middleware;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.nio.server.ServerMiddleware;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class LogMiddleware implements ServerMiddleware {

    private static Logger logger = LoggerFactory.getLogger(LogMiddleware.class);

    @Override
    public ByteBuffer decode(ByteBuffer inputBuffer, SelectionKey key) {
        logger.info("IN: {}", Hex.toHexString(inputBuffer.array()));
        return inputBuffer;
    }

    @Override
    public ByteBuffer encode(ByteBuffer outputBuffer, SelectionKey key) {
        logger.info("OUT: {}", Hex.toHexString(outputBuffer.array()));
        return outputBuffer;
    }
}
