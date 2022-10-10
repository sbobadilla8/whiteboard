package whiteboard;

import server.WhiteboardServer;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class CreateWhiteboard {
    public static void main (String[] args) {
        JFrame frame = new whiteboardUI("Whiteboard");
        // Cursed casting
        String fileName = ((whiteboardUI) frame).getFileName();
        frame.setVisible(true);
        int port = args.length == 2 ? Integer.parseInt(args[1]) : 3000;
        /*if(args.length != 3) {
            System.out.println("Invalid arguments, retry the command using the syntax: CreateWhiteBoard <port>");
            return;
        }*/
        try {
            WhiteboardServer whiteboardServer = new WhiteboardServer(port, fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
