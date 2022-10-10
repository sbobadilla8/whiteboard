package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class WhiteboardServer {
    private ServerSocket whiteboardSocket;

    private ConcurrentHashMap<String, Socket> clientList;

    private String fileName;
    public WhiteboardServer(int port, String fileName) throws IOException {
        this.clientList = new ConcurrentHashMap<>();
        this.fileName = fileName;
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try{
            this.whiteboardSocket = factory.createServerSocket(port);
            System.out.println("Server initialized, waiting for client connection...");

            // Wait for connections.
            while(true){
                Socket client = whiteboardSocket.accept();
                client.setKeepAlive(true);
                System.out.println("Client applying for connection!");

                // Start a new thread for a connection
                Thread t = new Thread(() -> serveClient(client));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(String fileName) throws IOException {
        return ImageIO.read(new File(fileName));
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

            output.writeUTF("Successfully added peer: "+ clientName);

            Boolean isPeerTerminated = false;
            // Receive more data..
            while(!isPeerTerminated){
                if(input.available() > 0){
                    isPeerTerminated = parseCommand(input, output);
                }
            }
            System.out.println("Terminating client connection: "+ clientName);
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

        if (command.get("command_name").equals("Math"))
        {
            //Math math = new Math();
            //Integer firstInt = Integer.parseInt(command.get("first_integer").toString());
            //Integer secondInt = Integer.parseInt(command.get("second_integer").toString());

            switch((String) command.get("method_name"))
            {
                case "client_join":
                    System.out.println("Server parsing client_join request");
                    // TODO: Implement sharing of image file to new client
                    //result = math.add(firstInt,secondInt);
                    break;
                case "whiteboard_line":
                    System.out.println("Server parsing whiteboard_line request");
                    //result = math.multiply(firstInt,secondInt);
                    // gets from peer
                    // multicast
                    break;
                case "whiteboard_rectangle":
                    System.out.println("Server parsing whiteboard_rectangle request");
                    //result = math.multiply(firstInt,secondInt);
                    // gets from peer
                    // multicast
                    break;
                case "whiteboard_circle":
                    System.out.println("Server parsing whiteboard_circle request");
                    //result = math.multiply(firstInt,secondInt);
                    // gets from peer
                    // multicast
                    break;
                case "whiteboard_triangle":
                    System.out.println("Server parsing whiteboard_triangle request");
                    //result = math.multiply(firstInt,secondInt);
                    // gets from peer
                    // multicast
                    break;
                case "whiteboard_freehand":
                    System.out.println("Server parsing whiteboard_freehand request");
                    //result = math.multiply(firstInt,secondInt);
                    // gets from peer
                    // multicast
                    break;
                case "whiteboard_text":
                    System.out.println("Server parsing whiteboard_text request");
                    //result = math.multiply(firstInt,secondInt);
                    // gets from peer
                    // multicast
                    break;
                case "client_remove":
                    System.out.println("Server parsing client_remove request");
                    return true;
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
