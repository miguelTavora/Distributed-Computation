package spreadevent;

import spread.MembershipInfo;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LeaderGroup {

    private final String REQUEST_LEADER = "leader?";
    private final String REQUEST_IS_LEADER = "request_leader:";

    // map where key -> group name, value -> leader of the group
    private Map<String, String> groupLeaders = new HashMap<String, String>();
    private Map<String, ArrayList<String>> messagesReceivedLeader = new HashMap<String, ArrayList<String>>();

    // if it's the first connected to the group it automatically the leader
    // if not requests to all members to know who is the leader
    public void findLeader(MembershipInfo msg, GroupMember groupMember) {
        // only the member that dont know the leader send the question message
        if(groupLeaders.get(msg.getGroup().toString()) == null) {
            int numMembers = msg.getMembers().length;
            if (numMembers == 1) {
                groupLeaders.put(msg.getGroup().toString(), groupMember.getMemberName());
                return;
            }
            // sends a message to get who is the current leader
            groupMember.sendMessage(msg.getGroup().toString(), REQUEST_LEADER + msg.getMembers().length);
        }
    }

    // sends a message sending the leader or sends a random value to become the leader
    // inside the message it sends the number of members
    public void sendMessageItsLeader(SpreadMessage msg, GroupMember groupMember) {
        for (int i = 0; i < msg.getGroups().length; i++) {
            String leader = groupLeaders.get(msg.getGroups()[i].toString());
            String resultLeader = (leader == null) ? "" + getRandomLeaderNumber() : leader;
            String numberMembers = new String(msg.getData()).replace(REQUEST_LEADER, "");
            groupMember.sendMessage(msg.getGroups()[i].toString(), REQUEST_IS_LEADER
                    + resultLeader + ":" + numberMembers);
        }
    }


    //  obtain the leader when a member connects to group
    public void obtainLeader(SpreadMessage msg, GroupMember groupMember) {
        for(int i = 0; i < msg.getGroups().length; i++) {
            String groupName = msg.getGroups()[i].toString();
            ArrayList<String> values = messagesReceivedLeader.get(groupName);
            if(values == null) values = new ArrayList<String>();
            // first index is the leader or a number to be the leader
            // second is the number of members on the group
            String[] info = new String(msg.getData()).replace(REQUEST_IS_LEADER, "").split(":");
            // the sender is on format -> #sender#node and keep only the sender
            String sender = msg.getSender().toString().split("#")[1];
            values.add(info[0]+":"+sender);
            // stores the received messages
            messagesReceivedLeader.put(groupName, values);

            // when receive the message from all the members saying who is the leader
            if(values.size() == Integer.parseInt(info[1])) {
                String leader = returnLeader(values);
                // null is when its a error in synchronization
                if(leader != null) {
                    groupLeaders.put(msg.getGroups()[i].toString(), leader);
                    // removes the stored values, because the leader is attributed
                    messagesReceivedLeader.remove(msg.getGroups()[i].toString());
                }
                // when gets an error tries to create a new leader
                else {
                    groupLeaders.remove(groupName);
                    groupMember.sendMessage(groupName, REQUEST_IS_LEADER+getRandomLeaderNumber()+info[1]);
                }
            }
        }
    }

    // calculates the leader based on the received messages
    private String returnLeader(ArrayList<String> values) {
        int biggerIndex = -1;
        int biggerValue = -1;
        String leader = "";
        for(int i = 0; i < values.size(); i++) {
            String[] result = values.get(i).split(":");
            // value to be the leader or the name of the leader
            if(result[0].matches("[0-9]+")) {
                int value = Integer.parseInt(result[0]);
                if(value > biggerValue && leader.equals("")) {
                    biggerValue = value;
                    biggerIndex = i;
                }
            }
            // when there is already a leader attributed
            else {
                if(leader.equals("")) leader = result[0];

                else if(!leader.equals(result[0])) {
                    System.err.println("Different leader for same group");
                    return null;
                }
            }
        }
        if(leader.equals("")) return values.get(biggerIndex).split(":")[1];

        return leader;

    }

    // when a member leaves or disconnect, check if the member was the leader
    // if was the leader reelect leader
    public void reelectLeader(MembershipInfo msg, GroupMember groupMember, String messageInfo) {
        String[] memberLeave = messageInfo.split("#");

        if(memberLeave[1].equals(groupLeaders.get(msg.getGroup().toString()))){
            groupLeaders.remove(msg.getGroup().toString());
            String message = REQUEST_IS_LEADER +getRandomLeaderNumber()+":"+msg.getMembers().length;
            groupMember.sendMessage(msg.getGroup().toString(), message);
        }
    }

    public void reelectLeaderNetwork(MembershipInfo info, GroupMember groupMember) {
        String groupName = info.getGroup().toString();
        MembershipInfo.VirtualSynchronySet virtual_synchrony_sets[] = info.getVirtualSynchronySets();
        boolean memberConnected = false;
        // goes through all the deamons and members to get all members currently connected
        // and check if the current attributed leader for the groups are connected
        for (int i = 0; i < virtual_synchrony_sets.length; i++) {
            MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[i];
            SpreadGroup[] setMembers = set.getMembers();
            for (int j = 0; j < setMembers.length; j++) {
                String memberName = setMembers[j].toString().split("#")[1];
                if(groupLeaders.get(groupName).equals(memberName)) {
                    memberConnected = true;
                    break;
                }

            }
        }
        // when leader of the group was on the deamon disconnected
        // sends message to set another
        if(!memberConnected) {
            groupLeaders.remove(info.getGroup().toString());
            String message = REQUEST_IS_LEADER +getRandomLeaderNumber()+":"+info.getMembers().length;
            groupMember.sendMessage(info.getGroup().toString(), message);
        }
    }

    public int getRandomLeaderNumber() {
        Random rd = new Random();
        return rd.nextInt(Integer.MAX_VALUE);
    }

    public String getRequestLeader() {
        return this.REQUEST_LEADER;
    }

    public String getRequestIsLeader() {
        return this.REQUEST_IS_LEADER;
    }

    public void printLeaders() {
        System.out.println("Leader: "+groupLeaders+"\n");
    }

    public String getLeaderOfGroup(String group) {return groupLeaders.get(group);}

}
