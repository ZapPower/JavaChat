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
    private static final List<ChatRoom> rooms = Collections.synchronizedList(new ArrayList<ChatRoom>());

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running.");

            PrintWriter clientOut;
            BufferedReader clientIn;

            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Client connected: " + clientSocket);
                    
                // Create input and output streams
                try {
                    clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                } catch (IOException e) {
                    System.out.println("Error in client connection:");
                    e.printStackTrace();
                    // Try to accept clients again. Do not complete loop
                    continue;
                }

                // Request the chat room to be connected to
                int roomNum = requestChatRoom(clientOut, clientIn);
                while (!isValidRoom(roomNum)) {
                    clientOut.println("ROOMSELECTERR");
                    roomNum = requestChatRoom(clientOut, clientIn);
                }

                // Connect client to requested room
                connectClient(clientSocket, roomNum, clientOut, clientIn);
            }
        } catch (IOException e) {
            System.out.println("Error in server");
            e.printStackTrace();
        }
    }

    private static int requestChatRoom(PrintWriter out, BufferedReader in) throws IOException {
        out.println("CHATROOMS");
        for (ChatRoom room : rooms) {
            out.println(room.getChatNum());
            out.println("CONNECTED");
            for (String user : room.getConnectedUsers()) {
                out.println(user);
            }
            out.println("ENDCONNECTED");
        }
        return Integer.parseInt(in.readLine());
    }

    private static boolean isValidRoom(int roomNum) {
        for (ChatRoom room : rooms) {
            if (room.getChatNum() == roomNum) {
                return true;
            }
        }
        return false;
    }

    private static void connectClient(Socket clientSocket, int roomNum, PrintWriter out, BufferedReader in) {
        for (ChatRoom room : rooms) {
            if (room.getChatNum() == roomNum) {
                room.addClient(clientSocket, out, in);
                return;
            }
        }
    }
}