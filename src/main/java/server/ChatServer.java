package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private ServerSocket chatSocket;
    private ConcurrentHashMap<String, Socket> clientList;
    private ConcurrentHashMap<Integer, String> unsentChats;
    private int port;

    public ChatServer() throws IOException {
        this.port = 3001;
        this.clientList = new ConcurrentHashMap<>();
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try{
            this.chatSocket = factory.createServerSocket(this.port);
            System.out.println("Chat server initialized on port "+this.port+", waiting for client connection...");

            // Extremely cursed / 10
            new Thread(() -> {
                // Wait for connections.
                while(true){
                    Socket client = null;
                    try {
                        client = chatSocket.accept();
                        client.setKeepAlive(true);
                        System.out.println("Client attempting subscription ...");

                        // Start a new thread for a connection
                        Socket finalClient = client;
                        Thread t = new Thread(() -> serveClient(finalClient));
                        t.start();
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serveClient(Socket client) {
        try(Socket clientSocket = client)
        {

            // The JSON Parser
            JSONParser parser = new JSONParser();
            // Input stream
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            // Output Stream
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            String clientName = readMessage(input);
            this.clientList.put(clientName, client);
            System.out.println("CLIENT: "+clientName);

            output.writeUTF("Client "+ clientName+" successfully subscribed to chat");

            Boolean isPeerTerminated = false;
            // Receive more data..
            while(!isPeerTerminated){
                if(input.available() > 0){
                    isPeerTerminated = parseCommand(input, output);
                }
            }
            System.out.println("Unsubscribing client: "+ clientName);
            this.clientList.remove(clientName);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private Boolean parseCommand(DataInputStream input, DataOutputStream output) throws IOException, ParseException {
        // The JSON Parser
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = (JSONObject) parser.parse(readMessage(input));
        System.out.println("COMMAND RECEIVED: "+command.toJSONString());

        int result = 0;

        if(command.containsKey("command_name")){
            System.out.println("IT HAS A COMMAND NAME");
        }

        if (command.get("command_name").equals("Chat"))
        {
            //Math math = new Math();
            //Integer firstInt = Integer.parseInt(command.get("first_integer").toString());
            //Integer secondInt = Integer.parseInt(command.get("second_integer").toString());

            switch((String) command.get("method_name"))
            {
                case "chat_message":
                    System.out.println("Server parsing chat_message request");
                    // TODO: Implement sharing of image file to new client
                    //result = math.add(firstInt,secondInt);
                    break;
                default:
                    try
                    {
                        throw new Exception();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
            JSONObject resObj = new JSONObject();
            resObj.put("result", result);

            writeMessage(output, resObj.toJSONString());
        }
        // TODO Auto-generated method stub
        return false;
    }

    public String readMessage(DataInputStream input) throws IOException {
        return input.readUTF();
    }

    public void writeMessage(DataOutputStream output, String message) throws IOException {
        output.writeUTF(message);
    }
}
