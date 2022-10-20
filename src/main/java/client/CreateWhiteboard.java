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
        WhiteboardServer whiteboardServer = null;
        try {
            whiteboardServer = new WhiteboardServer(fileName, frame.getDrawingPanel());
            frame.getDrawingPanel().setServer(whiteboardServer);
            ChatServer chatServer = new ChatServer(frame.getChat());
            frame.getChat().setChatServer(chatServer);
            whiteboardServer.setConnectedUsersList(frame.getConnectedUsers());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            assert whiteboardServer != null;

        }

        // Shutdown hook for publishing disconnection
        WhiteboardServer finalWhiteboardServer = whiteboardServer;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("In shutdown hook");
                finalWhiteboardServer.killAll();
            }
        }, "Shutdown-thread"));
    }
}
