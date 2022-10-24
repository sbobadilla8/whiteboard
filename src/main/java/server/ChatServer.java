package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.Chat;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private ServerSocket chatSocket;
    private ConcurrentHashMap<String, Socket> clientList;
    private ConcurrentHashMap<Integer, String> unsentChats;

    private Chat chat;
    private int port;

    public ChatServer(Chat chat) throws IOException {
        this.clientList = new ConcurrentHashMap<>();
        this.chat = chat;
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try {
            this.chatSocket = factory.createServerSocket(0);
            System.out.println("Chat server initialized on port " + getPort() + ", waiting for client connection...");

            // Extremely cursed / 10
            new Thread(() -> {
                // Wait for connections.
                while (true) {
                    Socket client = null;
                    try {
                        client = chatSocket.accept();
                        client.setKeepAlive(true);
                        System.out.println("Client attempting subscription ...");

                        // Start a new thread for a connection
                        Socket finalClient = client;
                        Thread t = new Thread(() -> serveClient(finalClient));
                        t.start();
                    } catch (IOException e) {
                        System.out.println("Unable to add client to chat list.");
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("An error occurred while starting the main chat thread.");
        }
    }


    public void serveClient(Socket client) {
        try (Socket clientSocket = client) {

            // The JSON Parser
            JSONParser parser = new JSONParser();
            // Input stream
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            // Output Stream
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            String clientName = readMessage(input);
            this.clientList.put(clientName, client);

            output.writeUTF("Client " + clientName + " successfully subscribed to chat");
            output.flush();
            boolean isPeerTerminated = false;
            // Receive more data..
            while (!isPeerTerminated) {
                if (input.available() > 0) {
                    isPeerTerminated = parseCommand(input);
                }
            }
        } catch (IOException | ParseException e) {
            System.out.println("Chat server connection unexpectedly terminated.");
        }
    }

    private Boolean parseCommand(DataInputStream input) throws IOException, ParseException {
        // The JSON Parser
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = (JSONObject) parser.parse(readMessage(input));

        if (command.containsKey("disconnected")) {
            this.clientList.remove(command.get("disconnected").toString());
            // Since client has disconnected, we return true
            return true;
        }

        String username = command.get("username").toString();
        String message = command.get("message").toString();

        this.chat.addReceivedMessage(username, message);

        this.clientList.forEach((user, conn) -> {
            if (!user.equals(username)) {
                try {
                    DataOutputStream output = new DataOutputStream(conn.getOutputStream());
                    output.writeUTF(command.toJSONString());
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Client disconnected");
                    this.clientList.remove(user);
                }
            }
        });
        return false;
    }

    public void multicastMessage(JSONObject messageCommand) {
        this.clientList.forEach((user, conn) -> {
            System.out.println(user);
            if (!messageCommand.containsKey("username")) {
                try {
                    DataOutputStream output = new DataOutputStream(conn.getOutputStream());
                    output.writeUTF(messageCommand.toJSONString());
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Client disconnected");
                    this.clientList.remove(user);
                }
            }
        });
    }

    public void kickUser(String username) {
        Socket client = this.clientList.get(username);
        try {
            DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
            JSONObject kickedMessage = new JSONObject();
            kickedMessage.put("kicked", "You have been kicked");
            outputStream.writeUTF(kickedMessage.toJSONString());
            outputStream.flush();
            client.close();
            this.clientList.remove(username);
        } catch (IOException e) {
            System.out.println("An error occurred while removing a client.");
        }
    }

    public String readMessage(DataInputStream input) throws IOException {
        return input.readUTF();
    }

    public int getPort(){
        return this.chatSocket.getLocalPort();
    }

}
