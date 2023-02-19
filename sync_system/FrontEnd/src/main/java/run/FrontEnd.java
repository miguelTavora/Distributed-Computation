package run;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import server.ServerUser;
import spreadcommon.GroupMember;
import spreadcommon.StoreInformation;

import java.io.IOException;

public class FrontEnd {

    private final UserInput input = new UserInput();
    private final String GROUP_NAME_PROCESSING = "EventProcessing";
    private final String GROUP_NAME_FRONTEND = "FrontEnd";


    private StoreInformation information;
    private GroupMember memberProcessing;
    private GroupMember memberFrontEnd;
    private Server server;

    private String ipAddressSpread;
    private int serverPort;
    private String memberNameProcessing;
    private String memberNameFront;


    public FrontEnd(String ipAddressSpread, int serverPort) {
        this.ipAddressSpread = ipAddressSpread;
        this.serverPort = serverPort;
        // name for member of the event processing
        memberNameProcessing = input.getValidMemberName("(Event-Processing)");
        // name for front end spread group
        memberNameFront = input.getValidMemberName("(Front-End)");
    }

    public void createMembersAndServer() {
        information = new StoreInformation();

        //creation of the member of event processing
        memberProcessing = new GroupMember(ipAddressSpread, memberNameProcessing, information, false);
        // join on a group of spread
        memberProcessing.joinGroup(GROUP_NAME_PROCESSING);

        // creation and join of front-end group
        memberFrontEnd = new GroupMember(ipAddressSpread, memberNameFront, information, true);
        memberFrontEnd.joinGroup(GROUP_NAME_FRONTEND);

        // creation of server for user requests
        ServerUser serverUser = new ServerUser(information);

        try {
            server = ServerBuilder.forPort(this.serverPort).addService(serverUser).build().start();
            System.err.println("*** server waiting");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        memberProcessing.leaveGroup(GROUP_NAME_PROCESSING);
        memberFrontEnd.leaveGroup(GROUP_NAME_FRONTEND);
        memberProcessing.closeConnection();
        memberFrontEnd.closeConnection();
        server.shutdown();
    }
}
