package ru.linachan.cluster;

import ru.linachan.rpc.RPCCallback;
import ru.linachan.rpc.RPCClient;
import ru.linachan.rpc.RPCPlugin;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ClusterCommand extends YggdrasilShellCommand implements RPCCallback {

    public static String commandName = "cluster";
    public static String commandDescription = "CLuster management";

    private boolean isWaiting = false;

    private void call(ClusterMessage message) throws IOException {
        try {
            RPCClient clusterClient = core.getManager(YggdrasilPluginManager.class).get(RPCPlugin.class).getRPCClient();
            clusterClient.call(ClusterPlugin.CLUSTER_EXCHANGE, "command", message.toJSON(), this);

            while (isWaiting) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }

            clusterClient.shutdown();
        } catch (TimeoutException | InterruptedException e) {
            console.error("Unable to perform RPC call: {}", e.getMessage());
        }
    }

    @Override
    protected void execute(String command, List<String> args, Map<String, String> kwargs) throws IOException {
        if (kwargs.containsKey("shutdown")) {
            ClusterMessage shutdownRequest = new ClusterMessage();
            shutdownRequest.setData("action", "shutdown");
            call(shutdownRequest);
        } else if (kwargs.containsKey("call")) {
            ClusterMessage callRequest = new ClusterMessage();
            callRequest.setData("action", kwargs.get("call"));

            kwargs.remove("call");
            for (String key: kwargs.keySet()) {
                callRequest.setData(key, kwargs.get(key));
            }

            call(callRequest);
        } else {
            Map<String, String> hostMap = new HashMap<>();

            for (ClusterNode node : core.getManager(YggdrasilPluginManager.class).get(ClusterPlugin.class).getNodes()) {
                hostMap.put(node.getNodeUUID().toString(), String.format("%0,3f", node.getLastSeen() / 1000.0));
            }

            console.writeMap(hostMap, "Node", "Last Seen");
        }
    }

    @Override
    protected void onInterrupt() {

    }

    @Override
    public void callback(String message) {
        try {
            console.writeLine(message);
        } catch (IOException e) {
            logger.error("Unable to handle response response: {}", e.getMessage());
        }
        isWaiting = false;
    }
}
