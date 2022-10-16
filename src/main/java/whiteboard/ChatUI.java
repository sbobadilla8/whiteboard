package whiteboard;

import server.ChatServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

public class ChatUI extends JPanel {
    private JPanel chatContainer;
    private JList listChatWindow;
    private JTextArea chatInput;
    private JButton chatSend;
    private JScrollBar chatScroll;
    private JPanel chatContent;
    private JPanel chatInputContent;

    private ChatServer chatServer;

    private Vector<String> chats;

    public ChatUI() {

        //this.setPreferredSize(new Dimension(800, 300));
        //this.setContentPane(this.chatContainer);
        //this.chatContent.setPreferredSize(new Dimension(800, 350));
        //this.chatInputContent.setPreferredSize(new Dimension(800,50));
        //this.listChatWindow.setPreferredSize(new Dimension(800,350));

        chats = new Vector<>();
        //this.chatScroll.setPreferredSize(new Dimension(30, 350));

        //JScrollPane jScrollPane = new JScrollPane(this.chatContent);
        //jScrollPane.setPreferredSize(new Dimension(800, 350));
        //jScrollPane.setViewportView(chatContent);
        //this.listChatWindow.setLayoutOrientation(JList.VERTICAL);
        //this.chatContainer.add(jScrollPane);
        /*String[] chatLabel = new String[50];
        for(int i=0;i<50;i++) {
            chatLabel[i] = "Example text";
        }
        this.listChatWindow.setListData(chatLabel);*/
        this.chatSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                chats.add("Hello");
                JLabel chat = new JLabel("Hello");
                //listChatWindow.setListData(chats);
                chatContent.add(chat);
                revalidate();
                chat.setVisible(true);
            }
        });
        //this.pack();
    }

    public void setChatConnection(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
}
