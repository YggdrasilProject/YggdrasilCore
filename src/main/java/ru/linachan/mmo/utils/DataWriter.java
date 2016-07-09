package ru.linachan.mmo.utils;

import ru.linachan.mmo.MMOPlugin;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.nio.server.Server;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;

import java.nio.channels.SelectionKey;

public class DataWriter {

    private Server server;
    private SelectionKey clientKey;

    public DataWriter(YggdrasilCore yggdrasilCore, SelectionKey sessionKey) {
        server = yggdrasilCore.getManager(YggdrasilPluginManager.class).get(MMOPlugin.class).getWorldServer();
        clientKey = sessionKey;
    }

    public void write(byte[] data) {
        server.write(clientKey, data);
    }

}
