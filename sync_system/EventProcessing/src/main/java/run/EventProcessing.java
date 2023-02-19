package run;

import mqconsumer.RabbitMQConsumer;
import spreadevent.GroupMember;

public class EventProcessing {

    private final UserInput input = new UserInput();
    private final String GROUP_NAME = "EventProcessing";
    private final int MIN_VALUE = 120;
    private RabbitMQConsumer consumer;
    private GroupMember member;



    public EventProcessing(String ipAddressRabbit, String ipAddressSpread) {
        // name for member of the spread group
        String name = input.getValidMemberName();

        //creation of the member of the spread group
        member = new GroupMember(ipAddressSpread, name);
        // join on a group of spread
        member.joinGroup(GROUP_NAME);

        // creation of rabbitmq consumer
        consumer = new RabbitMQConsumer(ipAddressRabbit, member, GROUP_NAME, MIN_VALUE);

    }

    public void createConsumer() {
        consumer.createConnection();
    }

    public void closeConnection() {
        member.leaveGroup(GROUP_NAME);
        member.closeConnection();
        consumer.closeChannel();
    }
}
