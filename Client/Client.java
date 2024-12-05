package Client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String SERVER_ADDRESS;
    private static int SERVER_PORT;
    private static Socket clientSocket = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to JavaChat!");

        SERVER_ADDRESS = getServerAddress();
        SERVER_PORT = getServerPort();

        connect(SERVER_ADDRESS, SERVER_PORT);
        System.out.println("Connected!");
    }

    private static String getServerAddress() {
        System.out.println("Please enter the server address:");
        return scanner.nextLine();
    }

    private static int getServerPort() {
        System.out.println("Please enter the server port:");
        int port = -1;
        while (port == -1) {
            try {
                port = Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid port. Please try again:");
            }
        }
        return port;
    }

    private static void connect(String address, int port) {
        try {
            System.out.println("Now connecting to server...");
            clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        } catch (IOException e) {
            System.out.println("Error in connection. Please try again");
            SERVER_ADDRESS = getServerAddress();
            SERVER_PORT = getServerPort();
            connect(SERVER_ADDRESS, SERVER_PORT);
        }
    }
}
