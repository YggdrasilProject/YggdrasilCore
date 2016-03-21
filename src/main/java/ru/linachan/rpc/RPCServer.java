package ru.linachan.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RPCServer implements Runnable {

    private Connection connection;
    private Channel channel;
    private QueueingConsumer consumer;

    private RPCService service;

    private Thread serverThread;
    private boolean isRunning = true;

    private static Logger logger = LoggerFactory.getLogger(RPCServer.class);

    public RPCServer(Connection rpcConnection, String rpcQueue, RPCService rpcService) throws IOException {
        connection = rpcConnection;
        channel = connection.createChannel();

        service = rpcService;

        channel.queueDeclare(rpcQueue, false, false, false, null);

        channel.basicQos(1);

        consumer = new QueueingConsumer(channel);
        channel.basicConsume(rpcQueue, false, consumer);
    }

    public void start() {
        serverThread = new Thread(this);
        serverThread.start();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                AMQP.BasicProperties props = delivery.getProperties();
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(props.getCorrelationId())
                    .build();

                String message = new String(delivery.getBody());

                String response = service.dispatch(message);

                channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (IOException e) {
                logger.error("Unable to process RPC call: {}", e.getMessage());
            } catch (InterruptedException ignored) {}
        }
    }

    public void shutdown() throws IOException {
        isRunning = false;
        serverThread.interrupt();
        connection.close();
    }
}
