package whiteboard;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.WhiteboardServer;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class JoinWhiteboard {
    public static void main (String[] args) {
        Socket client;
        try {
            client = new Socket("localhost", 3000);
            DataInputStream input = new DataInputStream(client.getInputStream());
            DataOutputStream output = new DataOutputStream(client.getOutputStream());

            output.writeUTF("example_client_name");
            output.flush();
            JSONObject newCommand = new JSONObject();
            newCommand.put("command_name", "Math");
            newCommand.put("method_name", "whiteboard_line");
            // Read hello from server.
            String message = input.readUTF();
            System.out.println(message);

            output.writeUTF(newCommand.toJSONString());
            output.flush();
            JSONParser parser = new JSONParser();
            JSONObject res = (JSONObject) parser.parse(input.readUTF());
            System.out.println("Received from server: " + res.get("result"));
            JSONObject goodbyeCommand = new JSONObject();
            goodbyeCommand.put("command_name", "Math");
            goodbyeCommand.put("method_name", "client_remove");
            output.writeUTF(goodbyeCommand.toJSONString());
            output.flush();
            client.close();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
