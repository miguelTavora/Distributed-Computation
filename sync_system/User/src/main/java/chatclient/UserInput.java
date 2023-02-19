package chatclient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class UserInput {

    private final String ACCESS = "admin";
    private Scanner input;
    private String clientName;

    public UserInput() {
        input = new Scanner(System.in);
    }

    public String getClientName() {
        return this.clientName;
    }


    public void getNickname() {
        System.out.print("Enter your nickName: ");
        String result = input.nextLine();
        this.clientName = result;
    }

    public int getSelectedNumber() {
        System.out.println("Select one of the below options:");
        System.out.println("1 - Max value of velocity");
        System.out.println("2 - Mean value of velocity");
        System.out.println("3 - Mean value of velocity between two dates");
        System.out.println("4 - Number of consumers");
        System.out.println("5 - Execute a consumer and show all consumers names");
        System.out.println("6 - Exit");

        while(true) {
            String iptUser = input.nextLine();
            if (iptUser.matches("[0-9]+")) {
                int number = Integer.parseInt(iptUser);
                if(number > 0 && number < 7) return number;

                else System.out.println("Not a valid number");
            }
            else System.out.println("Not a number");
        }
    }

    private boolean validDate(String date) {
        DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            System.out.println("Date is not correct");
            return false;
        }
    }

    public String[] getValidDate() {
        System.out.println("Begin Date (on format dd-MM-yyyy): ");
        String beginDate = "";
        while(true) {
            beginDate = input.nextLine();
            if(validDate(beginDate)) break;
            else System.out.println("Not a valid date!Try again");
        }

        System.out.println("End Date (on format dd-MM-yyyy):");
        String endDate = "";
        while(true) {
            endDate = input.nextLine();
            if(validDate(endDate)) break;
            else System.out.println("Not a valid date!Try again");
        }
        return new String[] {beginDate, endDate};
    }

    public boolean getValidUser(String username) {
        if(username.equals(ACCESS)) return true;
        else System.out.println("You don't have permission to do that, only ("+ACCESS+")\n");
        return false;
    }
}
