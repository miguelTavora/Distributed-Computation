package chatserver;

import chat.*;
import clientserverserver.ClientServerServer;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ConcurrentHashMap;

public class ServerClient extends ChatGrpc.ChatImplBase{

    // stores the client of server - server communication
    private ClientServerServer client;

    // stores the stream to send assinc messages to the clients
    private final ConcurrentHashMap<UserID, StreamObserver<ChatMessage>> clients;

    // has to store it to pass to the cliet of Server - Server connection
    private final String thisServerIP;

    public ServerClient(String nextServerIpAddress, int nextServerPort, String thisServerIP) {
        clients = new ConcurrentHashMap<UserID, StreamObserver<ChatMessage>>();
        client = new ClientServerServer(nextServerIpAddress, nextServerPort);
        this.thisServerIP = thisServerIP;
    }

    @Override
    public synchronized void sendMessage(ChatMessage inMessage, StreamObserver<Empty> responseObserver) {
        ChatMessage outMessage = null;
        // when a user send a message all the users registered receive the message
        for (UserID clientDest : clients.keySet()) {
            try {
                outMessage = ChatMessage.newBuilder()
                        .setFromUser(inMessage.getFromUser())
                        .setTxtMsg(inMessage.getTxtMsg()).setSourceIP(this.thisServerIP).build();
                clients.get(clientDest).onNext(outMessage);
            } catch (Throwable ex) {
                // error calling remote client, remove client name and callback
                System.out.println("Client " + clientDest.getName() + " removed");
                clients.remove(clientDest);
            }
        }

        //send the message to the others servers
        try {
            client.run(outMessage, this.thisServerIP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void register(UserID clientID, StreamObserver<ChatMessage> responseObserver) {
        // stores the stream to send messages to the client
        if (!clients.containsKey(clientID)) {
            clients.put(clientID, responseObserver);
            System.out.println("User stored: "+clientID.getName());
        }

        // when already have the client
        else {
            System.out.println("Client " + clientID.getName() + " already taken");
            Throwable t = new StatusException(
                    Status.INVALID_ARGUMENT.withDescription("Client nickname already taken")
            );
            responseObserver.onError(t);
        }
    }

    @Override
    public synchronized void closeRegister(UserID clientId, StreamObserver<Empty> responseObserver) {
        var id = clients.remove(clientId);

        // when the client don't exist
        if (id == null) {
            Throwable t = new StatusException(
                    Status.INVALID_ARGUMENT.withDescription("Client nickname don't exist")
            );
            responseObserver.onError(t);
        }
        // when remove the user
        else {
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getAllUsers(Empty request, StreamObserver<UsersID> responseObserver) {
        synchronized (clients) {
            UsersID.Builder users = UsersID.newBuilder();

            for(UserID user : clients.keySet()) {
                users.addNames(user.getName());
            }

            UsersID finalUsers = users.build();

            responseObserver.onNext(finalUsers);
            responseObserver.onCompleted();
        }
    }

    // method to send messages to all the clients connected to this server
    public synchronized void sendMessageToAll(ChatMessage message) {
        // for all users sends all the messages
        for(UserID user : clients.keySet()) {
            try {
                clients.get(user).onNext(message);
            } catch (Exception e) {
                clients.remove(user);
                System.out.println("User (removed): "+user.getName());
            }
        }
    }

    public synchronized void sendMessageToNextServer(ChatMessage message, String sourceIP) {
        try {
            client.run(message, sourceIP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
