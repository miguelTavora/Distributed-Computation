package rmqconfigurator;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConf {

    // name given to the exchange to receive messages and type used is fanout
    // fanout sends the message to all queues connected to a exchange
    private final String EXCHANGE_NAME = "messages";
    private final BuiltinExchangeType EXCHANGE_TYPE = BuiltinExchangeType.FANOUT;

    // name of the queue where the consumer will receive the messages
    // second level
    public static final String QUEUE_FOG_NAME = "fog";
    // third level
    private final String QUEUE_CLOUD_NAME = "cloud";

    // name of the binding keys to associate the queues to the exchanges
    // second level
    private final String BINDING_KEY_FOG_NAME = "keyfog";
    // third level
    private final String BINDING_KEY_CLOUD_NAME = "keycloud";




    private Connection connection = null;
    private Channel channel = null;

    public RabbitMQConf(String ipAddress) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ipAddress); factory.setPort(5672);
        System.out.println("Current IP: "+ipAddress);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            System.out.println("Error creating connection: "+e.getMessage());
        }
    }

    public void createExchangeAndQueues() {
        createExchange();
        // creates two queues one for the third level and one
        // to show the messages on the console
        createQueue(QUEUE_FOG_NAME);
        createQueue(QUEUE_CLOUD_NAME);

        // creates two binds for each queue created, with the fanout method sends to all queue
        bindQueue2Exchange(QUEUE_FOG_NAME, BINDING_KEY_FOG_NAME);
        bindQueue2Exchange(QUEUE_CLOUD_NAME, BINDING_KEY_CLOUD_NAME);
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void closeConnectionAndChannel() {
        try {
            this.connection.close();
            this.channel.close();
        } catch (IOException | TimeoutException e) {
            System.out.println("Error closing connection or channel: "+e.getMessage());
        }
    }

    // when restart a server the exchange will be deleted
    private void createExchange() {
        try {
            // sends the messages which the binding key is the same as the routing key
            // durable true if we are declaring a durable exchange (the exchange will survive a server restart)
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, false);
            System.out.println("Exchange created: "+EXCHANGE_NAME);
        } catch (IOException e) {
            System.out.println("Error creating Exchange: "+e.getMessage());
        }
    }

    private void createQueue(String queueName) {
        // @param durable true if we are declaring a durable queue (the queue will survive a server restart)
        // @param exclusive true if we are declaring an exclusive queue (restricted to this connection)
        // @param autoDelete true if we are declaring an autodelete queue (server will delete it when no longer in use)
        try {
            channel.queueDeclare(queueName, false, false, false, null);
            System.out.println("Queue created: "+queueName);
        } catch (IOException e) {
            System.out.println("Error creating Queue: "+e.getMessage());
        }
    }

    private void bindQueue2Exchange(String queueName, String bindingKey) {
        try {
            channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
            System.out.println("Bind queue("+queueName+") to exchange("+EXCHANGE_NAME+") with key: "+bindingKey);
        } catch (IOException e) {
            System.out.println("Error binding queue to exchange: "+e.getMessage());
        }
    }
}
