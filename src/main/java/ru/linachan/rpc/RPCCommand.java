package ru.linachan.rpc;

import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@ShellCommand(command = "rpc", description = "RPC cluster management")
public class RPCCommand extends YggdrasilShellCommand implements RPCCallback {

    @Override
    protected void init() throws IOException {}

    private void rpcCall(RPCMessage message) throws IOException {
        try {
            RPCClient rpcClient = core.getManager(YggdrasilPluginManager.class).get(RPCPlugin.class).getRPCClient();
            rpcClient.call(RPCPlugin.RPC_EXCHANGE, "command", message.toJSON(), this);

            Thread.sleep(2000);

            rpcClient.shutdown();
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
        List<Map<String, String>> hosts = new ArrayList<>();
        for (RPCNode node : core.getManager(YggdrasilPluginManager.class).get(RPCPlugin.class).listNodes()) {
            Map<String, String> host = new HashMap<>();

            host.put("uuid", node.getNodeUUID().toString());
            host.put("type", node.getNodeType());
            host.put("last_seen", String.format("%0,3f", node.getLastSeen() / 1000.0));

            host.put("os_name", (String) node.getNodeInfo("osName", null));
            host.put("os_arch", (String) node.getNodeInfo("osArch", null));
            host.put("os_version", (String) node.getNodeInfo("osVersion", null));

            hosts.add(host);
        }

        List<String> hostFields = new ArrayList<>();

        hostFields.add("uuid");
        hostFields.add("type");
        hostFields.add("last_seen");
        hostFields.add("os_name");
        hostFields.add("os_arch");
        hostFields.add("os_version");

        console.writeTable(hosts, hostFields);
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
    }
}
