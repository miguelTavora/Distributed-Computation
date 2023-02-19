package ringserver;

import io.grpc.stub.StreamObserver;
import ringServer.ServerInfo;

public class RingServerObserver implements StreamObserver<ServerInfo> {

    private boolean isComplete = false;
    private boolean success = false;
    private String nextHopIpAddress;
    private int nextHopPort;

    @Override
    public void onNext(ServerInfo serverInfo) {
        nextHopIpAddress = serverInfo.getIpAddress();
        nextHopPort = serverInfo.getPort();
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error on call: "+throwable.getMessage());
        isComplete = true;
    }

    @Override
    public void onCompleted() {
        isComplete = true;
        success = true;
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public boolean onSuccess() {
        return this.success;
    }

    public String getNextHopIpAddress() {
        return this.nextHopIpAddress;
    }

    public int getNextHopPort() {
        return this.nextHopPort;
    }
}
