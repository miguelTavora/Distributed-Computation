package serverserverserver;

import chatserver.ServerClient;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import chat.ChatMessage;
import serverServer.Message;

public class ServerServerServerStream implements StreamObserver<Message> {

    private final StreamObserver<Empty> replies;
    private final ServerClient clients;
    private final String thisServerIP;
    private ChatMessage chatMessage = null;
    private String sourceIP = null;

    public ServerServerServerStream(StreamObserver<Empty> replies, ServerClient clients, String thisServerIP) {
        this.replies = replies;
        this.clients = clients;
        this.thisServerIP = thisServerIP;
    }

    @Override
    public void onNext(Message message) {
        ChatMessage msg = ChatMessage.newBuilder()
                .setFromUser(message.getFromUser()).setTxtMsg(message.getTxtMsg())
                .setSourceIP(message.getSourceIP()).build();
        sourceIP = message.getSourceIP();
        chatMessage = msg;
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error on call:"+throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        if(!sourceIP.equals(this.thisServerIP)) {
            // for all users sends all the messages
            clients.sendMessageToAll(chatMessage);
            // sends the message to the next server
            clients.sendMessageToNextServer(chatMessage, sourceIP);
        }
        Empty empty = Empty.newBuilder().build();
        replies.onNext(empty);
        replies.onCompleted();
    }
}
