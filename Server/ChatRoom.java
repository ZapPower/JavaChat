package Server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

/**
 * A chat room that separates clients
 */
public class ChatRoom {
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
    private int chatNum;

    /**
     * Creates a new chat room with the specified number
     * 
     * @param chatNum The number for the chat room
     */
    public ChatRoom(int chatNum) {
        this.chatNum = chatNum;
    }

    /**
     * Adds a client to this room
     * 
     * @param clientSocket The client's socket connection
     * @param out The client's output stream
     * @param in The client's input stream
     */
    public void addClient(Socket clientSocket, PrintWriter out, BufferedReader in) {
        ClientHandler newClient = new ClientHandler(clientSocket, this, out, in);
        clients.add(newClient);
        new Thread(newClient).start();
    }

    /**
     * Broadcasts a message to all the clients in the chat room
     * 
     * @param message Message to be sent
     */
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage("[" + client.getUsername() + "]: " + message);
        }
    }

    /**
     * Returns a list of the usernames of connected clients
     * 
     * @return An array of usernames
     */
    public String[] getConnectedUsers() {
        String[] connected = new String[clients.size()];
        int count = 0;
        for (ClientHandler client : clients) {
            connected[count] = client.getUsername();
            count++;
        }
        return connected;
    }

    /**
     * Removes a client from the chat room
     * 
     * @param client The client to be removed
     */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Returns the chat number of this chat room
     * 
     * @return The chat room number
     */
    public int getChatNum() {
        return this.chatNum;
    }
}
