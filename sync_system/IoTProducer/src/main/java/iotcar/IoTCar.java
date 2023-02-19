package iotcar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

public class IoTCar {

    // this class is responsible to generate automatically the values
    // of the messages

    private int MAX_VALUE = 99999;
    private final String[] districts = {"Aveiro", "Beja", "Braga", "Castelo Branco",
            "Coimbra", "Évora", "Faro", "Guarda", "Leiria",
            "Lisboa", "Portalegre", "Porto", "Santarém",
            "Setúbal", "Viana do Castelo", "Vila Real",
            "Viseu"};

    private long identifier;
    private String local;


    public IoTCar() {
        // generate a random district from the array districts
        Random rd = new Random();
        local = districts[rd.nextInt(districts.length)];

        // generate a random long identifier and random district
        identifier = (int) ((Math.random() * MAX_VALUE));
        System.out.println("Identifier of IoTCar: "+identifier);
    }

    public Message generateRandomMessage() {
        String date = null;
        // to get a valid date
        while(date == null) {
            date = generateRandomDate();
        }
        int speed = generateRandomNumber(150, 30);
        return new Message(identifier, local, date, speed);
    }

    public int generateRandomNumber(int max, int min) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private String generateRandomDate() {
        int day = generateRandomNumber(30, 1);
        int month = generateRandomNumber(12, 10);

        return isValidDate(day, month);
    }

    private String isValidDate(int day, int month) {
        String date  = day+"-"+month+"-2021";

        DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return date;
        } catch (ParseException e) {
            System.out.println("Date is not correct");
            return null;
        }
    }


}
