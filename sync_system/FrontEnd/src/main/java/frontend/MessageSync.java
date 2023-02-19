package frontend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class MessageSync {

    // class used to sync the messages between servers on the front-end

    private String user;

    private ArrayList<Long> identifiers;
    private ArrayList<String> locals;
    private ArrayList<String> dates;
    private ArrayList<Integer> values;

    private ArrayList<String> userEvents;
    private ArrayList<String> typeEvents;

    private ArrayList<String> consumerNames;


    public String getUser() {return this.user;}
    public void setUser(String user) {this.user = user;}

    public ArrayList<Long> getIdentifiers() {return this.identifiers;}
    public void setIdentifiers(ArrayList<Long> identifiers) {this.identifiers = identifiers;}

    public ArrayList<String> getLocals() {return  this.locals;}
    public void setLocals(ArrayList<String> locals) {this.locals = locals;}

    public ArrayList<String> getDates() {return this.dates;}
    public void setDates(ArrayList<String> dates) {this.dates = dates;}

    public ArrayList<Integer> getValues() {return this.values;}
    public void setValues(ArrayList<Integer> values) {this.values = values;}

    public ArrayList<String> getUserEvents() {return  this.userEvents;}
    public void setUserEvents(ArrayList<String> userEvents) {this.userEvents = userEvents;}

    public ArrayList<String> getTypeEvents() {return this.typeEvents;}
    public void setTypeEvents(ArrayList<String> typeEvents) {this.typeEvents = typeEvents;}

    public ArrayList<String> getConsumerNames() {return this.consumerNames;}
    public void setConsumerNames(ArrayList<String> memberNames) {this.consumerNames = memberNames;}


    public String getJsonFormat() {
        Gson json = new GsonBuilder().create();
        String jsonStr = json.toJson(this);
        return jsonStr;
    }

    public MessageSync getClassFromJson(String jsonStr) {
        Gson json = new GsonBuilder().create();
        MessageSync obj = json.fromJson(jsonStr, MessageSync.class);
        return obj;
    }
}
