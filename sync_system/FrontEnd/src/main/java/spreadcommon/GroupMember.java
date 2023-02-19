package spreadcommon;

import eventprocessing.MessageHandlerProcessing;
import frontend.MessageHandlerFront;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class GroupMember {

    // class similar to the one in the Event-Processing
    // only difference is that can add a message receiver either from Event-Processing
    // or  Front-End

    private final int DAEMON_PORT = 4803;
    private SpreadConnection connection;
    private BasicMessageListener msgHandler;
    private Map<String, SpreadGroup> groupsConnected = new HashMap<String,SpreadGroup>();
    private String memberName;


    // creates a connection with the deamon of spread
    public GroupMember(String ipAddress, String memberName, StoreInformation information, boolean frontend) {
        System.out.println("Ip address(spread): "+ipAddress);
        this.memberName = memberName;
        try {
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(ipAddress), DAEMON_PORT, memberName, false, true);

            // depending on argument frontend creates a receiver for event processing or front-end
            // the StoreInformation is the class used to pass information between event processing to front end
            msgHandler = (frontend) ? new MessageHandlerFront(this, information) :
                    new MessageHandlerProcessing(this, information);
            connection.add(msgHandler);
        } catch (SpreadException | UnknownHostException e) {
            System.out.println("Error connecting: "+e.getMessage());
            System.exit(0);
        }
    }

    //joins a group of spread
    public void joinGroup(String groupName)  {
        SpreadGroup newGroup = new SpreadGroup();
        try {
            newGroup.join(connection, groupName);
            groupsConnected.put(groupName, newGroup);
        } catch (SpreadException e) {
            System.out.println("Error connecting group: "+e.getMessage());
        }
    }

    //sends a message to a certain group
    public void sendMessage(String group, String txtMessage) {
        SpreadMessage msg = new SpreadMessage();
        msg.setSafe();
        msg.addGroup(group);
        msg.setData(txtMessage.getBytes());
        try {
            connection.multicast(msg);
        } catch (SpreadException e) {
            System.out.println("Error sending message: "+e.getMessage());
        }
    }

    // leaves the group that was connected
    public void leaveGroup(String groupName)  {
        SpreadGroup group = groupsConnected.get(groupName);
        if(group != null) {
            try {
                group.leave();
            } catch (SpreadException e) {
                System.out.println("Error removing group: "+e.getMessage());
            }
            groupsConnected.remove(groupName);
            System.out.println("Left group: "+group.toString());
        }
        else System.out.println("No group to leave.");
    }

    // closes connection with deamon
    public void closeConnection()  {
        try {
            // remove listener
            connection.remove(msgHandler);
            // disconnect
            connection.disconnect();
        } catch (SpreadException e) {
            System.out.println("Error disconnecting: "+e.getMessage());
        }
    }

    public String getMemberName() {
        return this.memberName;
    }
}
