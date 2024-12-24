package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private static String SERVER_ADDRESS;
    private static int SERVER_PORT;
    private static Socket clientSocket = null;
    private static PrintWriter clientOut = null;
    private static BufferedReader clientIn = null;
    private static boolean createChatRoomFlag = false;
    private static final Scanner scanner = new Scanner(System.in);

    // TODO: Comment everything!
    // TODO: Handle client sending chat messages
    // TODO: Create buffer to watch input to not delete in progress input on incoming message

    public static void main(String[] args) {
        System.out.println("Welcome to JavaChat!");

        SERVER_ADDRESS = getServerAddress();
        SERVER_PORT = getServerPort();
        setRoomCreationFlag();

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

    public static void setRoomCreationFlag() {
        System.out.println("Create a room? (y/n)");
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("y")) {
            createChatRoomFlag = true;
        } else {
            createChatRoomFlag = false;
        }
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
            switch (response) {
                case "CREATEROOM?":
                    handleCreateRoom();
                    break;
                case "GETUSER":
                    handleGetUser();
                    break;
                case "CHATROOMS":
                    handleChatRoomRead();
                    break;
                case "ROOMSELECTERR":
                    System.out.println("Requested chat room was not valid. Please try again.");
                    break;
                case "CONNECTED":
                    new Thread(new UserInputHandler()).start();
                    break;
                default:
                    handleChatMessage(response);
                    break;
            }
        }

        public void handleChatMessage(String message) {
            System.out.print("\033[2K\r"); // Clear the current line (": ")
            System.out.println(message);
            System.out.print(": ");
        }

        public void handleCreateRoom() {
            clientOut.println(String.valueOf(createChatRoomFlag));
        }

        public void handleGetUser() {
            System.out.println("Please enter your username:");
            String username = scanner.nextLine();
            clientOut.println(username);
        }

        public void handleChatRoomRead() {
            ArrayList<String[]> rooms = new ArrayList<>();
            ArrayList<String> userBuffer = new ArrayList<>();

            String serverResponse;
            try {
                serverResponse = clientIn.readLine();
                while (!serverResponse.equals("ENDCHATROOMS")) {
                    if (serverResponse.equals("ENDCONNECTED")) {
                        String[] users = userBuffer.toArray(new String[0]);
                        rooms.add(users);
                        userBuffer.clear();
                    } else {
                        userBuffer.add(serverResponse);
                    }
                    serverResponse = clientIn.readLine();
                }
            } catch (IOException e) {
                System.out.println("Error in fetching chat rooms:");
                e.printStackTrace();
            }
            
            displayAvailableChatRooms(rooms);
            handleRoomSelection();
        }

        public void displayAvailableChatRooms(ArrayList<String[]> rooms) {
            System.out.println("Available Chat Rooms:");
            for (int i = 0; i < rooms.size(); i++) {
                String[] users = rooms.get(i);
                System.out.println("Room " + (i + 1) + ":");
                for (String user : users) {
                    System.out.println("\t" + user);
                }
            }
        }

        public void handleRoomSelection() {
            System.out.println("Please select a valid chat room:");
            String clientResponse = scanner.nextLine();
            while (!isInt(clientResponse)) {
                System.out.println("Invalid chat room. Please try again:");
                clientResponse = scanner.nextLine();
            }
            clientOut.println(clientResponse);
        }

        private boolean isInt(String s) {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private static class UserInputHandler implements Runnable {
        @Override
        public void run() {
            String userInput;
            while (true) {
                userInput = scanner.nextLine();
                // Before message sent, remove line
                // Clear the previous line using ANSI escape codes
                System.out.print("\033[1A"); // Move cursor up one line
                System.out.print("\033[2K"); // Clear the line

                clientOut.println(userInput);
                
                // Add text thingy for user
                System.out.print(": ");
            }
        }
    }
}
