package spreadcommon;

import java.util.ArrayList;

public class StoreInformation {

    // class used to store information
    // about all the information needed to
    // sync the server and to the User make requests

    private ArrayList<Long> identifiers;
    private ArrayList<String> locals;
    private ArrayList<String> dates;
    private ArrayList<Integer> values;

    // join or leave events
    private ArrayList<String> userEvents;
    private ArrayList<String> typeEvents;

    // consumer members
    private ArrayList<String> consumerNames;

    public StoreInformation() {
        identifiers = new ArrayList<Long>();
        locals = new ArrayList<String>();
        dates = new ArrayList<String>();
        values = new ArrayList<Integer>();

        userEvents = new ArrayList<String>();
        typeEvents = new ArrayList<String>();
        consumerNames = new ArrayList<String>();
    }

    public void addAllInformation(long identifier, String local, String date, int value) {
        identifiers.add(identifier);
        locals.add(local);
        dates.add(date);
        values.add(value);
    }

    public void addUserEvents(String user) {
        userEvents.add(user);
    }

    public void addTypeEvents(String type) {
        typeEvents.add(type);
    }

    public ArrayList<Long> getIdentifiers() {
        return this.identifiers;
    }

    public void setIdentifiers(ArrayList<Long> identifiers) {
        this.identifiers = identifiers;
    }

    public ArrayList<String> getLocals() {
        return this.locals;
    }

    public void setLocals(ArrayList<String> locals) {
        this.locals = locals;
    }

    public ArrayList<String> getDates() {
        return this.dates;
    }

    public void setDates(ArrayList<String> dates) {
        this.dates = dates;
    }

    public ArrayList<Integer> getValues() {
        return this.values;
    }

    public void setValues(ArrayList<Integer> values) {
        this.values = values;
    }

    public ArrayList<String> getUserEvents() {
        return this.userEvents;
    }

    public void setUserEvents(ArrayList<String> userEvents) {
        this.userEvents = userEvents;
    }

    public ArrayList<String> getTypeEvents() {
        return this.typeEvents;
    }

    public void setTypeEvents(ArrayList<String> typeEvents) {
        this.typeEvents = typeEvents;
    }


    public ArrayList<String> getConsumerNames() {return this.consumerNames;}

    public void setConsumerNames(ArrayList<String> memberNames) {this.consumerNames = memberNames;}
    public void addConsumerName(String memberName) {this.consumerNames.add(memberName);}
    public void removeConsumerName(String memberName) {this.consumerNames.remove(memberName);}

    public String toString() {
        String result = "###### DATA STORED #########\n";
        result += "identifiers: "+identifiers.toString()+"\n";
        result += "locals: "+locals.toString()+"\n";
        result += "dates: "+dates.toString()+"\n";
        result += "values: "+values.toString()+"\n";
        result += "userEvents: "+userEvents.toString()+"\n";
        result += "typeEvents: "+typeEvents.toString()+"\n";
        result += "numConsumers: "+consumerNames.size()+"\n";
        result += "consumerNames: "+consumerNames.toString()+"\n";
        result += "#########################\n";
        return result;
    }
}
