package mqconsumer;

import com.rabbitmq.client.*;
import spreadevent.GroupMember;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConsumer {

    private final String QUEUE_CLOUD_NAME = "cloud";
    private Connection connection;
    private Channel channel;
    // used to send the message received to the spread
    private GroupMember groupMember;
    private String groupName;
    private  int minValue;

    public RabbitMQConsumer(String ipAddress, GroupMember groupMember, String groupName, int minValue) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ipAddress); factory.setPort(5672);
        System.out.println("Ip Address(rabbit): "+ipAddress);

        try {
            connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.groupMember = groupMember;
            this.groupName = groupName;
            this.minValue = minValue;
        } catch (IOException | TimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createConnection() {
        DeliverCallback deliverCallback = createConsumerHandler();
        CancelCallback cancelCallback = cancelConsumption();

        //String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback)
        try {
            String consumeTag = channel.basicConsume(QUEUE_CLOUD_NAME, true, deliverCallback, cancelCallback);
            System.out.println("Consumer Tag:"+consumeTag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DeliverCallback createConsumerHandler() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            Message msg = new Message(new String(delivery.getBody(), "UTF-8"));
            String routingKey=delivery.getEnvelope().getRoutingKey();
            System.out.println("Message received:" +msg+" | "+ routingKey);

            // only when the value is more equals or higher than 120 the event is processed
            // in this case sends a message to spread group
            if(msg.getValue() >= this.minValue) groupMember.sendMessage(this.groupName, msg.toString());

        };
        return deliverCallback;
    }

    private CancelCallback cancelConsumption() {
        CancelCallback cancelCallback=(consumerTag)->{
            System.out.println("CANCEL Received! "+consumerTag);
        };
        return cancelCallback;
    }

    public void closeChannel() {
        try {
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            System.out.println("Error closing RabbitMQ channel: "+e.getMessage());
        }
    }
}
