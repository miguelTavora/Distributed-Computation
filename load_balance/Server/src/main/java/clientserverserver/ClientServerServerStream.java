package clientserverserver;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class ClientServerServerStream implements StreamObserver<Empty> {

    private boolean isCompleted = false;

    @Override
    public void onNext(Empty empty) {

    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error on call:"+throwable.getMessage());
        this.isCompleted = true;
    }

    @Override
    public void onCompleted() {
        this.isCompleted = true;
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }
}
