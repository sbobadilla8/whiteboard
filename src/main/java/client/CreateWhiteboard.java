package client;

import server.ChatServer;
import server.WhiteboardServer;
import whiteboard.whiteboardUI;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class CreateWhiteboard {
    public static void main (String[] args) {
        whiteboardUI frame = new whiteboardUI("Whiteboard (Administrator)", true, null);
        // Cursed casting
        String fileName = frame.getFileName();
        frame.setVisible(true);
        /*if(args.length != 3) {
            System.out.println("Invalid arguments, retry the command using the syntax: CreateWhiteBoard <port>");
            return;
        }*/
        try {
            WhiteboardServer whiteboardServer = new WhiteboardServer(fileName, frame.getDrawingPanel());
            ChatServer chatServer = new ChatServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
