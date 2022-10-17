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
        this.port = 3001;
        this.chat = chat;
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try {
            this.chatSocket = factory.createServerSocket(this.port);
            System.out.println("Chat server initialized on port " + this.port + ", waiting for client connection...");

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
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
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
            System.out.println("CLIENT: " + clientName);

            output.writeUTF("Client " + clientName + " successfully subscribed to chat");

            boolean isPeerTerminated = false;
            // Receive more data..
            while (!isPeerTerminated) {
                if (input.available() > 0) {
                    isPeerTerminated = parseCommand(input);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private Boolean parseCommand(DataInputStream input) throws IOException, ParseException {
        // The JSON Parser
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = (JSONObject) parser.parse(readMessage(input));

        String username = command.get("username").toString();
        String message = command.get("message").toString();

        this.chat.addReceivedMessage(username, message);

        this.clientList.forEach((user, conn) -> {
            if (!user.equals(username)){
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

    public void multicastMessage(JSONObject messageCommand){
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

    public void kickUser(String username){
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
            throw new RuntimeException(e);
        }
    }

    public String readMessage(DataInputStream input) throws IOException {
        return input.readUTF();
    }

}
