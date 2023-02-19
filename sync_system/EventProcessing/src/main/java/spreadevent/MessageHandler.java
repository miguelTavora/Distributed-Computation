package spreadevent;

import spread.BasicMessageListener;
import spread.MembershipInfo;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.ArrayList;


public class MessageHandler implements BasicMessageListener {

    // string used to the members that is not the leader to know that the message
    // is to set number os consumers
    private final String JSON_CONSUMERS = "{\"consumerNames\"";
    private final String NEW_CONSUMER = "##consumer##";
    private final String REQUEST_CONSUMERS = "##consumer?##";
    private ArrayList<String> consumerNames;

    private GroupMember groupMember;
    private LeaderGroup leaderGroup;



    public MessageHandler(GroupMember groupMember) {
        this.groupMember = groupMember;
        leaderGroup = new LeaderGroup();
        consumerNames = new ArrayList<>();
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
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void regularMessage(SpreadMessage msg) {
        System.out.println("Message sent by: "+msg.getSender().toString().split("#")[1]);
        byte[] bytes = msg.getData();
        System.out.println("The message is: " + new String(bytes));
        String data = new String(bytes);

        // when receives a message to attribute or say who is the leader
        leaderMessages(msg, data);
        leaderGroup.printLeaders();
    }

    public void printMembershipInfo(MembershipInfo info) {
        SpreadGroup group = info.getGroup();

        if (info.isRegularMembership()) {

            // when some member joins tries to be the leader, and sends messages to find the leader or become the leader
            if (info.isCausedByJoin()) {
                System.out.println("JOIN of " + info.getJoined());
                leaderGroup.findLeader(info, groupMember);

                // sends a message saying that is a consumer
                if(info.getJoined().toString().split("#")[1].equals(groupMember.getMemberName())) {
                    groupMember.sendMessage(group.toString(), NEW_CONSUMER+groupMember.getMemberName());
                }

            } else if (info.isCausedByLeave()) {
                System.out.println("LEAVE of " + info.getLeft());
                leaderGroup.reelectLeader(info, groupMember, info.getLeft().toString());

                // sets to need send message and info about the message
                this.consumerNames.remove(info.getLeft().toString().split("#")[1]);


            } else if (info.isCausedByDisconnect()) {
                System.out.println("DISCONNECT of " + info.getDisconnected());
                leaderGroup.reelectLeader(info, groupMember, info.getDisconnected().toString());

                this.consumerNames.remove(info.getDisconnected().toString().split("#")[1]);


            } else if (info.isCausedByNetwork()) {
                System.out.println("NETWORK change");
                ArrayList<String> consumersConnected = new ArrayList<>();
                MembershipInfo.VirtualSynchronySet virtual_synchrony_sets[] = info.getVirtualSynchronySets();
                for (int i = 0; i < virtual_synchrony_sets.length; ++i) {
                    MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[i];
                    SpreadGroup[] setMembers = set.getMembers();
                    System.out.println("Virtual Synchrony Set " + i + " has " + set.getSize() + " members:");
                    for (int j = 0; j < setMembers.length; ++j) {
                        System.out.println("\t" + setMembers[j]);
                        //check which consumers is still connected
                        if(this.consumerNames.contains(setMembers[j].toString().split("#")[1])) {
                            consumersConnected.add(setMembers[j].toString().split("#")[1]);
                        }
                    }
                }
                // elects new leader to the group
                leaderGroup.reelectLeaderNetwork(info, groupMember);

                // sets the consumers
                this.consumerNames = consumersConnected;

            }
        } else if (info.isTransition()) {System.out.println("TRANSITIONAL membership for group " + group);
        } else if (info.isSelfLeave()) {System.out.println("SELF-LEAVE message for group " + group);}
    }

    public void leaderMessages(SpreadMessage msg, String data) {
        // the message is asking who is the leader of this group
        if (data.contains(leaderGroup.getRequestLeader())) {
            leaderGroup.sendMessageItsLeader(msg, groupMember);
        }

        // obtains the messages to get the leader or elect one
        else if(data.contains(leaderGroup.getRequestIsLeader())) {
            leaderGroup.obtainLeader(msg, groupMember);
        }

        // when a member say that is a consumer, the leader increases his database and sync all the members
        else if(data.startsWith(NEW_CONSUMER) && (leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()) != null)) {
            if(leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()).equals(groupMember.getMemberName())) {
                this.consumerNames.add(data.replace(NEW_CONSUMER, ""));
                MessageConsumers msgCons = new MessageConsumers();
                msgCons.setConsumerNames(this.consumerNames);
                groupMember.sendMessage(msg.getGroups()[0].toString(), msgCons.getJsonFormat());
            }
        }

        // when a member that is not a consumer request for how many consumers exists
        else if (data.equals(REQUEST_CONSUMERS) && (leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()) != null)) {
            if(leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()).equals(groupMember.getMemberName())) {
                MessageConsumers msgCons = new MessageConsumers();
                msgCons.setConsumerNames(this.consumerNames);
                groupMember.sendMessage(msg.getGroups()[0].toString(), msgCons.getJsonFormat());
            }
        }

        // all the other members set the number of consumers said by the leader
        else if(data.startsWith(JSON_CONSUMERS)) {
            // check the member that join recently or the ones that ist not leader
            if((leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()) == null) ||
                    (!leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()).equals(groupMember.getMemberName()))) {

                MessageConsumers msgCons = new MessageConsumers();
                MessageConsumers messageConsumers = msgCons.getClassFromJson(data);
                this.consumerNames = messageConsumers.getConsumerNames();
            }
        }
    }

}
