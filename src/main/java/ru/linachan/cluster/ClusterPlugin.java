package ru.linachan.cluster;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.rpc.*;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.plugin.helpers.AutoStart;
import ru.linachan.yggdrasil.plugin.helpers.DependsOn;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.scheduler.YggdrasilRunnable;
import ru.linachan.yggdrasil.scheduler.YggdrasilTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@DependsOn(RPCPlugin.class)
public class ClusterPlugin extends YggdrasilPlugin implements RPCService, RPCCallback {

    private RPCServer clusterServer;
    private RPCClient clusterClient;
    private UUID nodeUUID = UUID.randomUUID();

    private List<ClusterNode> nodes = new ArrayList<>();

    private Logger logger = LoggerFactory.getLogger(ClusterPlugin.class);

    public static final String CLUSTER_EXCHANGE = "yggdrasilCluster";

    @Override
    protected void onInit() {
        try {
            clusterServer = core.getManager(YggdrasilPluginManager.class).get(RPCPlugin.class).getRPCServer(this);
            clusterClient = core.getManager(YggdrasilPluginManager.class).get(RPCPlugin.class).getRPCClient();

            clusterServer.bind(CLUSTER_EXCHANGE, "discover");
            clusterServer.bind(CLUSTER_EXCHANGE, "command");
            clusterServer.bind(CLUSTER_EXCHANGE, nodeUUID.toString());
        } catch (IOException | TimeoutException e) {
            logger.error("Unable to initialize cluster: {}", e.getMessage());
        }

        clusterServer.start();
        clusterClient.start();

        core.getScheduler().scheduleTask(new YggdrasilTask("clusterMonitor", new YggdrasilRunnable(core) {
            @Override
            public void run() {
                try {
                    ClusterMessage discoveryRequest = new ClusterMessage();
                    discoveryRequest.setData("action", "discover");
                    discoveryRequest.setData("nodeUUID", nodeUUID.toString());
                    clusterClient.call(CLUSTER_EXCHANGE, "discover", discoveryRequest.toJSON(), core.getManager(
                        YggdrasilPluginManager.class
                    ).get(ClusterPlugin.class));
                } catch (IOException | InterruptedException e) {
                    logger.error("Unable to perform RPC rpcCall: {}", e.getMessage());
                }
            }

            @Override
            public void onCancel() {}
        }, 1, 15, TimeUnit.SECONDS));

        core.getScheduler().scheduleTask(new YggdrasilTask("clusterHeartbeat", new YggdrasilRunnable(core) {
            @Override
            public void run() {
                nodes.stream()
                    .filter(ClusterNode::isExpired)
                    .collect(Collectors.toList()).stream()
                    .forEach(node -> {
                        nodes.remove(node);
                        logger.info("Node disconnected: {}", node.getNodeUUID().toString());
                    });
            }

            @Override
            public void onCancel() {}
        }, 20, 10, TimeUnit.SECONDS));
    }

    @Override
    protected void onShutdown() {
        try {
            clusterServer.shutdown();
            clusterClient.shutdown();
        } catch (IOException e) {
            logger.error("Unable to shutdown cluster: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ClusterMessage handleRequest(ClusterMessage request) {
        ClusterMessage response = new ClusterMessage();
        if (request.getData().getOrDefault("node", nodeUUID.toString()).equals(nodeUUID.toString())) {
            String action = (String) request.getData().getOrDefault("action", "");
            switch (action) {
                case "discover":
                    response.setData("nodeUUID", nodeUUID.toString());
                    break;
                case "shutdown":
                    core.shutdown();
                    break;
                default:
                    response.setData("status", "error");
                    response.setData("errorType", "UnknownAction");
                    response.setData("errorMsg", String.format("Unknown action: '%s'", action));
                    break;
            }
        } else {
            response.setData("status", "ignored");
        }
        return response;
    }

    @Override
    public String dispatch(String message) {
        try {
            ClusterMessage request = new ClusterMessage(message);
            ClusterMessage response = handleRequest(request);
            return ((response != null) ? response : new ClusterMessage()).toJSON();
        } catch (ParseException e) {
            logger.error("Unable to parse RPC request: {}", e.getMessage());
            return new ClusterMessage(e).toJSON();
        }
    }

    @Override
    public void callback(String message) {
        try {
            ClusterMessage callbackData = new ClusterMessage(message);
            String nodeUUID = (String) callbackData.getData("nodeUUID", null);
            if (nodeUUID != null) {
                Optional<ClusterNode> clusterNodeOptional = nodes.stream()
                    .filter(node -> node.getNodeUUID().equals(UUID.fromString(nodeUUID)))
                    .findFirst();

                if (clusterNodeOptional.isPresent()) {
                    clusterNodeOptional.get().update();
                } else {
                    logger.info("New node discovered: {}", nodeUUID);
                    nodes.add(new ClusterNode(nodeUUID));
                }
            }
        } catch (ParseException e) {
            logger.error("Unable to parse RPC request: {}", e.getMessage());
        }
    }

    public List<ClusterNode> getNodes() {
        return nodes;
    }
}
