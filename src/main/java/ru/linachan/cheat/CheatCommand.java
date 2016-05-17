package ru.linachan.cheat;

import ru.linachan.cheat.utils.CheatUtils;
import ru.linachan.yggdrasil.common.console.InterruptHandler;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CheatCommand extends YggdrasilShellCommand {

    public static String commandName = "cheat";
    public static String commandDescription = "Manipulate process memory";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        CheatPlugin cheatEngine = core.getManager(YggdrasilPluginManager.class).get(CheatPlugin.class);

        switch(args.get(0)) {
            case "ps":
                Map<Integer, String> processList;

                if (kwargs.containsKey("name")) {
                    processList = CheatUtils.processListToMap(cheatEngine.getProcessesByName(kwargs.get("name")));
                } else {
                    processList = CheatUtils.processListToMap(cheatEngine.getAllProcesses());
                }

                console.writeMap(processList, "PID", "Process Name");
                break;
            case "attach":
                if (kwargs.containsKey("pid")) {
                    Optional<CheatProcess> processOptional = cheatEngine.getAllProcesses().stream()
                        .filter(process -> String.valueOf(process.getProcessID()).equals(kwargs.get("pid")))
                        .findFirst();

                    if (processOptional.isPresent()) {
                        if (cheatEngine.isAttached()) {
                            if (!console.readYesNo(String.format("CheatPlugin is already attached to '%s'. Do you want to detach it?", cheatEngine.getAttachedProcess().getProcessName()))) {
                                console.writeLine("Canceled");
                                break;
                            }
                        }

                        CheatProcess process = processOptional.get();
                        cheatEngine.attachProcess(process);

                        console.writeLine("Attached to '%s' at PID%d", process.getProcessName(), process.getProcessID());
                    }
                } else if (kwargs.containsKey("name")) {
                    List<CheatProcess> processes = cheatEngine.getProcessesByName(kwargs.get("name"));

                    if (processes.size() > 1) {
                        console.writeLine("Multiple processes found. Provide --pid instead.");
                        console.writeMap(CheatUtils.processListToMap(processes), "PID", "Process Name");
                    } else if (processes.size() == 1) {
                        if (cheatEngine.isAttached()) {
                            if (!console.readYesNo(String.format("CheatPlugin is already attached to '%s'. Do you want to detach it?", cheatEngine.getAttachedProcess().getProcessName()))) {
                                console.writeLine("Canceled");
                                break;
                            }
                        }

                        CheatProcess process = processes.get(0);
                        cheatEngine.attachProcess(process);

                        console.writeLine("Attached to '%s' at PID%d", process.getProcessName(), process.getProcessID());
                    }
                } else {
                    if (cheatEngine.isAttached()) {
                        console.writeLine(
                            "CheatPlugin is attached to '%s' at PID%d",
                            cheatEngine.getAttachedProcess().getProcessName(),
                            cheatEngine.getAttachedProcess().getProcessID()
                        );
                    } else {
                        console.writeLine("No attached process");
                    }
                }
                break;
            case "detach":
                if (cheatEngine.isAttached()) {
                    if (kwargs.containsKey("kill")) {
                        console.writeLine("Killing process PID%d", cheatEngine.getAttachedProcess().getProcessID());
                        cheatEngine.getAttachedProcess().kill();
                    }

                    cheatEngine.detachProcess();
                    console.writeLine("Detached");
                } else {
                    console.writeLine("No process attached");
                }
                break;
            case "dump":
                if (cheatEngine.isAttached()) {
                    if (kwargs.containsKey("address")&&kwargs.containsKey("length")) {
                        long address = Long.decode(kwargs.get("address"));
                        int length = Integer.parseInt(kwargs.get("length"));

                        for (String line: cheatEngine.getAttachedProcess().dumpMemory(address, length).split("\r\n")) {
                            console.writeLine(line);
                        }
                    }
                } else {
                    console.writeLine("No process attached");
                }
                break;
            case "search":
                if (cheatEngine.isAttached()) {
                    String value = kwargs.getOrDefault("value", null);
                    Integer bytesPerChar = Integer.parseInt(kwargs.getOrDefault("bytes", "2"));
                    String type = kwargs.getOrDefault("type", "string");

                    switch (type) {
                        case "string":
                            if (value != null) {
                                List<Long> searchResults = cheatEngine.getAttachedProcess().findString(value, bytesPerChar);
                                Map<String, String> searchMap = new HashMap<>();

                                searchResults.stream()
                                    .forEach(result -> searchMap.put(
                                        String.format("%08X", result), cheatEngine.getAttachedProcess().readString(result, bytesPerChar))
                                    );

                                console.writeMap(searchMap, "Address", "Value");
                            } else {
                                console.writeLine("No pattern provided");
                            }
                            break;
                        default:
                            console.writeLine("Unknown type: '%s'", type);
                            break;
                    }
                } else {
                    console.writeLine("No process attached");
                }
                break;
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
