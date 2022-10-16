package whiteboard;

import client.Connection;
import org.json.simple.JSONObject;
import server.ChatServer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.IOException;
import java.util.Arrays;

public class Chat {

    private JEditorPane chatPanel;
    private ChatServer chatServer;
    private boolean isAdmin;
    private Connection connection;

    public Chat(boolean isAdmin, JEditorPane chatPanel) {
        this.isAdmin = isAdmin;
        this.chatPanel = chatPanel;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setChatServer(ChatServer server) {
        this.chatServer = server;
    }

    public void addReceivedMessage(String username, String message) {
        Document doc = chatPanel.getDocument();
        try {
            String text = doc.getText(0, doc.getLength());
            String[] previous = text.split("[:\n]");
            String newText = "";
            for (int i = 0; i < previous.length - 1; i++) {
                if (!previous[i].equals("")) {
                    newText += "<p><span color='red'>" + previous[i] + ": " + "</span>" + previous[i + 1].trim().replaceAll(" +", " ") + "</p>";
                    i += 1;
                }
            }
            newText += "<p><span color='red'>" + username + ": " + "</span>" + message + "</p>";
            chatPanel.setText(newText);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        JSONObject messageCommand = new JSONObject();
        messageCommand.put("message", message);
        if (!isAdmin) {
            messageCommand.put("username", this.connection.getUsername());
            try {
                connection.output.writeUTF(messageCommand.toJSONString());
                connection.output.flush();
            } catch (IOException e) {
                addReceivedMessage("error", "Error while sending message");
            }
            addReceivedMessage("me", message);
        } else {
            addReceivedMessage("me", message);
            this.chatServer.multicastMessage(messageCommand);
        }
    }
}
