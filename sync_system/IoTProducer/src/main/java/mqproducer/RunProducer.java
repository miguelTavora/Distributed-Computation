package mqproducer;

public class RunProducer {

    private static String brokerIpAddress = "localhost";

    public static void main(String[] args) {
        if(args.length > 0) brokerIpAddress = args[0];


        IoTProducer producer = new IoTProducer(brokerIpAddress);
        producer.executeSending();

        //when press enter it stops sending messages
        producer.setStopExecution(true);
    }
}
