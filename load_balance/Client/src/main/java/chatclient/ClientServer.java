package chatclient;

import chat.ChatGrpc;
import chat.ChatMessage;
import chat.UserID;
import chat.UsersID;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientServer {

    public static final Logger logger = Logger.getLogger(ClientServer.class.getName());

    private ManagedChannel channel = null;
    private ChatGrpc.ChatStub noBlockingStub;
    private ChatGrpc.ChatBlockingStub blockingStub;

    private UserInfo userInfo;
    private ClientChatObserver stream;


    public boolean createConnection(String serverIP, int serverPort) {
        userInfo = new UserInfo(serverIP, serverPort);

        try {
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            channel = ManagedChannelBuilder.forAddress(serverIP, serverPort)
                    .usePlaintext().build();

            noBlockingStub = ChatGrpc.newStub(channel);
            blockingStub = ChatGrpc.newBlockingStub(channel);
            return true;

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error:" + ex.getMessage());
            return false;
        }
    }

    public byte register() {
        try {
            String username = userInfo.getNickname();
            // if it contains a name from the list return false
            if(getAllNames().contains(username)) return 0;

            // register client in remote server
            noBlockingStub.register(UserID.newBuilder().setName(username).build(), new ClientChatObserver());
            return 1;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error:" + e.getMessage());
            return -1;
        }
    }

    public boolean sendMessages() throws InterruptedException {
        // when closes the sending correctly the value will be true
        boolean result = false;
        try {
            // send messages to other users
            writeMessage(userInfo);
            result = true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error:" + ex.getMessage());
        }
        if (channel!=null) {
            logger.log(Level.INFO, "Shutdown channel to server ");
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        return result;
    }

    private List<String> getAllNames() {
        UsersID users = blockingStub.getAllUsers(Empty.newBuilder().build());

        List<String> ids = users.getNamesList();

        System.out.print("Users: ");
        for(String name : ids) {
            System.out.print(name+" | ");
        }
        System.out.println();
        return ids;
    }

    private void disconnect(String clientName) {
        UserID userID = UserID.newBuilder().setName(clientName).build();

        Empty empty = blockingStub.closeRegister(userID);

        if( empty !=  null) System.out.println("Connection closed");
    }

    private void writeMessage(UserInfo userInfo) {
        System.out.println("Enter lines or the word: \n" +
                "\"dis\"-> to disconnect");

        while (true) {
            String message = userInfo.obtainWrittenMessage();

            if(message.equals("dis")) {
                disconnect(userInfo.getClientName());
                break;
            }
            else
                // sends the message to all users
                blockingStub.sendMessage(ChatMessage.newBuilder()
                        .setFromUser(userInfo.getClientName())
                        .setTxtMsg(message).build());

        }
    }
}
