package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static final int PORT = 1234;
    private static final List<ChatRoom> rooms = Collections.synchronizedList(new ArrayList<>());

    // TODO: allow clients to exit ChatRoom to enter a different one - not sure how to implement atm
    // TODO: Add comments to all methods and further explain process

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + serverSocket.getLocalPort());

            ChatRoom startupRoom = new ChatRoom(1);
            rooms.add(startupRoom);

            ChatRoom startupRoom2 = new ChatRoom(2);
            rooms.add(startupRoom2);

            // Begin room cleanup routine. Will remove all unused rooms except rooms 1 and 2 every 10 minutes
            new Thread(new RoomCleanupRoutine()).start();

            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Client connected: " + clientSocket);
                
                // Dedicate new thread to client room connection process
                ClientConnector c = new ClientConnector(clientSocket);
                new Thread(c).start();
            }
        } catch (IOException e) {
            System.out.println("Error in server");
            e.printStackTrace();
        }
    }

    /**
     * Requests to the user which chat room to be connected to. Additionally sends all currently conneted users to each chat room
     * 
     * @param out The output stream for the client
     * @param in The input stream for the client
     * @return The chat number to be connected to
     * @throws IOException If there is an error in fetching user input
     */
    private static int requestChatRoom(PrintWriter out, BufferedReader in) throws IOException {
        out.println("CHATROOMS");
        for (ChatRoom room : rooms) {
            for (String user : room.getConnectedUsers()) {
                out.println(user);
            }
            out.println("ENDCONNECTED");
        }
        out.println("ENDCHATROOMS");
        return Integer.parseInt(in.readLine());
    }

    /**
     * Requests the client if they would like to create a new chat room
     * 
     * @param out The output stream for the client
     * @param in The input stream for the client
     * @return The client's response
     * @throws IOException If there is an error in fetching user input
     */
    private static boolean requestChatCreation(PrintWriter out, BufferedReader in) throws IOException {
        out.println("CREATEROOM?");
        String response = in.readLine();
        return Boolean.parseBoolean(response);
    }

    /**
     * Checks to see if a generated room number is valid
     * 
     * @param roomNum The room number
     * @return True if valid, false otherwise
     */
    private static boolean isValidRoom(int roomNum) {
        for (ChatRoom room : rooms) {
            if (room.getChatNum() == roomNum) {
                return true;
            }
        }
        return false;
    }

    /**
     * Connects a client to a given room
     * 
     * @param clientSocket The client's socket connection
     * @param roomNum The room number to be transferred to
     * @param out The output stream of the client
     * @param in The input stream of the client
     */
    private static void connectClient(Socket clientSocket, int roomNum, PrintWriter out, BufferedReader in) {
        for (ChatRoom room : rooms) {
            if (room.getChatNum() == roomNum) {
                room.addClient(clientSocket, out, in);
                return;
            }
        }
    }

    /**
     * Generates a new chat room number, unused by any other chat room
     * 
     * @return A new, unused chat room number
     */
    private static int getNewChatRoomNum() {
        int count = 1;
        try {
            while (rooms.get(count - 1).getChatNum() == count) {
                count++;
            }
        } catch (IndexOutOfBoundsException e) {}
        return count;
    }

    /**
     * Goes through the initial connection process for a client
     */
    private static class ClientConnector implements Runnable {
        private Socket clientSocket;

        public ClientConnector(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            PrintWriter clientOut;
            BufferedReader clientIn;

            // Create input and output streams
            try {
                clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Error in client connection:");
                e.printStackTrace();
                // Try to accept clients again
                return;
            }

            // Request new chat creation ?
            try {
                if (requestChatCreation(clientOut, clientIn)) {
                    int roomNum = getNewChatRoomNum();
                    ChatRoom newChat = new ChatRoom(roomNum);
                    rooms.add(newChat);
                    System.out.println("New room created: " + roomNum);
    
                    // Connect client to created chat room and accept new clients
                    connectClient(clientSocket, roomNum, clientOut, clientIn);
                    return;
                }
            } catch (IOException e) {
                System.out.println("Error in client chat creation request:");
                e.printStackTrace();
                return;
            }
            

            // Request the chat room to be connected to
            int roomNum = 1;
            try {
                roomNum = requestChatRoom(clientOut, clientIn);
                while (!isValidRoom(roomNum)) {
                    clientOut.println("ROOMSELECTERR");
                    roomNum = requestChatRoom(clientOut, clientIn);
                }
            } catch (IOException e) {
                System.out.println("Error in requesting chat room to be connected to:");
                e.printStackTrace();
                return;
            }
            

            // Connect client to requested room
            connectClient(clientSocket, roomNum, clientOut, clientIn);
        }
    }

    /**
     * Routine to clean up unused chat rooms
     */
    private static class RoomCleanupRoutine implements Runnable {
        public void run() {
            Timer time = new Timer();
            RoomCleanup task = new RoomCleanup();
            // Clean up rooms every 10 minutes
            time.schedule(task, 0, 600000);
        }
    }

    /**
     * Task for room cleanup routine
     */
    private static class RoomCleanup extends TimerTask {
        public void run() {
            System.out.println("Running chat room cleanup...");
            for (int i = 2; i < rooms.size(); i++) {
                if (rooms.get(i).getNumConnected() == 0) {
                    rooms.remove(i);
                }
            }
        }
    }
}