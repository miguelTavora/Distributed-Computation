package chatclient;

import chat.ChatMessage;
import io.grpc.stub.StreamObserver;

public class ClientChatObserver implements StreamObserver<ChatMessage> {

    private boolean onError = false;

    @Override
    public void onNext(ChatMessage chatMessage) {
        System.out.println("[sender("+chatMessage.getSourceIP()+"): " + chatMessage.getFromUser() + "] " + chatMessage.getTxtMsg());
    }

    @Override
    public void onError(Throwable throwable) {
        onError = true;
        System.err.println("Error received: "+throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        System.out.println("You finished the sending of messages");
    }

    public boolean isOnError() {
        return this.onError;
    }
}
