package client;

import server.ChatServer;
import server.WhiteboardServer;
import whiteboard.WhiteboardUI;

import java.io.IOException;

public class CreateWhiteboard {
    public static void main (String[] args) {
        WhiteboardUI frame = new WhiteboardUI("Whiteboard (Administrator)", true, null);
        // Cursed casting
        String fileName = frame.getFileName();
        frame.setVisible(true);
        /*if(args.length != 3) {
            System.out.println("Invalid arguments, retry the command using the syntax: CreateWhiteBoard <port>");
            return;
        }*/
        try {
            WhiteboardServer whiteboardServer = new WhiteboardServer(fileName, frame.getDrawingPanel());
            frame.getDrawingPanel().setServer(whiteboardServer);
            //ChatServer chatServer = new ChatServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
