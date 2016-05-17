package ru.linachan.test;

import ru.linachan.cheat.CheatPlugin;
import ru.linachan.tcpserver.TCPServerPlugin;
import ru.linachan.webservice.WebService;
import ru.linachan.webservice.WebServicePlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.plugin.helpers.AutoStart;
import ru.linachan.yggdrasil.plugin.helpers.DependsOn;

@AutoStart
@DependsOn(CheatPlugin.class)
@DependsOn(WebServicePlugin.class)
public class TestPlugin extends YggdrasilPlugin {

    @Override
    protected void onInit() {
        core.getManager(YggdrasilPluginManager.class).get(TCPServerPlugin.class).startTCPService(8888, new WebService());
    }

    @Override
    protected void onShutdown() {
    }
}
