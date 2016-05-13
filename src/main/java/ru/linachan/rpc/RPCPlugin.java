package ru.linachan.rpc;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class RPCPlugin extends YggdrasilPlugin {

    private ConnectionFactory rpcConnectionFactory;
    private List<Connection> rpcConnectionList;

    private Logger logger = LoggerFactory.getLogger(RPCPlugin.class);

    @Override
    protected void onInit() {
        rpcConnectionFactory = new ConnectionFactory();
        rpcConnectionList = new ArrayList<>();

        rpcConnectionFactory.setHost(core.getConfig().getString("rabbitmq.host", "localhost"));
        rpcConnectionFactory.setPort(core.getConfig().getInt("rabbitmq.port", 5672));
        rpcConnectionFactory.setUsername(core.getConfig().getString("rabbitmq.user", "guest"));
        rpcConnectionFactory.setPassword(core.getConfig().getString("rabbitmq.password", "guest"));
        rpcConnectionFactory.setVirtualHost(core.getConfig().getString("rabbitmq.virtual_host", "/"));
    }

    @Override
    protected void onShutdown() {
        rpcConnectionList.stream()
            .collect(Collectors.toList()).stream()
            .filter(ShutdownNotifier::isOpen)
            .forEach(connection -> {
                try {
                    connection.close();
                } catch (IOException e) {
                    logger.warn("Unable to close RabbitMQ connection: {}", e.getMessage());
                }
                rpcConnectionList.remove(connection);
            });
    }

    public Connection newConnection() throws IOException, TimeoutException {
        Connection rpcConnection = rpcConnectionFactory.newConnection();
        rpcConnectionList.add(rpcConnection);
        return rpcConnection;
    }

    public RPCServer getRPCServer(String rpcQueue, RPCService rpcService) throws IOException, TimeoutException {
        return new RPCServer(newConnection(), rpcQueue, rpcService);
    }

    public RPCClient getRPCClient(String rpcQueue) throws IOException, TimeoutException {
        return new RPCClient(newConnection(), rpcQueue);
    }
}
