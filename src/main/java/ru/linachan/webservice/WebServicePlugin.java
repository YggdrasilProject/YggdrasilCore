package ru.linachan.webservice;

import ru.linachan.tcpserver.TCPServerPlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.helpers.DependsOn;

@DependsOn(TCPServerPlugin.class)
public class WebServicePlugin extends YggdrasilPlugin {

    @Override
    protected void onInit() {

    }

    @Override
    protected void onShutdown() {

    }
}
