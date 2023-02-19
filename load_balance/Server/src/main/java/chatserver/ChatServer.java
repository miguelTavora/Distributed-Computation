package chatserver;

import io.grpc.*;
import ringserver.RingServer;
import serverserverserver.ServerServerServer;

import java.util.logging.Logger;

public class ChatServer {

    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private static RingServer ringServer;
    // to use this ports must allow the firewall to use this ports
    private static String ringServerIP = "localhost";//obter o endereço IP da máquina virtual
    private static int ringServerPort = 8000;  // Port between this server and the ring manager

    private static String thisServerIP;

    private static ServerClient serverClient;
    private static int serverClientPort = 9000; // Port between this server and a PC client

    private static ServerServerServer serverServer;
    private static int serverServerPort = 8500; // Port used to communicate between servers of this type

    public static void main(String[] args) {
        try {
            ringServerIP = args[0];
            thisServerIP = args[1];

            ringServer = new RingServer(ringServerIP, ringServerPort, thisServerIP, serverServerPort);
            ringServer.run(); // server has to be a client of ring manager


            // server communication between server and client
            serverClient = new ServerClient(ringServer.getNextIpAddress(), ringServer.getNextPort(), thisServerIP);
            final Server svc = ServerBuilder.forPort(serverClientPort)
                    .addService(serverClient)
                    .build()
                    .start();

            logger.info("Server Client started, listening on " + serverClientPort);

            serverServer = new ServerServerServer(serverClient, thisServerIP);
            final Server svcSvc = ServerBuilder.forPort(serverServerPort)
                    .addService(serverServer)
                    .build()
                    .start();

            logger.info("Server Server started, listening on " + serverServerPort);

            //starts a thread to not stop the Server that listen to client requests
            new Thread() {
                public void run() {
                    try {
                        svc.awaitTermination();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();


            System.err.println("*** server await termination");
            svcSvc.awaitTermination();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
