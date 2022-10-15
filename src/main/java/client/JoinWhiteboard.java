package client;

import client.Connection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.WhiteboardServer;
import whiteboard.whiteboardUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class JoinWhiteboard {
    public static void main (String[] args) {
        try {
            Connection conn = new Connection("localhost", 3000);
//            client = new Socket("localhost", 3000);
//            DataInputStream input = new DataInputStream(client.getInputStream());
//            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            JSONObject connectionRequest = new JSONObject();
            connectionRequest.put("client-name", "Username1");
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
            whiteboardUI frame = new whiteboardUI("Whiteboard Client", false, conn);
            frame.setVisible(true);
            //String message = input.readUTF();
            //System.out.println(message);
            /*Thread.sleep(6000);
            JSONObject drawCommand = new JSONObject();
            drawCommand.put("paint-color", 6556410);
            drawCommand.put("line-width", 20);
            drawCommand.put("draw-mode", "Triangle");
            JSONArray firstPoints = new JSONArray();
            JSONArray secondPoints = new JSONArray();
            Map<String, Integer> pointsMap = new HashMap<>();
            Map<String, Integer> pointsMap2 = new HashMap<>();
            pointsMap.put("x", 200);
            pointsMap.put("y", 200);
            firstPoints.add(pointsMap);
            drawCommand.put("first-point", pointsMap);
            pointsMap2.put("x", 500);
            pointsMap2.put("y", 500);
            secondPoints.add(pointsMap);
            drawCommand.put("second-point", pointsMap2);

            output.writeUTF(drawCommand.toJSONString());
            /*output.flush();
            JSONParser parser = new JSONParser();
            JSONObject res = (JSONObject) parser.parse(input.readUTF());
            System.out.println("Received from server: " + res.get("result"));
            JSONObject goodbyeCommand = new JSONObject();
            goodbyeCommand.put("command_name", "Math");
            goodbyeCommand.put("method_name", "client_remove");
            output.writeUTF(goodbyeCommand.toJSONString());
            output.flush();*/
//            conn.close();

        } catch (IOException /*| ParseException*/ e) {
            throw new RuntimeException(e);
        } /*catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }
}
