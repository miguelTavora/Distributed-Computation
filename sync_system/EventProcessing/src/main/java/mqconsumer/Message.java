package mqconsumer;

public class Message {

    private final long sid;
    private final String local;
    private final String date;
    private final int value;

    public Message(String receivedMessage) {
        // to collect information it separates the information on different indexes of a array
        String[] message = receivedMessage.substring(1, receivedMessage.length() -1)
                .split(",");

        this.sid = Long.parseLong(message[0]);
        this.local = message[1].trim();
        this.date = message[2].trim();
        this.value = Integer.parseInt(message[3].trim());
    }

    public long getId() {
        return this.sid;
    }

    public String getLocal() {
        return this.local;
    }

    public String getDate() {
        return this.date;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        return "("+this.sid+", "+this.local+", "+this.date+", "+this.value+")";
    }
}
