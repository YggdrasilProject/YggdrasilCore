package ru.linachan.test;

import ru.linachan.cheat.CheatPlugin;
import ru.linachan.tcpserver.TCPServerPlugin;
import ru.linachan.webservice.WebService;
import ru.linachan.webservice.WebServicePlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.plugin.helpers.AutoStart;
import ru.linachan.yggdrasil.plugin.helpers.DependsOn;

@DependsOn(CheatPlugin.class)
public class TestPlugin extends YggdrasilPlugin {

    @Override
    protected void onInit() {

    }

    @Override
    protected void onShutdown() {
    }
}
