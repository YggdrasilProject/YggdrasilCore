package ru.linachan.yggdrasil.shell.commands;

import com.google.common.base.Joiner;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ShellCommand(command = "plugin", description = "Manage installed plugins")
public class PluginCommand extends YggdrasilShellCommand {

    private YggdrasilPluginManager pluginManager;

    @Override
    protected void init() throws IOException {
        pluginManager = core.getManager(YggdrasilPluginManager.class);
    }

    @CommandAction("List available plugins")
    public void list() throws IOException {
        Map<Class<? extends YggdrasilPlugin>, Boolean> plugins = pluginManager.list();
        List<Map<String, String>> pluginsInfo = new ArrayList<>();

        for (Class<? extends YggdrasilPlugin> plugin : plugins.keySet()) {
            boolean isEnabled = plugins.get(plugin);

            Map<String, String> pluginInfo = new HashMap<>();

            pluginInfo.put("name", pluginManager.getPluginInfo(plugin).name());
            pluginInfo.put("enabled", String.valueOf(isEnabled));
            pluginInfo.put("description", pluginManager.getPluginInfo(plugin).description());
            pluginInfo.put("dependencies", Joiner.on(", ").join(pluginManager.getPluginDependencies(plugin)));

            pluginsInfo.add(pluginInfo);
        }

        List<String> pluginsFields = new ArrayList<>();
        pluginsFields.add("name");
        pluginsFields.add("enabled");
        pluginsFields.add("description");
        pluginsFields.add("dependencies");

        console.writeTable(pluginsInfo, pluginsFields);
    }

    @CommandAction("Enable given plugins")
    public void enable() throws IOException {
        if (args.size() > 0) {
            args.stream()
                .forEach(pluginName -> pluginManager.list().keySet().stream()
                    .filter(plugin -> pluginManager.getPluginInfo(plugin).name().equals(pluginName))
                    .forEach(pluginManager::enable)
                );
        } else {
            console.writeLine("No plugins specified");
            exit(1);
        }
    }

    @CommandAction("Disable given plugins")
    public void disable() throws IOException {
        if (args.size() > 0) {
            args.stream()
                .forEach(pluginName -> pluginManager.list().keySet().stream()
                    .filter(plugin -> pluginManager.getPluginInfo(plugin).name().equals(pluginName))
                    .forEach(pluginManager::disable)
                );
        } else {
            console.writeLine("No plugins specified");
            exit(1);
        }
    }

    @Override
    protected void onInterrupt() {}
}
