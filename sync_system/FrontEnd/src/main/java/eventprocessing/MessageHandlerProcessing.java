package eventprocessing;

import spread.BasicMessageListener;
import spread.MembershipInfo;
import spread.SpreadGroup;
import spread.SpreadMessage;
import spreadcommon.GroupMember;
import spreadcommon.LeaderGroup;
import spreadcommon.StoreInformation;

import java.util.ArrayList;


public class MessageHandlerProcessing implements BasicMessageListener {

    // string used to the members that is not the leader to know that the message
    // is to set number os consumers
    private final String JSON_CONSUMERS = "{\"consumerNames\"";
    private final String NEW_CONSUMER = "##consumer##";
    private final String REQUEST_CONSUMERS = "##consumer?##";

    // this class is similar to the MessageHandler of the Event-Processing
    // only with some adjustments to store information
    // when a member leaves or joins the group
    private GroupMember groupMember;
    private LeaderGroup leaderGroup;
    private StoreInformation information;



    public MessageHandlerProcessing(GroupMember groupMember, StoreInformation information) {
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

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void regularMessage(SpreadMessage msg) {
        String message = new String(msg.getData());

        // when the messages are to define the leader or obtain current leader
        leaderMessages(msg, message);

        // sets the values to the storeInformation
        // this way the front-end can access the values
        if(message.startsWith("(") && message.endsWith(")")) {
            MessageInfo messageInfo = new MessageInfo(message);
            this.information.addAllInformation(messageInfo.getId(), messageInfo.getLocal(),
                    messageInfo.getDate(), messageInfo.getValue());
            // to show the info stored
            System.out.println(information.toString());
        }

        // when a new consumer joins the leader sends messagens with all consumers
        // only leader enters this if
        else if(message.startsWith(NEW_CONSUMER) && (leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()) != null)) {
            if(leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()).equals(groupMember.getMemberName())) {
                this.information.addConsumerName((message.replace(NEW_CONSUMER, "")));
                MessageConsumers msgCons = new MessageConsumers();
                msgCons.setConsumerNames(this.information.getConsumerNames());
                groupMember.sendMessage(msg.getGroups()[0].toString(), msgCons.getJsonFormat());
            }
        }

        // when a member that is not a consumer request for how many consumers exists
        // only the leader responds to this
        else if (message.equals(REQUEST_CONSUMERS) && (leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()) != null)) {
            if(leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()).equals(groupMember.getMemberName())) {
                MessageConsumers msgCons = new MessageConsumers();
                msgCons.setConsumerNames(this.information.getConsumerNames());
                groupMember.sendMessage(msg.getGroups()[0].toString(), msgCons.getJsonFormat());
            }
        }


        // all the other members set the number of consumers said by the leader
        else if(message.startsWith(JSON_CONSUMERS) &&
                (leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()) == null
                        || !leaderGroup.getLeaderOfGroup(msg.getGroups()[0].toString()).equals(groupMember.getMemberName()))) {

            MessageConsumers msgCons = new MessageConsumers();
            MessageConsumers messageConsumers = msgCons.getClassFromJson(message);
            this.information.setConsumerNames(messageConsumers.getConsumerNames());
        }
        leaderGroup.printLeaders();
    }

    public void printMembershipInfo(MembershipInfo info) {
        if (info.isRegularMembership()) {
            // when some member joins tries to be the leader, and sends messages to find the leader or become the leader
            if (info.isCausedByJoin()) {
                leaderGroup.findLeader(info, groupMember);
                // sets to need send message and info about the message
                this.information.addUserEvents(info.getJoined().toString().split("#")[1]);
                this.information.addTypeEvents("join");

                // sends a message saying that needs a message to sinc the number of members
                if(info.getJoined().toString().split("#")[1].equals(groupMember.getMemberName())) {
                    groupMember.sendMessage(info.getGroup().toString(), REQUEST_CONSUMERS);
                }

            } else if (info.isCausedByLeave()) {
                leaderGroup.reelectLeader(info, groupMember, info.getLeft().toString());
                // sets to need send message and info about the message
                this.information.addUserEvents(info.getLeft().toString().split("#")[1]);
                this.information.addTypeEvents("leave");
                this.information.removeConsumerName(info.getLeft().toString().split("#")[1]);


            } else if (info.isCausedByDisconnect()) {
                leaderGroup.reelectLeader(info, groupMember, info.getDisconnected().toString());
                // set info
                this.information.addUserEvents(info.getDisconnected().toString().split("#")[1]);
                this.information.addTypeEvents("disconnect");
                this.information.removeConsumerName(info.getDisconnected().toString().split("#")[1]);

            } else if (info.isCausedByNetwork()) {
                ArrayList<String> consumersConnected = new ArrayList<>();
                for (int i = 0; i < info.getVirtualSynchronySets().length; ++i) {
                    SpreadGroup[] setMembers = info.getVirtualSynchronySets()[i].getMembers();
                    for (int j = 0; j < setMembers.length; ++j) {
                        //check which consumers is still connected
                        if(this.information.getConsumerNames().contains(setMembers[j].toString().split("#")[1])) {
                            consumersConnected.add(setMembers[j].toString().split("#")[1]);
                        }
                    }
                }

                // elects new leader to the group
                leaderGroup.reelectLeaderNetwork(info, groupMember);

                // sets info
                this.information.addUserEvents("#error-connection#"+info.getVirtualSynchronySets().length);
                this.information.addTypeEvents("network change");
                this.information.setConsumerNames(consumersConnected);
            }
        }
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
    }
}
