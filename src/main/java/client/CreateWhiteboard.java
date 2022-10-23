package client;

import server.ChatServer;
import server.WhiteboardServer;
import whiteboard.WhiteboardUI;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class CreateWhiteboard {
    public static void main (String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid arguments, retry the command using the syntax: <username> <port>");
            return;
        }
        if (Integer.parseInt(args[1]) > 65535 || Integer.parseInt(args[1]) < 1024){
            System.out.println("Please choose a valid port range");
            return;
        }
        WhiteboardUI frame = new WhiteboardUI("Whiteboard (Administrator)", true, null);

        String fileName = frame.getFileName();
        frame.setVisible(true);

        WhiteboardServer whiteboardServer = null;
        try {
            whiteboardServer = new WhiteboardServer(fileName, frame.getDrawingPanel(), args[0], Integer.parseInt(args[1]));
            frame.getDrawingPanel().setServer(whiteboardServer);
            ChatServer chatServer = new ChatServer(frame.getChat(), Integer.parseInt(args[1]));
            frame.getChat().setChatServer(chatServer);
            whiteboardServer.setConnectedUsersList(frame.getConnectedUsers());
            whiteboardServer.setChatPort(chatServer.getPort());
        } catch (IOException e) {
            System.out.println("Failed to initialise the whiteboard/chat servers.");
        } finally {
            assert whiteboardServer != null;

        }

        // Shutdown hook for publishing disconnection
        WhiteboardServer finalWhiteboardServer = whiteboardServer;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("In shutdown hook");
                finalWhiteboardServer.killAll();
                //if(!frame.getDrawingPanel().getIsFileOpened()) {
                    File currentFile = new File(frame.getDrawingPanel().getFileName());
                    currentFile.delete();
                //}
            }
        }, "Shutdown-thread"));
    }
}
