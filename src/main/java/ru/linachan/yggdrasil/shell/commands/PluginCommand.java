package ru.linachan.yggdrasil.shell.commands;

import com.google.common.base.Joiner;
import ru.linachan.yggdrasil.common.console.tables.Table;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
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
        Map<Class<? extends YggdrasilPlugin>, Boolean> pluginList = pluginManager.list();
        Table plugins = new Table("name", "enabled", "description", "dependencies");

        for (Class<? extends YggdrasilPlugin> plugin : pluginList.keySet()) {
            boolean isEnabled = pluginList.get(plugin);

            plugins.addRow(
                pluginManager.getPluginInfo(plugin).name(),
                String.valueOf(isEnabled),
                pluginManager.getPluginInfo(plugin).description(),
                Joiner.on(", ").join(pluginManager.getPluginDependencies(plugin))
            );
        }

        console.writeTable(plugins);
    }

    @CommandAction("Enable given plugins")
    public void enable() throws IOException {
        if (args.size() > 0) {
            args.forEach(pluginName -> pluginManager.list().keySet().stream()
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
            args.forEach(pluginName -> pluginManager.list().keySet().stream()
                    .filter(plugin -> pluginManager.getPluginInfo(plugin).name().equals(pluginName))
                    .forEach(pluginManager::disable)
            );
        } else {
            console.writeLine("No plugins specified");
            exit(1);
        }
    }

    @CommandAction("Restart given plugins")
    public void restart() throws IOException {
        if (args.size() > 0) {
            args.forEach(pluginName -> pluginManager.list().keySet().stream()
                    .filter(plugin -> pluginManager.getPluginInfo(plugin).name().equals(pluginName))
                    .forEach(plugin -> {
                        pluginManager.disable(plugin);
                        pluginManager.enable(plugin);
                    })
            );
        } else {
            console.writeLine("No plugins specified");
            exit(1);
        }
    }

    @Override
    protected void onInterrupt() {}
}
