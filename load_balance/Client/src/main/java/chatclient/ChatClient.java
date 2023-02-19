package chatclient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ringClient.IPUsed;
import ringClient.RingClientGrpc;
import ringClient.ServerInfo;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClient {

    public static final Logger logger = Logger.getLogger(ChatClient.class.getName());

    private static String ringManagerIP = "localhost";
    private static int ringManagerPort = 7000;

    private static ManagedChannel channelRing = null;
    private static RingClientGrpc.RingClientStub noBlockingStubRing;
    private static RingClientGrpc.RingClientBlockingStub blockingStubRing;
    private static int clientServerPort = 9000;

    // class used to communicate with server
    private static ClientServer client;

    public static void main(String[] args) throws Exception {

        try {
            // to set the ring manager IP, it has to be well known
            if (args.length > 0) ringManagerIP = args[0];

            channelRing = ManagedChannelBuilder.forAddress(ringManagerIP, ringManagerPort)
                    .usePlaintext().build();

            noBlockingStubRing = RingClientGrpc.newStub(channelRing);
            blockingStubRing = RingClientGrpc.newBlockingStub(channelRing);

            client = new ClientServer();

            String ipStr = "";

            //TODO
            for(int i = 0; i < 5; i++) {
                IPUsed ip = IPUsed.newBuilder().setIps(ipStr).build();
                ServerInfo svInfo = blockingStubRing.getAnyServer(ip);

                // waiting when the ring is not complete
                if(svInfo.getIpAddress().equals("")) {
                    System.out.println("Waiting server...");
                    Thread.sleep(2000);
                    i--;
                    continue;
                }

                boolean result = client.createConnection(svInfo.getIpAddress(), clientServerPort);

                if(result) {
                    byte resultRegister = -1;
                    //loops until get a valid username
                    while(true) {
                        resultRegister = client.register();
                        // 1 -> when success store the name
                        //-1 -> when can't connect to the server
                        //0 -> not valid name
                        if(resultRegister == 1 || resultRegister == -1) break;
                    }

                    if(resultRegister == 1) {
                        // sends messages
                        boolean close = client.sendMessages();
                        // if ends ths sending stops the execution
                        if(close) break;
                        // if got an error retries
                        else i--;
                    }
                }
                ipStr = svInfo.getIpAddress();
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error:" + ex.getMessage());
        }
        if (channelRing!=null) {
            logger.log(Level.INFO, "Shutdown Ring channel");
            channelRing.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
