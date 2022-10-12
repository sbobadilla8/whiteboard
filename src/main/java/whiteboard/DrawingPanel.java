package whiteboard;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DrawingPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    private final BufferedImage bufferedImage;
    private Graphics2D g2d;
    private String drawMode;
    private Point first = new Point(0, 0);
    private Point second = new Point(0, 0);
    private JLabel imageLabel;
    private int rgbValue;
    private float lineWidth;

    private JTextField textInput;
    private String fileName;

    public DrawingPanel() {
        this.textInput = new JTextField(10);
        this.lineWidth = (float) 10.0;
        // Long.toString is very much necessary, do not edit
        this.fileName = "whiteboard_" + Long.toString(System.currentTimeMillis()) + ".png";
        this.bufferedImage = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_ARGB);
        this.g2d = this.bufferedImage.createGraphics();
        this.g2d.setColor(Color.WHITE);
        this.g2d.fillRect(0, 0, 1000, 800);
        try {
            ImageIO.write(this.bufferedImage, "PNG", new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            imageLabel = new JLabel(new ImageIcon(ImageIO.read(new File(fileName))));
            this.add(imageLabel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.repaint();
        addMouseListener(this);
        addMouseMotionListener(this);

//        this.setVisible(true);
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

    public void setFirstLocation(int x, int y) {
        this.first.setLocation(x, y);
    }

    public void setSecondLocation(int x, int y) {
        this.second.setLocation(x, y);
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
        this.g2d.setPaint(new Color(this.rgbValue));
        this.g2d.setStroke(new BasicStroke(this.lineWidth));
        if (this.drawMode.equals("Text")) {
            Font font = new Font("TimesRoman", Font.BOLD, (int) this.lineWidth);
            this.g2d.setFont(font);
            this.g2d.drawString(this.textInput.getText(), first.x, first.y);
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
            }
        }
        try {
            ImageIO.write(this.bufferedImage, "PNG", new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.remove(imageLabel);
        this.revalidate();
        try {
            imageLabel = new JLabel(new ImageIcon(ImageIO.read(new File(fileName))));
            this.add(imageLabel);
            this.revalidate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.repaint();
    }

    public void draw(String drawMode,int rgbValue, float lineWidth, Point first, Point second) {
        // TODO: add textInput param
        this.g2d.setPaint(new Color(rgbValue));
        this.g2d.setStroke(new BasicStroke(lineWidth));
        if (this.drawMode.equals("Text")) {
            Font font = new Font("TimesRoman", Font.BOLD, (int) lineWidth);
            this.g2d.setFont(font);
            this.g2d.drawString(this.textInput.getText(), first.x, first.y);
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
            }
        }
        try {
            ImageIO.write(this.bufferedImage, "PNG", new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.remove(imageLabel);
        this.revalidate();
        try {
            imageLabel = new JLabel(new ImageIcon(ImageIO.read(new File(fileName))));
            this.add(imageLabel);
            this.revalidate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.repaint();
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
