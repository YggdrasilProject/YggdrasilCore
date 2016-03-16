package ru.linachan.webservice;

import ru.linachan.tcpserver.TCPServerPlugin;
import ru.linachan.yggdrasil.component.YggdrasilPlugin;

public class WebServicePlugin extends YggdrasilPlugin {

    @Override
    protected void setUpDependencies() {
        dependsOn(TCPServerPlugin.class);
    }

    @Override
    protected void onInit() {

    }

    @Override
    protected void onShutdown() {

    }
}
