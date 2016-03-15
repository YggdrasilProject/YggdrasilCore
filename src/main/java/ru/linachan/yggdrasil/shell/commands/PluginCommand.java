package ru.linachan.yggdrasil.shell.commands;

import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.component.YggdrasilPlugin;
import ru.linachan.yggdrasil.component.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PluginCommand extends YggdrasilShellCommand {

    public static String commandName = "plugin";
    public static String commandDescription = "Manage installed plugins";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        if (args.size() > 0) {
            YggdrasilPluginManager pluginManager = core.getManager(YggdrasilPluginManager.class);

            switch (args.get(0)) {
                case "list":
                    Map<Class<? extends YggdrasilPlugin>, Boolean> plugins = pluginManager.list();
                    for (Class<? extends YggdrasilPlugin> plugin: plugins.keySet()) {
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
                    break;
                case "enable":
                    if (args.size() > 1) {
                        args.subList(1, args.size()).stream()
                            .forEach(pluginName -> pluginManager.list().keySet().stream()
                                .filter(plugin -> plugin.getSimpleName().equals(pluginName))
                                .forEach(pluginManager::enable)
                            );

                    } else {
                        console.writeLine("No plugins specified");
                        exit(1);
                    }
                    break;
                case "disable":
                    if (args.size() > 1) {
                        args.subList(1, args.size()).stream()
                            .forEach(pluginName -> pluginManager.list().keySet().stream()
                                .filter(plugin -> plugin.getSimpleName().equals(pluginName))
                                .forEach(pluginManager::disable)
                            );

                    } else {
                        console.writeLine("No plugins specified");
                        exit(1);
                    }
                    break;
                default:
                    console.writeLine("Unknown action: %s", args.get(0));
                    exit(1);
                    break;
            }
        } else {
            exit(1);
        }
    }
}
