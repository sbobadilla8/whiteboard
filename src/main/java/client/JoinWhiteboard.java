package client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.WhiteboardUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Vector;

public class JoinWhiteboard {

    static WhiteboardUI whiteboardUI;
    static Connection conn;
    static Connection chatConn;

    static Vector connectedUsers;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Invalid arguments, retry the command using the syntax: JoinWhiteBoard <ip> <port> <username>");
            return;
        }
        connectedUsers = new Vector<>();
        try {
            conn = new Connection("localhost", 3000);
            JSONObject connectionRequest = new JSONObject();
            conn.setUsername(args[0]);
//            conn.setUsername("user1");
            connectionRequest.put("client-name", conn.getUsername());
            conn.output.writeUTF(connectionRequest.toJSONString());
            conn.output.flush();
            // Read hello from server.
            byte[] sizeArr = new byte[4];
            conn.input.read(sizeArr);
            int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();

            byte[] imageArr = new byte[size];
            conn.input.read(imageArr);
            BufferedImage initialImage = ImageIO.read(new ByteArrayInputStream(imageArr));
            conn.setFilename("remoteWhiteboard.png");
            ImageIO.write(initialImage, "png", new File(conn.getFilename()));
            whiteboardUI = new WhiteboardUI("Whiteboard Client - " + args[0], false, conn);
            whiteboardUI.setVisible(true);

            chatConn = new Connection("localhost", 3001);
            chatConn.output.writeUTF(connectionRequest.toJSONString());
            chatConn.output.flush();
            chatConn.input.readUTF();
            chatConn.setUsername(args[0]);
//            chatConn.setUsername("user1");
            JSONParser parser = new JSONParser();
            JSONObject command = (JSONObject) parser.parse(conn.input.readUTF());
            JSONArray values = (JSONArray) command.get("connected-users");
            whiteboardUI.getChat().setConnection(chatConn);
            connectedUsers.add("Admin");
            connectedUsers.addAll(values);
            whiteboardUI.getConnectedUsers().setListData(connectedUsers);


            Thread t = new Thread(() -> listenServer(conn.socket));
            t.start();

            Thread t2 = new Thread(() -> listenChatServer(chatConn.socket));
            t2.start();

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void listenServer(Socket conn) {
        try (Socket serverSocket = conn) {
            DataInputStream input = new DataInputStream(serverSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(serverSocket.getOutputStream());
            boolean isConnTerminated = false;
            // Listen to drawing commands
            while (!isConnTerminated) {
                if (input.available() > 0) {
                    isConnTerminated = parseCommand(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean parseCommand(DataInputStream input) {
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = null;
        try {
            command = (JSONObject) parser.parse(input.readUTF());
            if (command.containsKey("new-user")){
                connectedUsers.add(command.get("new-user").toString());
                whiteboardUI.getConnectedUsers().setListData(connectedUsers);
                return false;
            }
            if (command.containsKey("username") && command.get("username").toString().equals(conn.getUsername())) {
                return false;
            }
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
            whiteboardUI.getDrawingPanel().draw(drawMode, rgbValue, lineWidth, first, second, textInput);
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static void listenChatServer(Socket conn) {
        try (Socket serverSocket = conn) {
            DataInputStream input = new DataInputStream(serverSocket.getInputStream());
            boolean isConnTerminated = false;
            // Listen to drawing commands
            while (!isConnTerminated) {
                if (input.available() > 0) {
                    isConnTerminated = parseChatCommand(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean parseChatCommand(DataInputStream input) {
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = null;
        try {
            command = (JSONObject) parser.parse(input.readUTF());
            System.out.println(command.toJSONString());
            if (command.containsKey("username") && command.get("username").toString().equals(chatConn.getUsername())) {
                return false;
            }
            String username = "";
            if (command.containsKey("username")) {
                username = command.get("username").toString();
            } else {
                username = "admin";
            }
            String message = command.get("message").toString();
            whiteboardUI.getChat().addReceivedMessage(username, message);
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
