package chatclient;

import com.google.protobuf.Empty;
import contract.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class User {

    private static String serverIP = "localhost";
    private static int serverPort = 8000;

    private static ManagedChannel channel = null;
    private static QuestionsGrpc.QuestionsBlockingStub blockingStub;


    public static void main(String[] args) throws Exception {

        try {
            // to set the server IP
            if (args.length > 0) serverIP = args[0];

            channel = ManagedChannelBuilder.forAddress(serverIP, serverPort)
                    .usePlaintext().build();

            System.out.println("Server IP: "+serverIP+", port: "+serverPort);

            blockingStub = QuestionsGrpc.newBlockingStub(channel);

            // gets the name of the user
            UserInput input = new UserInput();
            input.getNickname();

            run(input);


        } catch (Exception ex) {
            System.out.println("Error client: "+ex.getMessage());
        }
        if (channel!=null) {
            System.out.println("Shutdown Ring channel");
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static void run(UserInput input) {
        boolean flag = true;
        while(flag) {
            int selected = input.getSelectedNumber();

            switch (selected) {
                case 1:
                    getMaxValueStored();
                    break;
                case 2:
                    getMeanVelocity();
                    break;
                case 3:
                    getMeanBetweenDates(input);
                    break;
                case 4:
                    getNumConsumers();
                    break;
                case 5:
                    executeConsumer(input);
                    break;
                case 6:
                    flag = false;
                    break;
            }
        }
    }

    private static void getMaxValueStored() {
        Empty empty = Empty.newBuilder().build();

        VelocityComplete vel =  blockingStub.maxValueVelocity(empty);
        System.out.println("Max value: "+vel.getVelocity()+", sid: "+vel.getSid()
                +", local: "+vel.getLocal()+", date: "+vel.getData()+"\n");
    }

    private static void getMeanVelocity() {
        Empty empty = Empty.newBuilder().build();

        Velocity vel = blockingStub.meanVelocity(empty);
        System.out.println("Mean value: "+vel.getVelocity()+"\n");
    }

    private static void getMeanBetweenDates(UserInput input) {
        String[] dates = input.getValidDate();

        Date date = Date.newBuilder().setStartDate(dates[0]).setEndDate(dates[1]).build();

        Velocity vel = blockingStub.meanVelocityBetweenDates(date);
        System.out.println("Mean velocity: "+vel.getVelocity()+", between: "+dates[0]+", and "+dates[1]+"\n");
    }

    private static void getNumConsumers() {
        Empty empty = Empty.newBuilder().build();

        Consumer consumer = blockingStub.numConsumers(empty);
        System.out.println("Number of consumers: "+consumer.getNumConsumers()+"\n");
    }

    private static void executeConsumer(UserInput input) {
        boolean result = input.getValidUser(input.getClientName());

        if(result) {
            ConsumerName consumer = ConsumerName.newBuilder().setConsumerName(input.getClientName()).build();
            Confirm conf = blockingStub.executeConsumer(consumer);
            System.out.println("Result: "+conf.getConfirm()+", with: "+conf.getText()+"\n");
        }
    }

}
