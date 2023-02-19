package ringmanager;

import io.grpc.*;

public class RingManager {

    private static int NUM_INSTANCES = 3;
    private final static int ringServerPort = 8000;
    private final static int ringClientPort = 7000;
    private static RingManagerServer managerServer;
    private static RingManagerClient managerClient;

    public static void main(String[] args) {
        try {
            // first argument is port, second argument num instances
            if (args.length > 0) NUM_INSTANCES = Integer.parseInt(args[0]);

            managerServer = new RingManagerServer(NUM_INSTANCES);
            managerClient = new RingManagerClient(managerServer, NUM_INSTANCES);

            // ring to receive servers
            final Server ringServer = ServerBuilder.forPort(ringServerPort)
                    .addService(managerServer).build().start();

            // ring to receive client, to send IP address of a server
            final Server ringClient = ServerBuilder.forPort(ringClientPort)
                    .addService(managerClient).build().start();

            // anonymous thread, so it can execute both servers (to receive client and server)
            new Thread() {
                public void run() {
                    try {
                        ringServer.awaitTermination();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            System.err.println("*** ring manager waiting");
            ringClient.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
