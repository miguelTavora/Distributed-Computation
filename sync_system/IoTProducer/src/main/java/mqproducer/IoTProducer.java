package mqproducer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import iotcar.Input;
import iotcar.IoTCar;
import iotcar.Message;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class IoTProducer {

    private final int BROKER_PORT = 5672;
    private final String EXHANGE_NAME = "messages";
    private final String ROUTING_KEY = "keyfog";
    private Connection connection;
    private Channel channel;
    private IoTCar ioTCar;
    private Input input;
    private boolean stopExecution = false;

    public IoTProducer(String brokerIpAddress) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(brokerIpAddress);
        factory.setPort(BROKER_PORT);
        System.out.println("Ip Address: "+brokerIpAddress);
        input = new Input();

        //tries to connect to the exchange
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            System.out.println("Error connecting: "+e.getMessage());
            System.exit(0);
        }
        ioTCar = new IoTCar();
    }

    // publish a RabbitMQ to the exchange specified
    public void publishMessage(Message msg) {
        try {
            channel.basicPublish(EXHANGE_NAME, ROUTING_KEY, null, msg.toString().getBytes());
            System.out.println("Message sent: "+msg.toString());
        } catch (IOException e) {
            System.out.println("Error sending message: "+e.getMessage());
        }
    }

    // method to select auto or manual sending messages
    public void executeSending() {
        int selected = input.selectOption();
        if(selected == 1) generateMessagesAndPublish();
        else createMessagesByUser();

    }

    // generates a random message and send it to RabbitMQ Exchange
    public void generateMessagesAndPublish() {
        // scanner to stop execution of the program
        Scanner sc = new Scanner(System.in);
        new Thread() {
            public void run() {
                while(!stopExecution) {
                    Message msg = ioTCar.generateRandomMessage();
                    publishMessage(msg);

                    // waits randomly between 5 and 3 seconds
                    try {
                        Thread.sleep(ioTCar.generateRandomNumber(5000, 3000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    channel.close();
                    connection.close();
                } catch (IOException | TimeoutException e) {
                    System.out.println("Error closing channel: "+e.getMessage());
                }
            }
        }.start();

        sc.nextLine();
    }

    // generates a message based on the user input
    public void createMessagesByUser() {
        while(!stopExecution) {
            // obtain the values needed to send the message
            long sid = input.getValidSid();
            String local = input.getValidLocal();
            String date = input.isValidDate();
            int value = input.getValidValue();

            // obtain the message and publish on rabbitMQ
            Message msg = new Message(sid, local, date, value);

            publishMessage(msg);
        }
        try {
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            System.out.println("Error closing channel: "+e.getMessage());
        }
    }

    // to set the value to stop the sending of messages
    public void setStopExecution(boolean stopExecution) {this.stopExecution = stopExecution;}

}
