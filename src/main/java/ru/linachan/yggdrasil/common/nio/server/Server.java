package ru.linachan.yggdrasil.common.nio.server;

import com.google.common.collect.Lists;
import ru.linachan.yggdrasil.common.nio.AbstractServer;
import ru.linachan.yggdrasil.common.nio.MessageLength;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

public class Server extends AbstractServer {

    private List<ServerMiddleware> middlewareList = new ArrayList<>();
    private ServerHandler handler;

    protected Server(int port) {
        super(port);
    }

    protected Server(int port, MessageLength messageLength, int defaultBufferSize) {
        super(port, messageLength, defaultBufferSize);
    }

    @Override
    protected void messageReceived(ByteBuffer message, SelectionKey key) {
        final ByteBuffer[] input = { message };
        middlewareList.stream().forEach(middleware -> input[0] = middleware.decode(input[0], key));

        final ByteBuffer[] output = { handler.onMessageReceived(input[0], key) };

        if (output[0] != null) {
            write(key, output[0].array());
        }
    }

    @Override
    public void write(SelectionKey key, byte[] data) {
        final ByteBuffer[] output = { ByteBuffer.wrap(data) };

        Lists.reverse(middlewareList).stream().forEach(middleware -> output[0] = middleware.encode(output[0], key));

        super.write(key, output[0].array());
    }

    @Override
    protected void connection(SelectionKey key) {
        handler.onConnect(key);
    }

    @Override
    protected void disconnected(SelectionKey key) {
        handler.onDisconnect(key);
    }

    @Override
    protected void started(boolean alreadyStarted) {
        handler.onStart(alreadyStarted);
    }

    @Override
    protected void stopped() {
        handler.onStop();
    }

    public void registerMiddleware(ServerMiddleware middleware) {
        middlewareList.add(middleware);
    }

    public void setHandler(ServerHandler serverHandler) {
        handler = serverHandler;
    }
}
