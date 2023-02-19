package ringmanager;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import ringServer.Confirm;
import ringServer.RingServerGrpc;
import ringServer.ServerInfo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RingManagerServer extends RingServerGrpc.RingServerImplBase{

    private final ArrayList<ServerInfo> orderedServers = new ArrayList<ServerInfo>(); // to guarantee the order
    private final ConcurrentHashMap<ServerInfo, StreamObserver<ServerInfo>> serversOnHold;
    private final int numServers;

    public RingManagerServer(int numServers) {
        super();
        this.numServers = numServers;
        serversOnHold = new ConcurrentHashMap<ServerInfo, StreamObserver<ServerInfo>>();
    }

    // with synchronized it guarantees only when finish a request it starts another
    @Override
    public synchronized void registerServer(ServerInfo serverInfo, StreamObserver<Confirm> responseObserver) {
        Confirm confirm = null;
        boolean ipExists = false;

        // check if the IP already exists stored
        for (ServerInfo info : orderedServers) {
            if (info.getIpAddress().equals(serverInfo.getIpAddress())) {
                System.out.println("The IP address of the server already exists!");
                ipExists = true;
                break;
            }
        }

        // when the IP don't exist stores it
        if(!ipExists) orderedServers.add(serverInfo);

        confirm = Confirm.newBuilder().setResult(true).build();

        responseObserver.onNext(confirm);
        responseObserver.onCompleted();
        sendNextHop();// when ring is closed and has to send messages
    }

    @Override
    public synchronized void nextRingServer(ServerInfo serverInfo, StreamObserver<ServerInfo> responseObserver) {

        // when all the servers are connected to the ring
        if(this.numServers == orderedServers.size()) {
            //when it's the last element, goes to the first
            int nextServerIndex = (orderedServers.indexOf(serverInfo)+1)%orderedServers.size();

            responseObserver.onNext(orderedServers.get(nextServerIndex));
            responseObserver.onCompleted();
        } else {
            // stores the info of the server, when the ring is finished it sends the information
            if (!serversOnHold.containsKey(serverInfo))
                serversOnHold.put(serverInfo, responseObserver);

            // when the server already sent a request for the next hop
            else {
                System.err.println("Server " + serverInfo.getIpAddress() + " sent more than 1 request");
                Throwable t = new StatusException(
                        Status.INVALID_ARGUMENT.withDescription("Server already sent one request, " +
                                "please wait to finish the ring")
                );
                responseObserver.onError(t);
            }
        }
    }

    @Override
    public synchronized void disconnectServer(ServerInfo serverInfo, StreamObserver<Confirm> responseObserver) {
        boolean result = orderedServers.remove(serverInfo);
        Confirm confirm = Confirm.newBuilder().setResult(result).build();

        responseObserver.onNext(confirm);
        responseObserver.onCompleted();
    }

    // when there is servers waiting to send messages
    private synchronized void sendNextHop() {
        //check if there is any server waiting to send messages
        if(this.numServers == orderedServers.size()) {
            // goes to all servers waiting to send message
            for(ServerInfo serverInfo : serversOnHold.keySet()) {
                //when it's the last element, goes to the first
                int nextServerIndex = (orderedServers.indexOf(serverInfo)+1)%orderedServers.size();
                serversOnHold.get(serverInfo).onNext(orderedServers.get(nextServerIndex));
                serversOnHold.get(serverInfo).onCompleted();
            }
            serversOnHold.clear();
        }
    }

    public ArrayList<ServerInfo> getOrderedServers() {
        return this.orderedServers;
    }
}
