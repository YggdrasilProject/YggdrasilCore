package ru.linachan.cluster;

import ru.linachan.rpc.*;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@ShellCommand(command = "cluster", description = "Cluster management")
public class ClusterCommand extends YggdrasilShellCommand implements RPCCallback {

    private boolean isWaiting = false;

    @Override
    protected void init() throws IOException {}

    private void rpcCall(RPCMessage message) throws IOException {
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
            console.error("Unable to perform RPC rpcCall: {}", e.getMessage());
        }
    }

    @CommandAction("Shutdown entire cluster")
    public void shutdown() throws IOException {
        RPCMessage shutdownRequest = new RPCMessage();
        shutdownRequest.setData("action", "shutdown");
        rpcCall(shutdownRequest);
    }

    @CommandAction("Perform custom RPC call")
    public void call() throws IOException {
        RPCMessage callRequest = new RPCMessage();

        for (String key : kwargs.keySet()) {
            callRequest.setData(key, kwargs.get(key));
        }

        rpcCall(callRequest);
    }

    @CommandAction("Show cluster status")
    public void status() throws IOException {
        Map<String, String> hostMap = new HashMap<>();

        for (RPCNode node : core.getManager(YggdrasilPluginManager.class).get(ClusterPlugin.class).getNodes()) {
            hostMap.put(node.getNodeUUID().toString(), String.format("%0,3f", node.getLastSeen() / 1000.0));
        }

        console.writeMap(hostMap, "Node", "Last Seen");
    }

    @Override
    protected void onInterrupt() {

    }

    @Override
    public void callback(RPCMessage message) {
        try {
            console.writeLine(message.toJSON());
        } catch (IOException e) {
            logger.error("Unable to handle response response: {}", e.getMessage());
        }
        isWaiting = false;
    }
}
