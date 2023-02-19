package chatclient;

import chat.ChatMessage;

import java.util.Scanner;

public class UserInfo {

    private Scanner input;
    private String clientName;

    public UserInfo(String serverIP, int serverPort) {
        System.out.println("Server IP: "+serverIP+", port: "+serverPort);
        input = new Scanner(System.in);
    }

    public String getClientName() {
        return this.clientName;
    }

    public String obtainWrittenMessage() {
        return input.nextLine();
    }

    public String getNickname() {
        System.out.print("Enter your nickName: ");
        String result = input.nextLine();
        this.clientName = result;
        return result;
    }
}
