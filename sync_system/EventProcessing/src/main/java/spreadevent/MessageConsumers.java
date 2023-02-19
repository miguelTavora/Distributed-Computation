package spreadevent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class MessageConsumers {

    private ArrayList<String> consumerNames;

    public ArrayList<String> getConsumerNames() {return this.consumerNames;}
    public void setConsumerNames(ArrayList<String> consumerNames) {this.consumerNames = consumerNames;}


    public String getJsonFormat() {
        Gson json = new GsonBuilder().create();
        String jsonStr = json.toJson(this);
        return jsonStr;
    }

    public MessageConsumers getClassFromJson(String jsonStr) {
        Gson json = new GsonBuilder().create();
        MessageConsumers obj = json.fromJson(jsonStr, MessageConsumers.class);
        return obj;
    }
}
