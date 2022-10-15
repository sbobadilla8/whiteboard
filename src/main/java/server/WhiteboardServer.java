package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.DrawingPanel;

import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class WhiteboardServer {
    private ServerSocket whiteboardSocket;

    private ConcurrentHashMap<String, Socket> clientList;

    private String fileName;

    private DrawingPanel whiteboard;


    public WhiteboardServer(String fileName, DrawingPanel whiteboard) throws IOException {
        this.clientList = new ConcurrentHashMap<>();
        this.fileName = fileName;
        this.whiteboard = whiteboard;
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try {
            this.whiteboardSocket = factory.createServerSocket(3000);
            System.out.println("Server initialized, waiting for client connection...");

            // Extremely cursed / 10
            new Thread(() -> {
                // Wait for connections.
                while(true){
                    Socket client = null;
                    try {
                        client = whiteboardSocket.accept();
                        client.setKeepAlive(true);
                        System.out.println("Client applying for connection!");

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

    public BufferedImage getImage(String fileName) throws IOException {
        return ImageIO.read(new File(fileName));
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

            // The welcome message consists of two parts - the first is the initial canvas size, the second is the canvas png
            BufferedImage image = ImageIO.read(new File(this.whiteboard.getFileName()));

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);

            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            output.write(size);
            output.write(byteArrayOutputStream.toByteArray());
            output.flush();

            //output.writeUTF("Successfully added peer: " + clientName);

            Boolean isPeerTerminated = false;
            // Receive more data..
            while (!isPeerTerminated) {
                if (input.available() > 0) {
                    isPeerTerminated = parseCommand(input, output);
                }
            }
//            System.out.println("Terminating client connection: " + clientName);
//            this.clientList.remove(clientName);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private Boolean parseCommand(DataInputStream input, DataOutputStream output) throws IOException, ParseException {
        // The JSON Parser
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = (JSONObject) parser.parse(readMessage(input));
        System.out.println("COMMAND RECEIVED: " + command.toJSONString());

        int result = 0;
        String drawMode = command.get("draw-mode").toString();
        int rgbValue = Integer.parseInt(command.get("paint-color").toString());
        float lineWidth = Float.parseFloat(command.get("line-width").toString());
        String firstPoints = command.get("first-point").toString();
        JSONObject firstPoint = (JSONObject) parser.parse(firstPoints);
        int x1 = Integer.parseInt(firstPoint.get("x").toString());
        int y1 = Integer.parseInt(firstPoint.get("y").toString());
        Point first = new Point(x1, y1);
        String secondPoints = command.get("second-point").toString();
        JSONObject secondPoint = (JSONObject) parser.parse(secondPoints);
        int x2 = Integer.parseInt(secondPoint.get("x").toString());
        int y2 = Integer.parseInt(secondPoint.get("y").toString());
        Point second = new Point(x2, y2);
        String textInput = command.get("text-input").toString();
        this.whiteboard.draw(drawMode, rgbValue,  lineWidth,  first,  second, textInput);

        return false;
    }

    public String readMessage(DataInputStream input) throws IOException {
        return input.readUTF();
    }

    public void writeMessage(DataOutputStream output, String message) throws IOException {
        output.writeUTF(message);
    }
}
