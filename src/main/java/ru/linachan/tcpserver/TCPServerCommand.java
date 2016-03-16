package ru.linachan.tcpserver;

import ru.linachan.webservice.WebService;
import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.utils.RequestBin;
import ru.linachan.webservice.utils.RequestBinMonitor;
import ru.linachan.yggdrasil.common.console.InterruptHandler;
import ru.linachan.yggdrasil.component.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.*;
import java.util.List;
import java.util.Map;

public class TCPServerCommand extends YggdrasilShellCommand implements InterruptHandler {

    public static String commandName = "server";
    public static String commandDescription = "Manage TCPService instances";

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        if (args.size() > 0) {
            TCPServerPlugin tcpServerPlugin = core.getManager(YggdrasilPluginManager.class).get(TCPServerPlugin.class);
            switch (args.get(0)) {
                case "list":
                    console.writeMap(
                        tcpServerPlugin.listTCPServices(),
                        "port", "serviceClass"
                    );
                    break;
                case "start":
                    String serverClass = kwargs.getOrDefault("serverClass", "WebService");
                    Integer serverPort = Integer.valueOf(kwargs.getOrDefault("port", "9999"));

                    Class<? extends TCPService> serviceClass = core.getManager(TCPServiceManager.class)
                        .getClassByName(serverClass);

                    if (serviceClass != null) {
                        try {
                            TCPService serviceInstance = serviceClass.newInstance();

                            if (serviceClass.equals(WebService.class)) {
                                core.createQueue(WebServiceRequest.class, "requestBin");
                                ((WebService) serviceInstance).addRoute("^/monitor/$", RequestBinMonitor.class);
                                ((WebService) serviceInstance).addRoute("^(.*?)$", RequestBin.class);
                            }

                            tcpServerPlugin.startTCPService(serverPort, serviceInstance);
                        } catch (InstantiationException | IllegalAccessException e) {
                            logger.error("Unable to instantiate TCPService: {}" + e.getMessage());
                            console.writeLine("Unable to start server on port: %d", serverPort);
                            exit(1);
                        }
                    } else {
                        console.writeLine("Unable to find TCPService %s", serverClass);
                        exit(1);
                    }
                    break;
                case "stop":
                    if (args.size() > 1) {
                        args.subList(1, args.size()).stream()
                            .forEach(port -> tcpServerPlugin.stopTCPService(Integer.valueOf(port)));
                    } else {
                        console.writeLine("No port specified");
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

    @Override
    protected void onInterrupt() {}
}
