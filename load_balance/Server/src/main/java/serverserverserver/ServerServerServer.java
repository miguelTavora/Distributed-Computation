package serverserverserver;

import chatserver.ServerClient;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import serverServer.Message;
import serverServer.ServerServerGrpc;

public class ServerServerServer extends ServerServerGrpc.ServerServerImplBase{

    private final ServerClient clients;
    private final String thisServerIP;

    public ServerServerServer(ServerClient clients, String thisServerIP) {
        this.clients = clients;
        this.thisServerIP = thisServerIP;
    }

    @Override
    public StreamObserver<Message> sendMessageNextHop(StreamObserver<Empty> responseObserver) {
        ServerServerServerStream reqs = new ServerServerServerStream(responseObserver, this.clients, this.thisServerIP);
        return reqs;
    }
}
