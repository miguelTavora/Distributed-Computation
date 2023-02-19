package rmqconfigurator;

import rabbitmqconsumer.RabbitMQConsumer;

import java.util.Scanner;

public class RunConfig {

    private static String ipAddress = "localhost";

    public static void main(String[] args) {
        if (args.length > 0) ipAddress = args[0];

        Scanner scan = new Scanner(System.in);

        // creates the one exchange and two queues
        // one for this level and one for the third level
        RabbitMQConf conf = new RabbitMQConf(ipAddress);
        conf.createExchangeAndQueues();

        // creates one consumer for one of the queues created, to print the messages
        RabbitMQConsumer consumer = new RabbitMQConsumer(conf.getChannel());
        consumer.createConsumer();

        // waits any key to stop execution
        scan.nextLine();
        // closes all connections
        conf.closeConnectionAndChannel();
    }
}
