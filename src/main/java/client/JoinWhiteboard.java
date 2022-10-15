package client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.WhiteboardUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class JoinWhiteboard {

    static WhiteboardUI whiteboardUI;
    static Connection conn;

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Invalid arguments, retry the command using the syntax: JoinWhiteBoard <ip> <port> <username>");
            return;
        }
        // TODO: get the username from the args

        try {
            conn = new Connection("localhost", 3000);
            JSONObject connectionRequest = new JSONObject();
            conn.setUsername(args[0]);
//            conn.setUsername("Username1");
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
            whiteboardUI = new WhiteboardUI("Whiteboard Client", false, conn);
            whiteboardUI.setVisible(true);

            Thread t = new Thread(() -> listenServer(conn.socket));
            t.start();

        } catch (IOException e) {
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
            if (command.containsKey("username") && command.get("username").toString().equals(conn.getUsername())){
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
}
