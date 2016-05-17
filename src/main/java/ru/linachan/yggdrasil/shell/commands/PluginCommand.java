package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
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
        for (Class<? extends YggdrasilPlugin> plugin : plugins.keySet()) {
            boolean isEnabled = plugins.get(plugin);

            console.writeLine(" [%s] %s",
                isEnabled ? console.format(
                        "+", ConsoleColor.GREEN, null, null, true
                ) : console.format(
                        "-", ConsoleColor.RED, null, null, true
                ),
                plugin.getSimpleName()
            );
        }
    }

    @CommandAction("Enable given plugins")
    public void enable() throws IOException {
        if (args.size() > 0) {
            args.stream()
                .forEach(pluginName -> pluginManager.list().keySet().stream()
                    .filter(plugin -> plugin.getSimpleName().equals(pluginName))
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
                    .filter(plugin -> plugin.getSimpleName().equals(pluginName))
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
