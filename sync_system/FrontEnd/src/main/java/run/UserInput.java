package run;

import java.util.Scanner;

public class UserInput {

    private Scanner sc;

    public UserInput() {
        sc = new Scanner(System.in);

    }

    public String getValidMemberName(String group) {
        while(true) {
            System.out.println("Member name "+group+": ");
            String userInput = sc.nextLine();
            if(userInput.matches("[a-zA-Z]+")) {
                return userInput;
            }
            System.out.println("Not valid name (it can only contains letters without ç or â)");
        }
    }

}
