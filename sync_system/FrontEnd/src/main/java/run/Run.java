package run;

import java.util.Scanner;

public class Run {

    private static String IP_DEAMON = "localhost";
    private static int SERVER_PORT = 8000;

    public static void main(String[] args) {
        if(args.length > 1) {
            IP_DEAMON = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
        }
        else if(args.length > 0) {
            IP_DEAMON = args[0];
        }

        Scanner sc = new Scanner(System.in);

        // creation of the rabbitMQ consumer and member of spread
        FrontEnd eventProcessing = new FrontEnd(IP_DEAMON, SERVER_PORT);
        eventProcessing.createMembersAndServer();

        sc.nextLine();
        eventProcessing.closeConnection();

    }
}
