package iotcar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Input {

    // this class is used to wait to input of the user
    // select manual or auto generation
    // when is manual to verify the inputted data

    private final String[] districts = {"Aveiro", "Beja", "Braga", "Castelo Branco",
            "Coimbra", "Évora", "Faro", "Guarda", "Leiria",
            "Lisboa", "Portalegre", "Porto", "Santarém",
            "Setúbal", "Viana do Castelo", "Vila Real",
            "Viseu"};


    public int selectOption() {
        System.out.println("Select:");
        System.out.println("1 - auto");
        System.out.println("2 - manual");
        System.out.println("0 - Exit");

        Scanner sc = new Scanner(System.in);
        while(true) {
            String input = sc.nextLine();
            if(input.matches("[0-9]+")) {
                int value = Integer.parseInt(input);
                if(value == 1 || value == 2) return value;
                else if(value == 0) System.exit(0);
                else System.out.println("Not a valid number");
            }
            else System.out.println("Not a number");
        }
    }

    public long getValidSid() {
        System.out.println("Select a number between (0 - 99999):");

        Scanner sc = new Scanner(System.in);
        while(true) {
            String input = sc.nextLine();
            if(input.matches("[0-9]+")) {
                long value = Long.parseLong(input);
                if(value > -1 && value < 1_000_000) return value;
                else System.out.println("Not a valid number");
            }
            else System.out.println("Not a number");
        }
    }

    public String getValidLocal() {
        System.out.println("Select a local:");
        for(int i = 0; i < districts.length; i++) {
            System.out.println((1+i)+" - "+districts[i]);
        }
        Scanner sc = new Scanner(System.in);
        while(true) {
            String input = sc.nextLine();
            if(input.matches("[0-9]+")) {
                int value = Integer.parseInt(input);
                if(value > 0 && value < districts.length+1) return districts[value-1];
                else System.out.println("Not a valid number");
            }
            else System.out.println("Not a number");
        }
    }

    public String isValidDate() {
        System.out.println("Select a date on format (dd-MM-yyyy): ");
        DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);

        Scanner sc = new Scanner(System.in);
        String beginDate = "";
        while(true) {
            beginDate = sc.nextLine();
            try {
                sdf.parse(beginDate);
                break;
            } catch (ParseException e) {
                System.out.println("Date is not correct");
            }
        }
        return beginDate;
    }

    public int getValidValue() {
        System.out.println("Select a number between (30 - 150):");

        Scanner sc = new Scanner(System.in);
        while(true) {
            String input = sc.nextLine();
            if(input.matches("[0-9]+")) {
                int value = Integer.parseInt(input);
                if(value > 29 && value < 151) return value;
                else System.out.println("Not a valid number");
            }
            else System.out.println("Not a number");
        }
    }
}
