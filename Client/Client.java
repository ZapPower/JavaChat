package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static String SERVER_ADDRESS;
    private static int SERVER_PORT;
    private static Socket clientSocket = null;
    private static PrintWriter clientOut = null;
    private static BufferedReader clientIn = null;
    private static boolean createChatRoom = false;
    private static final Scanner scanner = new Scanner(System.in);

    // TODO: Comment everything!

    public static void main(String[] args) {
        System.out.println("Welcome to JavaChat!");

        SERVER_ADDRESS = getServerAddress();
        SERVER_PORT = getServerPort();

        connect(SERVER_ADDRESS, SERVER_PORT);
        System.out.println("Connected!");

        ServerHandler sh = new ServerHandler();
        new Thread(sh).start();
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
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error in connection. Please try again");
            SERVER_ADDRESS = getServerAddress();
            SERVER_PORT = getServerPort();
            connect(SERVER_ADDRESS, SERVER_PORT);
        }
    }

    private static class ServerHandler implements Runnable {
        public ServerHandler() {
            setRoomCreationFlag();
        }

        public void setRoomCreationFlag() {
            System.out.println("Create a room? (y/n)");
            String response = scanner.nextLine();
            if (response.equalsIgnoreCase("y")) {
                createChatRoom = true;
            } else {
                createChatRoom = false;
            }
        }

        public void run() {
            try {
                String serverResponse;
                while ((serverResponse = clientIn.readLine()) != null) {
                    handleServerResponse(serverResponse);
                }
            } catch (IOException e) {
                System.out.println("An unknown error has occurred:");
                e.printStackTrace();
            }
        }

        public void handleServerResponse(String response) {
            // TODO: Create handlers for each response
            switch (response) {
                case "CREATEROOM?":
                    break;
                case "GETUSER":
                    break;
                case "CHATROOMS":
                    break;
                case "ROOMSELECTERR":
                    break;
            }
        }
    }
}
