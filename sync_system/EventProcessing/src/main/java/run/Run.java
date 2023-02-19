package run;

import java.util.Scanner;

public class Run {

    private static String IP_RABBITMQ = "localhost";
    private static String IP_DEAMON = "localhost";

    public static void main(String[] args) {
        if(args.length > 0) {
            IP_RABBITMQ = args[0];
            IP_DEAMON = args[1];
        }
        Scanner sc = new Scanner(System.in);

        // creation of the rabbitMQ consumer and member of spread
        EventProcessing eventProcessing = new EventProcessing(IP_RABBITMQ, IP_DEAMON);
        eventProcessing.createConsumer();

        sc.nextLine();
        eventProcessing.closeConnection();

    }
}
