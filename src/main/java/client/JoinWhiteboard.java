package client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.WhiteboardUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.Vector;

public class JoinWhiteboard {

    static WhiteboardUI whiteboardUI;
    static Connection conn;
    static Connection chatConn;

    static Vector connectedUsers;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Invalid arguments, retry the command using the syntax: JoinWhiteBoard <username> <ip>");
            return;
        }
        connectedUsers = new Vector<>();
        try {
            JSONParser parser = new JSONParser();
            System.out.println("Requesting access to whiteboard...");
            conn = new Connection(args[1], 3000);
            JSONObject connectionRequest = new JSONObject();
            conn.setUsername(args[0]);
//            conn.setUsername("user1");
            connectionRequest.put("client-name", conn.getUsername());
            conn.output.writeUTF(connectionRequest.toJSONString());
            conn.output.flush();
            // Read hello from server.
            JSONObject response = (JSONObject) parser.parse(conn.input.readUTF());
            System.out.println("Server Response: " + response.toString());
            if (response.get("result").toString().equals("rejected")) {
                System.out.println("Admin rejected the connection :(");
                return;
            } else if (response.get("result").toString().equals("duplicated")) {
                System.out.println("Username is already being used, please choose a different one and try again.");
                return;
            }

            JSONObject fileNameObj = (JSONObject) parser.parse(conn.input.readUTF());
            System.out.println("File Name: " + fileNameObj.get("fileName").toString());
            byte[] sizeArr = new byte[4];
            conn.input.read(sizeArr);
            int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();

            byte[] imageArr = new byte[size];
            conn.input.read(imageArr);
            BufferedImage initialImage = ImageIO.read(new ByteArrayInputStream(imageArr));
            conn.setFilename(fileNameObj.get("fileName").toString());
            //conn.setFilename("remoteWhiteboard.png");
            ImageIO.write(initialImage, "png", new File(conn.getFilename()));
            whiteboardUI = new WhiteboardUI("Whiteboard Client - " + args[0], false, conn);
            whiteboardUI.setVisible(true);

            chatConn = new Connection(args[1], 3001);
            chatConn.output.writeUTF(connectionRequest.toJSONString());
            chatConn.output.flush();
            chatConn.input.readUTF();
            chatConn.setUsername(args[0]);
//            chatConn.setUsername("user1");
            JSONObject command = (JSONObject) parser.parse(conn.input.readUTF());
            JSONArray values = (JSONArray) command.get("connected-users");
            whiteboardUI.getChat().setConnection(chatConn);
            connectedUsers.addAll(values);
            whiteboardUI.getConnectedUsers().setListData(connectedUsers);


            Thread t = new Thread(() -> listenServer(conn.socket));
            t.start();

            Thread t2 = new Thread(() -> listenChatServer(chatConn.socket));
            t2.start();

        } catch (ConnectException e) {
            System.out.println("An error occurred while connecting to the administrator's whiteboard. Check the IP address and try again.");
            return;
        } catch (IOException | ParseException e) {
            System.out.println("An error occurred while connecting to the administrator's whiteboard. Please try again.");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("In shutdown hook");
                JSONObject disconnectMessage = new JSONObject();
                disconnectMessage.put("disconnected", conn.getUsername());
                try {
                    conn.output.writeUTF(disconnectMessage.toJSONString());
                    conn.output.flush();
                    chatConn.output.writeUTF(disconnectMessage.toJSONString());
                    chatConn.output.flush();
                    File currentFile = new File(whiteboardUI.getDrawingPanel().getFileName());
                    currentFile.delete();
                } catch (IOException e) {
                    System.out.println("An error occurred while shutting down the whiteboard.");
                }
            }
        }, "Shutdown-thread"));
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
            System.out.println("Whiteboard server connection unexpectedly terminated.");
        }
    }

    public static boolean parseCommand(DataInputStream input) {
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = null;
        try {
            command = (JSONObject) parser.parse(input.readUTF());
            if (command.containsKey("killall")) {
                JOptionPane.showMessageDialog(whiteboardUI, "Admin closed the whiteboard connection");
                System.exit(0);
            }
            if (command.containsKey("kicked")) {
                JOptionPane.showMessageDialog(whiteboardUI, "You have been removed from the whiteboard");
                System.exit(0);
            }
            if (command.containsKey("new-user")) {
                connectedUsers.add(command.get("new-user").toString());
                whiteboardUI.getConnectedUsers().setListData(connectedUsers);
                return false;
            }
            if (command.containsKey("removed-user")) {
                String userRemoved = command.get("removed-user").toString();
                connectedUsers.remove(userRemoved);
                whiteboardUI.getConnectedUsers().setListData(connectedUsers);
                return false;
            }
            if (command.containsKey("username") && command.get("username").toString().equals(conn.getUsername())) {
                return false;
            }
            if (command.containsKey("fileName")) {
                byte[] sizeArr = new byte[4];
                conn.input.read(sizeArr);
                int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
                byte[] imageArr = new byte[size];
                conn.input.read(imageArr);
                BufferedImage newImage = ImageIO.read(new ByteArrayInputStream(imageArr));
                whiteboardUI.getDrawingPanel().openFile(null, newImage);
            } else {
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
            }
        } catch (ParseException | IOException e) {
            System.out.println("Failed to parse whiteboard server command");
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
            System.out.println("Chat server connection unexpectedly terminated.");
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
            System.out.println("Failed to parse chat server command");
        }
        return false;
    }
}
