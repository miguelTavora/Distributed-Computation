package frontend;

import spread.BasicMessageListener;
import spread.MembershipInfo;
import spread.SpreadGroup;
import spread.SpreadMessage;
import spreadcommon.GroupMember;
import spreadcommon.LeaderGroup;
import spreadcommon.StoreInformation;


public class MessageHandlerFront implements BasicMessageListener {

    private GroupMember groupMember;
    private LeaderGroup leaderGroup;
    private StoreInformation information;

    public MessageHandlerFront(GroupMember groupMember, StoreInformation information) {
        this.groupMember = groupMember;
        this.information = information;
        leaderGroup = new LeaderGroup();
    }

    @Override
    public void messageReceived(SpreadMessage msg) {
        try {
            // regular message with safe flag
            if (msg.isRegular() && msg.isSafe()) {
                regularMessage(msg);
            }
            // a membership message
            else if (msg.isMembership()) {
                MembershipInfo info = msg.getMembershipInfo();
                printMembershipInfo(info);
            }
            // Received a Reject message
            else if (msg.isReject()) {System.out.println("The message(rejected) is: " + new String(msg.getData()));}
            // other
            else {System.out.println("Message is of unknown type: " + msg.getServiceType());}

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void regularMessage(SpreadMessage msg) {
        String messageStr = new String(msg.getData());


        // the message is asking who is the leader of this group
        if (messageStr.contains(leaderGroup.getRequestLeader())) {
            leaderGroup.sendMessageItsLeader(msg, groupMember);
        }

        // obtains the messages to get the leader or elect one
        else if(messageStr.contains(leaderGroup.getRequestIsLeader())) {
            // leaderDefined is true when set the leader
            leaderGroup.obtainLeader(msg, groupMember);
        }


        // when a new member join the groups this if syncs the info with all the others
        if(messageStr.startsWith("{") && messageStr.endsWith("}")) {
            MessageSync ms = new MessageSync();
            MessageSync msgSync = ms.getClassFromJson(messageStr);
            // only if the user is the current member and the leader sent on the message
            if(msgSync.getUser().equals(groupMember.getMemberName())) {
                information.setIdentifiers(msgSync.getIdentifiers());
                information.setLocals(msgSync.getLocals());
                information.setDates(msgSync.getDates());
                information.setValues(msgSync.getValues());
                information.setUserEvents(msgSync.getUserEvents());
                information.setTypeEvents(msgSync.getTypeEvents());
                information.setConsumerNames(msgSync.getConsumerNames());
            }
        }
        System.out.println(information.toString());
        leaderGroup.printLeaders();
    }

    public void printMembershipInfo(MembershipInfo info) {
        SpreadGroup group = info.getGroup();

        if (info.isRegularMembership()) {

            // when some member joins tries to be the leader, and sends messages to find the leader or become the leader
            if (info.isCausedByJoin()) {
                System.out.println("JOIN of " + info.getJoined());
                leaderGroup.findLeader(info, groupMember);

                if(leaderGroup.getLeaderOfGroup(group.toString()) != null) {
                    if(leaderGroup.getLeaderOfGroup(group.toString()).equals(groupMember.getMemberName())) {
                        sendMessageSync(group.toString(), info.getJoined().toString().split("#")[1]);
                    }
                }

            } else if (info.isCausedByLeave()) {
                System.out.println("LEAVE of " + info.getLeft());
                leaderGroup.reelectLeader(info, groupMember, info.getLeft().toString());


            } else if (info.isCausedByDisconnect()) {
                System.out.println("DISCONNECT of " + info.getDisconnected());
                leaderGroup.reelectLeader(info, groupMember, info.getDisconnected().toString());


            } else if (info.isCausedByNetwork()) {
                System.out.println("NETWORK change");
                MembershipInfo.VirtualSynchronySet virtual_synchrony_sets[] = info.getVirtualSynchronySets();
                for (int i = 0; i < virtual_synchrony_sets.length; ++i) {
                    MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[i];
                    SpreadGroup[] setMembers = set.getMembers();
                    System.out.println("Virtual Synchrony Set " + i + " has " + set.getSize() + " members:");
                    for (int j = 0; j < setMembers.length; ++j) {
                        System.out.println("\t" + setMembers[j]);
                    }
                }
                // elects new leader to the group
                leaderGroup.reelectLeaderNetwork(info, groupMember);
            }
        } else if (info.isTransition()) {System.out.println("TRANSITIONAL membership for group " + group);
        } else if (info.isSelfLeave()) {System.out.println("SELF-LEAVE message for group " + group);}
    }

    private void sendMessageSync(String groupName, String memberName) {
        MessageSync msgSync = new MessageSync();
        msgSync.setUser(memberName);
        msgSync.setIdentifiers(information.getIdentifiers());
        msgSync.setLocals(information.getLocals());
        msgSync.setDates(information.getDates());
        msgSync.setValues(information.getValues());
        msgSync.setUserEvents(information.getUserEvents());
        msgSync.setTypeEvents(information.getTypeEvents());
        msgSync.setConsumerNames(information.getConsumerNames());
        groupMember.sendMessage(groupName, msgSync.getJsonFormat());
    }

}
