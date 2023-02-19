package ringmanager;

import io.grpc.stub.StreamObserver;
import ringClient.IPUsed;
import ringClient.RingClientGrpc;
import ringClient.ServerInfo;

import java.util.Random;

public class RingManagerClient extends RingClientGrpc.RingClientImplBase {

    // object of the ring that listens to servers, to access the list of IPs of servers
    private final RingManagerServer server;
    private final int numInstances;

    public RingManagerClient(RingManagerServer server, int numInstances) {
        super();
        this.server = server;
        this.numInstances = numInstances;
    }

    @Override
    public void getAnyServer(IPUsed ipUsed, StreamObserver<ServerInfo> responseObserver) {
        String ip = ipUsed.getIps();

        // when the ring is not created
        if(this.numInstances != server.getOrderedServers().size()) {
            ServerInfo serverInfo = ServerInfo.newBuilder()
                    .setIpAddress("").build();
            responseObserver.onNext(serverInfo);
            responseObserver.onCompleted();
            return;
        }

        Random rd = new Random();
        while (true) {
            int indexIP = -1;
            for(int i = 0; i < server.getOrderedServers().size(); i++) {
                if(server.getOrderedServers().get(i).getIpAddress().equals(ip)) {
                    indexIP = i;
                    break;
                }
            }
            // if already tested a IP goes to the next one, if not tries a random
            int index = !ip.equals("") ? (indexIP + 1)%this.numInstances : rd.nextInt(this.numInstances);
            ringServer.ServerInfo info = server.getOrderedServers().get(index);

            ServerInfo serverInfo = ServerInfo.newBuilder()
                    .setIpAddress(info.getIpAddress()).build();

            responseObserver.onNext(serverInfo);
            responseObserver.onCompleted();
            break;
        }
    }
}
