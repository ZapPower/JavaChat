package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles client connections within chatrooms
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean waitingForUsername;
    private ChatRoom room;

    /**
     * Creates a new client handler
     * @param clientSocket The client's socket connection
     * @param room The ChatRoom reference for this client
     * @param out The client's out stream
     * @param in The client's in stream
     */
    public ClientHandler(Socket clientSocket, ChatRoom room, PrintWriter out, BufferedReader in) {
        this.clientSocket = clientSocket;
        this.waitingForUsername = true;
        this.room = room;

        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {
        try {
            // Request username
            this.username = requestUsername();
            this.room.broadcast(this.username + " has connected.");
            // Send currently connected users to the client
            sendConnected();
            // Let client know now accepting messages
            this.out.println("CONNECTED");

            String input;

            // Read continuous input from client
            while ((input = in.readLine()) != null) {
                System.out.println("[" + this.room.getChatNum() + "][" + this.username + "]: " + input);
                this.room.broadcast("[" + this.username + "]: " + input);
            }

            // Client has disconnected
            this.in.close();
            this.clientSocket.close();
            this.out.close();

            this.room.removeClient(this);
            this.room.broadcast(username + " has disconnected");

        } catch (IOException e) {
            try {
                this.clientSocket.close();
                this.in.close();
                this.out.close();
            } catch (IOException err) {
                System.out.println("Error in disconnecting client:");
                err.printStackTrace();
            }

            this.room.removeClient(this);
            this.room.broadcast(username + " has disconnected.");
            
        }
    }

    /**
     * Requests the user to enter a username (handled by client)
     * 
     * @return The user's inputted username
     * @throws IOException If error occurs in request
     */
    private String requestUsername() throws IOException {
        out.println("GETUSER");
        String user = in.readLine();
        this.waitingForUsername = false;
        return user;
    }

    /**
     * Sends a message to this client if this client is not waiting for a username
     * 
     * @param message message to be sent
     */
    public void sendMessage(String message) {
        if (!this.waitingForUsername) {
            out.println(message);
        }
    }

    /**
     * Returns the client's username
     * 
     * @return client's username, "???" if not chosen
     */
    public String getUsername() {
        if (this.waitingForUsername) {
            return "???";
        }
        return this.username;
    }

    /**
     * Sends the currently connected users to the client
     */
    private void sendConnected() {
        out.println("Currently connected users:");
        for (String user : room.getConnectedUsers()) {
            out.println(user);
        }
    }
}
