package whiteboard;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class whiteboardUI extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JButton btnLine;
    private JButton btnRectangle;
    private JButton btnCircle;
    private JButton btnTriangle;
    private JButton btnFree;
    private JButton btnText;
    private JButton btnFile;
    private JButton btnColor;
    private JTextPane rgbShow;
    private JList listConnectedUsers;
    private JList listChatWindow;
    private JTextField inputChat;
    private JButton btnChatSend;
    private JPanel drawingPanelContainer;
    private JButton btnLineWidth;
    private DrawingPanel drawingPanel;


    public whiteboardUI(String title, Boolean isAdmin) {

        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1400, 900));
        this.setContentPane(mainPanel);
        this.drawingPanel = new DrawingPanel(isAdmin);
        this.drawingPanelContainer.add(drawingPanel);

        final JPopupMenu filePopup = new JPopupMenu();
        final JPopupMenu colorPopup = new JPopupMenu();
        final JPopupMenu lineWidthPopup = new JPopupMenu();

        filePopup.add(new JMenuItem(new AbstractAction("Option 1") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(mainPanel, "Option 1 selected");
            }
        }));
        filePopup.add(new JMenuItem(new AbstractAction("Option 2") {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(mainPanel, "Option 2 selected");
            }
        }));

        btnFile.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                filePopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        btnLine.setActionCommand("Line");
        btnRectangle.setActionCommand("Rectangle");
        btnTriangle.setActionCommand("Triangle");
        btnCircle.setActionCommand("Circle");
        btnFree.setActionCommand("Free");
        btnText.setActionCommand("Text");
        btnLine.addActionListener(this);
        btnRectangle.addActionListener(this);
        btnTriangle.addActionListener(this);
        btnCircle.addActionListener(this);
        btnFree.addActionListener(this);
        btnText.addActionListener(this);

        JSlider lineWidthSlider = new JSlider();
        lineWidthSlider.setMinimum(0);
        lineWidthSlider.setMaximum(40);
        lineWidthSlider.setValue((int) this.drawingPanel.getLineWidth());
        lineWidthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                drawingPanel.setLineWidth(lineWidthSlider.getValue());
            }
        });
        JPanel lineWidthPanel = new JPanel();
        lineWidthPanel.add(lineWidthSlider);

        lineWidthPopup.add(lineWidthPanel);

        btnLineWidth.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lineWidthPopup.show(e.getComponent(), e.getX(), e.getY() + 10);
            }
        });

        // Text input
        JPopupMenu textInputPopup = new JPopupMenu();
        JPanel textInputPanel = new JPanel();

        textInputPanel.add(this.drawingPanel.getTextInput());
        textInputPopup.add(textInputPanel);

        btnText.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                textInputPopup.show(e.getComponent(), e.getX(), e.getY() + 10);
            }
        });

        JLabel redLabel = new JLabel("Red");
        JLabel blueLabel = new JLabel("Blue");
        JLabel greenLabel = new JLabel("Green");
        JSlider redSlider = new JSlider();
        redSlider.setMinimum(0);
        redSlider.setMaximum(255);
        JSlider blueSlider = new JSlider();
        blueSlider.setMinimum(0);
        blueSlider.setMaximum(255);
        JSlider greenSlider = new JSlider();
        greenSlider.setMinimum(0);
        greenSlider.setMaximum(255);

        this.drawingPanel.setRgbValue(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()).getRGB());

        rgbShow.setForeground(new Color(this.drawingPanel.getRgbValue()));
        rgbShow.setBackground(new Color(this.drawingPanel.getRgbValue()));
        redSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                drawingPanel.setRgbValue(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()).getRGB());
                rgbShow.setForeground(new Color(drawingPanel.getRgbValue()));
                rgbShow.setBackground(new Color(drawingPanel.getRgbValue()));
            }
        });

        blueSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                drawingPanel.setRgbValue(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()).getRGB());
                rgbShow.setForeground(new Color(drawingPanel.getRgbValue()));
                rgbShow.setBackground(new Color(drawingPanel.getRgbValue()));
            }
        });

        greenSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                drawingPanel.setRgbValue(new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue()).getRGB());
                rgbShow.setForeground(new Color(drawingPanel.getRgbValue()));
                rgbShow.setBackground(new Color(drawingPanel.getRgbValue()));
            }
        });

        JPanel red = new JPanel();
        red.add(redLabel);
        red.add(redSlider);

        JPanel green = new JPanel();
        green.add(greenLabel);
        green.add(greenSlider);

        JPanel blue = new JPanel();
        blue.add(blueLabel);
        blue.add(blueSlider);

        colorPopup.add(red);
        colorPopup.add(green);
        colorPopup.add(blue);
        btnColor.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                colorPopup.show(e.getComponent(), e.getX(), e.getY() + 10);
            }
        });

        String week[] = {"Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"};

        //create list
        listConnectedUsers.setListData(week);

        /*try {
            File myObj = new File(this.fileName);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }*/

        String drawMode = "Line";
        drawingPanel.setDrawMode(drawMode);
        updateDrawButtonFocus(drawMode);

        this.pack();
    }

    public String getFileName() {
        return this.drawingPanel.getFileName();
    }

    public DrawingPanel getDrawingPanel() {
        return this.drawingPanel;
    }

    public void updateDrawButtonFocus(String drawMode) {
        switch (drawMode) {
            case "Line":
                btnLine.setForeground(Color.DARK_GRAY);
                btnRectangle.setForeground(new Color(160, 167, 180));
                btnCircle.setForeground(new Color(160, 167, 180));
                btnTriangle.setForeground(new Color(160, 167, 180));
                btnFree.setForeground(new Color(160, 167, 180));
                btnText.setForeground(new Color(160, 167, 180));
                break;
            case "Rectangle":
                btnLine.setForeground(new Color(160, 167, 180));
                btnRectangle.setForeground(Color.DARK_GRAY);
                btnCircle.setForeground(new Color(160, 167, 180));
                btnTriangle.setForeground(new Color(160, 167, 180));
                btnFree.setForeground(new Color(160, 167, 180));
                btnText.setForeground(new Color(160, 167, 180));
                break;
            case "Circle":
                btnLine.setForeground(new Color(160, 167, 180));
                btnRectangle.setForeground(new Color(160, 167, 180));
                btnCircle.setForeground(Color.DARK_GRAY);
                btnTriangle.setForeground(new Color(160, 167, 180));
                btnFree.setForeground(new Color(160, 167, 180));
                btnText.setForeground(new Color(160, 167, 180));
                break;
            case "Triangle":
                btnLine.setForeground(new Color(160, 167, 180));
                btnRectangle.setForeground(new Color(160, 167, 180));
                btnCircle.setForeground(new Color(160, 167, 180));
                btnTriangle.setForeground(Color.DARK_GRAY);
                btnFree.setForeground(new Color(160, 167, 180));
                btnText.setForeground(new Color(160, 167, 180));
                break;
            case "Free":
                btnLine.setForeground(new Color(160, 167, 180));
                btnRectangle.setForeground(new Color(160, 167, 180));
                btnCircle.setForeground(new Color(160, 167, 180));
                btnTriangle.setForeground(new Color(160, 167, 180));
                btnFree.setForeground(Color.DARK_GRAY);
                btnText.setForeground(new Color(160, 167, 180));
                break;
            case "Text":
                btnLine.setForeground(new Color(160, 167, 180));
                btnRectangle.setForeground(new Color(160, 167, 180));
                btnCircle.setForeground(new Color(160, 167, 180));
                btnTriangle.setForeground(new Color(160, 167, 180));
                btnFree.setForeground(new Color(160, 167, 180));
                btnText.setForeground(Color.DARK_GRAY);
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String drawMode = e.getActionCommand();
        drawingPanel.setDrawMode(drawMode);
        updateDrawButtonFocus(drawMode);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 10, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnRectangle = new JButton();
        btnRectangle.setText("Rectangle");
        panel1.add(btnRectangle, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnCircle = new JButton();
        btnCircle.setText("Circle");
        panel1.add(btnCircle, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnTriangle = new JButton();
        btnTriangle.setText("Triangle");
        panel1.add(btnTriangle, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnFree = new JButton();
        btnFree.setText("Freehand");
        panel1.add(btnFree, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnText = new JButton();
        btnText.setText("Text");
        panel1.add(btnText, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnLine = new JButton();
        btnLine.setText("Line");
        panel1.add(btnLine, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        panel1.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        btnFile = new JButton();
        btnFile.setText("File");
        toolBar1.add(btnFile);
        btnColor = new JButton();
        btnColor.setText("Color");
        panel1.add(btnColor, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rgbShow = new JTextPane();
        rgbShow.setText("Label");
        panel1.add(rgbShow, new GridConstraints(0, 9, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLineWidth = new JButton();
        btnLineWidth.setText("Line width");
        panel1.add(btnLineWidth, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        listConnectedUsers = new JList();
        panel2.add(listConnectedUsers, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        drawingPanelContainer = new JPanel();
        drawingPanelContainer.setLayout(new GridBagLayout());
        mainPanel.add(drawingPanelContainer, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        listChatWindow = new JList();
        panel3.add(listChatWindow, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        inputChat = new JTextField();
        panel4.add(inputChat, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        btnChatSend = new JButton();
        btnChatSend.setText("Send");
        panel4.add(btnChatSend, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
