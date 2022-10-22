package whiteboard;

import client.Connection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.WhiteboardServer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DrawingPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    private int width;
    private int height;
    private BufferedImage bufferedImage;
    private Graphics2D g2d;
    private String drawMode;
    private Point first = new Point(0, 0);
    private Point second = new Point(0, 0);
    private JLabel imageLabel;
    private int rgbValue;
    private float lineWidth;
    private JTextField textInput;
    private String fileName;
    private Boolean isAdmin;
    private Connection connection;
    private WhiteboardServer server;

    public DrawingPanel(Boolean isAdmin, Connection conn) {
        this.isAdmin = isAdmin;
        this.textInput = new JTextField(10);
        this.lineWidth = (float) 10.0;
        this.width = 800;
        this.height = 600;
        if (!this.isAdmin) {
            this.connection = conn;
            this.fileName = conn.getFilename();
            try {
                this.bufferedImage = ImageIO.read(new File(this.fileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.g2d = this.bufferedImage.createGraphics();
        } else {
            this.connection = null;
            // Long.toString is very much necessary, do not remove
            setFileName("whiteboard_" + Long.toString(System.currentTimeMillis()) + ".png");
            initializeBlankCanvas(false);
        }
        try {
            writeFile(this.fileName);
            imageLabel = new JLabel(new ImageIcon(ImageIO.read(new File(this.fileName))));
            this.add(imageLabel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.repaint();
        addMouseListener(this);
        addMouseMotionListener(this);

//        this.setVisible(true);
    }

    public void setServer(WhiteboardServer server){
        this.server = server;
    }

    public WhiteboardServer getServer(){
        return this.server;
    }

    public void setDrawMode(String drawMode) {
        this.drawMode = drawMode;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getLineWidth() {
        return this.lineWidth;
    }

    public int getRgbValue() {
        return rgbValue;
    }

    public void setRgbValue(int newValue) {
        this.rgbValue = newValue;
    }

    public JTextField getTextInput() {
        return this.textInput;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        drawMode = e.getActionCommand();
        first.setLocation(0, 0);
        second.setLocation(0, 0);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        draw();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        first.setLocation(0, 0);
        second.setLocation(0, 0);
        first.setLocation(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!drawMode.equals("Text")) {
            second.setLocation(e.getX(), e.getY());
            this.draw();
        }

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (drawMode.equals("Free")) {
            if (second.x != 0 && second.y != 0) {
                first.x = second.x;
                first.y = second.y;
            }
            second.setLocation(event.getX(), event.getY());
            draw();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    //    @Override
    public void draw() {
        JSONObject drawCommand = new JSONObject();
        drawCommand.put("paint-color", this.rgbValue);
        drawCommand.put("line-width", this.lineWidth);
        drawCommand.put("draw-mode", this.drawMode);
        Map<String, Integer> firstMap = new HashMap<>();
        Map<String, Integer> secondMap = new HashMap<>();
        firstMap.put("x", this.first.x);
        firstMap.put("y", this.first.y);
        drawCommand.put("first-point", firstMap);
        secondMap.put("x", this.second.x);
        secondMap.put("y", this.second.y);
        drawCommand.put("second-point", secondMap);
        drawCommand.put("text-input", this.textInput.getText());
        if (!isAdmin) {
            drawCommand.put("username", this.connection.getUsername());
            try {
                connection.output.writeUTF(drawCommand.toJSONString());
                connection.output.flush();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error while writing input to server");
            }
            drawAndSaveCanvas(this.drawMode, this.rgbValue, this.lineWidth, this.first, this.second, this.textInput.getText());
        } else {
            drawAndSaveCanvas(this.drawMode, this.rgbValue, this.lineWidth, this.first, this.second, this.textInput.getText());
            this.server.multicastDrawing(drawCommand);
        }
    }

    public synchronized void draw(String drawMode, int rgbValue, float lineWidth, Point first, Point second, String textInput) {
        drawAndSaveCanvas(drawMode, rgbValue, lineWidth, first, second, textInput);
    }

    // Synchronized since multiple whiteboard server threads may access this
    public synchronized void drawAndSaveCanvas(String drawMode, int rgbValue, float lineWidth, Point first, Point second, String textInput) {
        this.g2d.setPaint(new Color(rgbValue));
        this.g2d.setStroke(new BasicStroke(lineWidth));
        if (drawMode.equals("Text")) {
            Font font = new Font("TimesRoman", Font.BOLD, (int) lineWidth);
            this.g2d.setFont(font);
            this.g2d.drawString(textInput, first.x, first.y);
        } else if (!first.equals(second)) {
            switch (drawMode) {
                case "Line":
                    this.g2d.drawLine(first.x, first.y, second.x, second.y);
                    break;
                case "Rectangle":
                    int width = Math.abs(second.x - first.x);
                    int height = Math.abs(second.y - first.y);
                    Point topLeft = new Point();
                    topLeft.x = Math.min(first.x, second.x);
                    topLeft.y = Math.min(first.y, second.y);
                    this.g2d.drawRect(topLeft.x, topLeft.y, width, height);
                    break;
                case "Circle":
                    width = Math.abs(second.x - first.x);
                    height = Math.abs(second.y - first.y);
                    topLeft = new Point();
                    topLeft.x = Math.min(first.x, second.x);
                    topLeft.y = Math.min(first.y, second.y);
                    this.g2d.drawOval(topLeft.x, topLeft.y, width, height);
                    break;
                case "Triangle":
                    int[] xPoints = {first.x, (first.x + second.x) / 2, second.x};
                    int[] yPoints = {second.y, first.y, second.y};

                    this.g2d.drawPolygon(xPoints, yPoints, 3);
                    break;
                case "Free":
                    this.g2d.drawLine(first.x, first.y, second.x, second.y);
                    break;
                case "Clean":
                    System.out.println("Clearing whiteboard ...");
                    this.g2d.setColor(Color.WHITE);
                    this.g2d.fillRect(0, 0, this.width, this.height);
                    break;
            }
        }
        try {
            writeFile(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            JLabel newImageLabel = new JLabel(new ImageIcon(ImageIO.read(new File(fileName))));
            this.remove(imageLabel);
            imageLabel = newImageLabel;
            this.add(imageLabel);
            this.revalidate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.repaint();
    }

    public void initializeBlankCanvas(Boolean overwriteExisting) {
        if(!overwriteExisting) {
            System.out.println("Initializing buffered image ...");
            this.bufferedImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
            this.g2d = this.bufferedImage.createGraphics();
            this.g2d.setColor(Color.WHITE);
            this.g2d.fillRect(0, 0, this.width, this.height);
        }
        else {
            System.out.println("Initializing whiteboard clear ...");
            draw("Clean", this.rgbValue, this.lineWidth, new Point(0,0), new Point(0,1), "");
            
            JSONObject drawCommand = new JSONObject();
            drawCommand.put("paint-color", this.rgbValue);
            drawCommand.put("line-width", this.lineWidth);
            drawCommand.put("draw-mode", "Clean");
            Map<String, Integer> firstMap = new HashMap<>();
            Map<String, Integer> secondMap = new HashMap<>();
            firstMap.put("x", 0);
            firstMap.put("y", 0);
            drawCommand.put("first-point", firstMap);
            secondMap.put("x", 0);
            secondMap.put("y", 1);
            drawCommand.put("second-point", secondMap);
            drawCommand.put("text-input", "");
            this.server.multicastDrawing(drawCommand);
        }
    }

    public void saveFile(String fileName) {
        try {
            writeFile(fileName.isEmpty() ? "whiteboard_SNAPSHOT.png" : fileName);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void writeFile(String fileName) throws IOException {
        ImageIO.write(this.bufferedImage, "PNG", new File(fileName));
    }

    public void kickUser(String username){
        this.server.kickUser(username);
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
