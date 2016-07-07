package ru.linachan.yggdrasil.common.nio.server;

import ru.linachan.yggdrasil.common.nio.MessageLength;

public class ServerBuilder {

    private Server server;

    public ServerBuilder(int port) {
        server = new Server(port);
    }

    public ServerBuilder(int port, MessageLength messageLength, int defaultBufferSize) {
        server = new Server(port, messageLength, defaultBufferSize);
    }

    public ServerBuilder setHandler(ServerHandler handler) {
        server.setHandler(handler);
        return this;
    }

    public ServerBuilder registerMiddleware(ServerMiddleware middleware) {
        server.registerMiddleware(middleware);
        return this;
    }

    public Server build() {
        return server;
    }
}
