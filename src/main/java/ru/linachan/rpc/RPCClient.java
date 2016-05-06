package ru.linachan.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RPCClient implements Runnable {

    private Connection connection;
    private Channel channel;
    private String requestQueueName;
    private String replyQueueName;
    private QueueingConsumer consumer;
    private Boolean isRunning = true;

    private Map<String, RPCCallback> callbackMap = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(RPCClient.class);

    public RPCClient(Connection rpcConnection, String rpcQueue) throws IOException {
        connection = rpcConnection;
        channel = connection.createChannel();

        requestQueueName = rpcQueue;
        replyQueueName = channel.queueDeclare().getQueue();

        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    public void call(String message, RPCCallback callback) throws IOException, InterruptedException {
        String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
            .Builder()
            .correlationId(corrId)
            .replyTo(replyQueueName)
            .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes());

        callbackMap.put(corrId, callback);
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                if (callbackMap.containsKey(delivery.getProperties().getCorrelationId())) {
                    callbackMap.get(delivery.getProperties().getCorrelationId()).callback(new String(delivery.getBody()));
                }
            } catch (InterruptedException e) {
                logger.error("Unable to handle RPC message: {}", e.getMessage());
            }
        }
    }

    public void shutdown() throws IOException {
        connection.close();
        isRunning = false;
    }
}
