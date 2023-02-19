package rabbitmqconsumer;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import rmqconfigurator.RabbitMQConf;

import java.io.IOException;

public class RabbitMQConsumer {

    private Channel channel;

    public RabbitMQConsumer(Channel channel) {
        this.channel = channel;
    }

    public void createConsumer() {
        System.out.println("Acknowledge method: Auto Acknowledge");
        DeliverCallback deliverCallback = createConsumerHandler();

        CancelCallback cancelCallback = createCancelConsuption();

        //String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback)
        // throws IOException;
        try {
            String consumeTag = channel.basicConsume(RabbitMQConf.QUEUE_FOG_NAME, true, deliverCallback, cancelCallback);
            System.out.println("Consumer Tag:"+consumeTag);
        } catch (IOException e) {
            System.out.println("Error creating consumer: "+e.getMessage());
        }
    }

    //method where it gives auto acknowledge
    private DeliverCallback createConsumerHandler() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // receive the message and converts to a class
            Message msg = new Message(new String(delivery.getBody(), "UTF-8"));
            String routingKey = delivery.getEnvelope().getRoutingKey();
            System.out.println("Message received: " +msg.toString()+" | routing key: "+ routingKey);
            System.out.println("Consumer tag: "+consumerTag+"\n");
        };
        return deliverCallback;
    }

    private CancelCallback createCancelConsuption() {
        // Consumer handler to receive cancel receiving messages
        CancelCallback cancelCallback = (consumerTag)-> {
            System.out.println("CANCEL Received! "+consumerTag);
        };
        return cancelCallback;
    }
}
