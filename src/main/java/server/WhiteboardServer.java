package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import whiteboard.DrawingPanel;

import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class WhiteboardServer {
    private ServerSocket whiteboardSocket;
    private ConcurrentHashMap<String, Socket> clientList;
    private String fileName;
    private DrawingPanel whiteboard;
    private JList connectedUsersList;
    private Vector usernamesList;
    private int chatPort;
    private String admin;


    public WhiteboardServer(String fileName, DrawingPanel whiteboard, String adminUsername, int port) throws IOException {
        this.clientList = new ConcurrentHashMap<>();
        this.fileName = fileName;
        this.admin = adminUsername;
        this.whiteboard = whiteboard;
        this.usernamesList = new Vector<>();
        this.usernamesList.add(adminUsername + " (admin)");
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try {
            this.whiteboardSocket = factory.createServerSocket(port);
            System.out.println("Server initialized, waiting for client connection...");

            // Extremely cursed / 10
            new Thread(() -> {
                // Wait for connections.
                while (true) {
                    Socket client = null;
                    try {
                        client = whiteboardSocket.accept();
                        client.setKeepAlive(true);
                        JSONParser parser = new JSONParser();
                        DataInputStream input = new DataInputStream(client.getInputStream());
                        DataOutputStream output = new DataOutputStream(client.getOutputStream());
                        String clientName = readMessage(input);
                        JSONObject command = (JSONObject) parser.parse(clientName);
                        JSONObject resultMessage = new JSONObject();
                        String username = command.get("client-name").toString();
                        if (this.usernamesList.contains(username)) {
                            resultMessage.put("result", "duplicated");
                            output.writeUTF(resultMessage.toJSONString());
                            output.flush();
                        } else {
                            int n = JOptionPane.showConfirmDialog(whiteboard, "New user applying for connection! Admit " + command.get("client-name").toString() + "?", "New connection request", JOptionPane.YES_NO_OPTION);
                            if (n == 0) {
                                this.usernamesList.add(command.get("client-name").toString());
                                this.multicastNewUser(this.usernamesList.lastElement().toString());
                                this.clientList.put(command.get("client-name").toString(), client);
                                resultMessage.put("result", this.chatPort);
                                output.writeUTF(resultMessage.toJSONString());
                                output.flush();
                                Socket finalClient = client;
                                Thread t = new Thread(() -> serveClient(finalClient));
                                t.start();
                            } else {
                                resultMessage.put("result", "rejected");
                                output.writeUTF(resultMessage.toJSONString());
                                output.flush();
                            }
                        }
                    } catch (IOException | ParseException e) {
                        System.out.println("Unable to add client to whiteboard list.");
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("An error occurred while starting the main whiteboard thread.");
        }
    }

    public void serveClient(Socket client) {
        try (Socket clientSocket = client) {
            JSONParser parser = new JSONParser();
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            sendImage(output, whiteboard.getFileName());

            JSONObject userList = new JSONObject();
            JSONArray values = new JSONArray();
            values.addAll(this.usernamesList);
            userList.put("connected-users", values);
            output.writeUTF(userList.toJSONString());
            output.flush();


            this.connectedUsersList.setListData(usernamesList);


            boolean isPeerTerminated = false;
            // Listen to drawing commands
            while (!isPeerTerminated || !client.isClosed()) {
                if (input.available() > 0) {
                    isPeerTerminated = parseCommand(input);
                }
            }
        } catch (IOException | ParseException e) {
            System.out.println("Client disconnected");
        }
    }

    private Boolean parseCommand(DataInputStream input) throws IOException, ParseException {
        // The JSON Parser
        JSONParser parser = new JSONParser();
        // Attempt to convert read data to JSON
        JSONObject command = (JSONObject) parser.parse(readMessage(input));

        if (command.containsKey("disconnected")) {
            String usernameDis = command.get("disconnected").toString();
            this.clientList.remove(usernameDis);
            this.usernamesList.remove(usernameDis);
            this.connectedUsersList.setListData(usernamesList);
            multicastUsers(usernameDis);
            // Since peer has disconnected, we return true
            return true;
        }

        String username = command.get("username").toString();
        String drawMode = command.get("draw-mode").toString();
        Color rgbValue = Color.decode(command.get("paint-color").toString());
        Color fullColor = new Color(rgbValue.getRed(), rgbValue.getGreen(), rgbValue.getBlue(), Integer.parseInt(command.get("color-alpha").toString()));
        boolean fillForm = Boolean.parseBoolean(command.get("filled").toString());
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
        this.whiteboard.draw(drawMode, fullColor, fillForm, lineWidth, first, second, textInput);

        this.clientList.forEach((user, conn) -> {
            if (!user.equals(username)) {
                try {
                    DataOutputStream output = new DataOutputStream(conn.getOutputStream());
                    output.writeUTF(command.toJSONString());
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Client disconnected");
                    this.clientList.remove(user);
                    this.usernamesList.remove(user);
                    multicastUsers(user);
                }
            }
        });

        return false;
    }

    public void multicastImage(String fileName) {
        this.clientList.forEach((user, conn) -> {
            try {
                DataOutputStream output = new DataOutputStream(conn.getOutputStream());
                sendImage(output, fileName);
            } catch (IOException e) {
                System.out.println("Failed to send image to user "+user);
            }
        });
    }

    public void multicastDrawing(JSONObject drawingCommand) {
        this.clientList.forEach((user, conn) -> {
            if (!drawingCommand.containsKey("username")) {
                try {
                    DataOutputStream output = new DataOutputStream(conn.getOutputStream());
                    output.writeUTF(drawingCommand.toJSONString());
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Client disconnected");
                    this.clientList.remove(user);
                    this.usernamesList.remove(user);
                    multicastUsers(user);
                }
            }
        });
    }

    public void multicastUsers(String username) {
        this.connectedUsersList.setListData(usernamesList);
        this.clientList.forEach((user, conn) -> {
            if (!user.equals(username)) {
                try {
                    DataOutputStream output = new DataOutputStream(conn.getOutputStream());
                    JSONObject removedUser = new JSONObject();
                    removedUser.put("removed-user", username);
                    output.writeUTF(removedUser.toJSONString());
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Client disconnected in multicast of disconnection");
                    this.clientList.remove(user);
                    this.usernamesList.remove(user);
                    multicastUsers(user);
                }
            }
        });
    }

    public void multicastNewUser(String username) {
        this.clientList.forEach((user, conn) -> {
            try {
                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                JSONObject newUser = new JSONObject();
                newUser.put("new-user", username);
                outputStream.writeUTF(newUser.toJSONString());
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Client disconnected");
                this.clientList.remove(user);
                this.usernamesList.remove(user);
                multicastUsers(user);
            }
        });
    }

    public void kickUser(String username) {
        Socket client = this.clientList.get(username);
        try {
            DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
            JSONObject kickedMessage = new JSONObject();
            kickedMessage.put("kicked", "You have been kicked");
            outputStream.writeUTF(kickedMessage.toJSONString());
            outputStream.flush();
            client.close();
            this.clientList.remove(username);
            this.usernamesList.remove(username);
            multicastUsers(username);
        } catch (IOException e) {
            System.out.println("An error occurred while removing client.");
        }
    }

    public void killAll() {
        this.clientList.forEach((user, conn) -> {
            try {
                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                JSONObject killAll = new JSONObject();
                killAll.put("killall", "killall");
                outputStream.writeUTF(killAll.toJSONString());
                outputStream.flush();
            } catch (IOException e) {
                // It shouldn't need to get here... check later.
                System.out.println("Client disconnected");
                this.clientList.remove(user);
                this.usernamesList.remove(user);
                multicastUsers(user);
            }
        });
    }

    public String readMessage(DataInputStream input) throws IOException {
        return input.readUTF();
    }

    public void setConnectedUsersList(JList connectedUsersList) {
        this.connectedUsersList = connectedUsersList;
        this.connectedUsersList.setListData(usernamesList);
    }

    public void sendImage(DataOutputStream output, String fileName) throws IOException {
        // The welcome message consists of three parts - the first is the file name, the second is the initial canvas size, the second is the canvas png
        JSONObject fileNameObj = new JSONObject();
        fileNameObj.put("fileName", fileName);
        output.writeUTF(fileNameObj.toJSONString());
        output.flush();
        BufferedImage image = ImageIO.read(new File(fileName));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);

        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
        output.write(size);
        output.flush();
        output.write(byteArrayOutputStream.toByteArray());
        output.flush();
    }

    public void setChatPort(int port){
        this.chatPort = port;
    }

    public String getUsername(){
        return this.admin;
    }
}
