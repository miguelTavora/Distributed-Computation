package ringserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ringServer.Confirm;
import ringServer.RingServerGrpc;
import ringServer.ServerInfo;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RingServer {

    public static final Logger logger = Logger.getLogger(RingServer.class.getName());

    private String ringServerIP;
    private int ringServerPort;

    private String thisServerIP;
    private int clientServerPort;

    private ManagedChannel channel = null;
    private RingServerGrpc.RingServerStub noBlockingStub;
    private RingServerGrpc.RingServerBlockingStub blockingStub;

    private String nextIpAddress = null;
    private int nextPort = -1;

    public RingServer(String ringServerIP, int ringServerPort, String thisServerIP, int clientServerPort) {
        this.ringServerIP = ringServerIP;
        this.ringServerPort = ringServerPort;
        this.thisServerIP = thisServerIP;
        this.clientServerPort = clientServerPort;
    }

    public void run() {
        try {
            // Channels are secure by default (via SSL/TLS)
            channel = ManagedChannelBuilder.forAddress(this.ringServerIP, this.ringServerPort)
                    .usePlaintext().build();

            noBlockingStub = RingServerGrpc.newStub(channel);
            blockingStub = RingServerGrpc.newBlockingStub(channel);

            ServerInfo currentServer = ServerInfo.newBuilder()
                    .setIpAddress(this.thisServerIP).setPort(this.clientServerPort).build();


            int count = 0;
            while(true) {
                // register the IP address of the server,
                // it's synchronized, so it can be on loop
                Confirm confirm = blockingStub.registerServer(currentServer);

                // when got the confirmation that the server is correctly stored
                if(confirm.getResult()) break;
                //tries 3 times to connect, if it can't connect stops execution
                if(count ==  2) throw new Exception("Too many tries of register server");
                count++;
            }

            // assinc call to get the next hop of the ring
            RingServerObserver stream = new RingServerObserver();
            noBlockingStub.nextRingServer(currentServer, stream);

            System.out.println("Register the server with success");
            System.out.println("Waiting to receive next hop...");
            while(!stream.isComplete()) {
                Thread.sleep(1000);
            }

            // set the values received values to give access to the information
            if(stream.onSuccess()) {
                nextIpAddress = stream.getNextHopIpAddress();
                nextPort = stream.getNextHopPort();
                System.out.println("Next Hop IP: "+nextIpAddress+", Port: "+nextPort);
            }

            // when the server didn't receive the next hop
            else {
                Confirm confirm = blockingStub.disconnectServer(currentServer);
                System.out.println("Server disconnected result: "+confirm.getResult());
                disconnectToRing();
                throw new Exception("The server didn't received the next hop on the ring");
            }


        } catch (Exception ex) {
            // when catch an error connecting to the server
            logger.log(Level.SEVERE, "Error:" + ex.getMessage());
        }
    }

    public void disconnectToRing() {
        logger.log(Level.INFO, "Shutdown channel to ring manager ");
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getNextIpAddress() {
        return this.nextIpAddress;
    }

    public int getNextPort() {
        return this.nextPort;
    }

}
