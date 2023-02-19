package iotcar;

public class Message {

    private final long sid;
    private final String local;
    private final String date;
    private final int value;

    public Message(long sid, String local, String date, int value) {
        this.sid = sid;
        this.local = local;
        this.date = date;
        this.value = value;
    }

    public String toString() {
        return "("+this.sid+", "+this.local+", "+this.date+", "+this.value+")";
    }
}
