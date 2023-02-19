package clientserverserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ringserver.RingServer;
import chat.ChatMessage;
import serverServer.Message;
import serverServer.ServerServerGrpc;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientServerServer {

    public static final Logger logger = Logger.getLogger(RingServer.class.getName());

    private ManagedChannel channel = null;
    private ServerServerGrpc.ServerServerStub noBlockingStub;

    public ClientServerServer(String nextIpAddress, int nextPort) {
        try {
            // Channels are secure by default (via SSL/TLS)
            channel = ManagedChannelBuilder.forAddress(nextIpAddress, nextPort).usePlaintext().build();
            noBlockingStub = ServerServerGrpc.newStub(channel);
        }catch (Exception ex) {
            // when catch an error connecting to the server
            logger.log(Level.SEVERE, "Error:" + ex.getMessage());
        }
    }

    public synchronized void run(ChatMessage message, String sourceIP) throws InterruptedException {
        ClientServerServerStream stream = new ClientServerServerStream();
        StreamObserver<Message> reqs = noBlockingStub.sendMessageNextHop(stream);

        //converts the ChatMessages to Message where Message has the source IP
        // to prevent a loop of messages
        Message msg = Message.newBuilder()
                .setFromUser(message.getFromUser()).setTxtMsg(message.getTxtMsg())
                .setSourceIP(sourceIP).build();
        reqs.onNext(msg);

        reqs.onCompleted();
        while (!stream.isCompleted()) {
            Thread.sleep(1000);
        }
    }
}
