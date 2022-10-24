package whiteboard;

import client.Connection;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class WhiteboardUI extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JButton btnLine;
    private JButton btnRectangle;
    private JButton btnCircle;
    private JButton btnTriangle;
    private JButton btnFree;
    private JButton btnText;
    private JButton btnFile;
    private JButton btnColor;
    private JPanel rgbDisplay;
    private JPanel drawingPanelContainer;
    private JButton btnLineWidth;
    private JPanel connectedUsersContainer;
    private JPanel chatContainer;
    private JEditorPane chatPanel;
    private JTextField chatInput;
    private JButton btnSend;
    private JList connectedUsers;
    private JButton btnKick;
    private JScrollPane chatScrollContainer;
    private JCheckBox fillForm;
    private DrawingPanel drawingPanel;
    private Chat chat;
    private JFileChooser fileChooser;

    private JColorChooser colorChooser;


    public WhiteboardUI(String title, Boolean isAdmin, Connection conn) {

        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setPreferredSize(new Dimension(1225, 700));
        this.setContentPane(mainPanel);
        this.drawingPanel = new DrawingPanel(isAdmin, conn);
        this.drawingPanelContainer.add(drawingPanel);
        this.chat = new Chat(isAdmin, chatPanel);

        final JPopupMenu filePopup = new JPopupMenu();
        final JPopupMenu colorPopup = new JPopupMenu();
        final JPopupMenu lineWidthPopup = new JPopupMenu();


        this.colorChooser = new JColorChooser(Color.black);
        drawingPanel.setRgbValue(colorChooser.getColor());
        rgbDisplay.setBackground(new Color(colorChooser.getColor().getRed(), colorChooser.getColor().getGreen(), colorChooser.getColor().getBlue(), colorChooser.getColor().getAlpha()));
        AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : panels) {
            if (!accp.getDisplayName().equals("HSV")) {
                colorChooser.removeChooserPanel(accp);
            }
        }
        colorChooser.setPreviewPanel(new JPanel());
        colorChooser.getSelectionModel().addChangeListener(e -> {
            drawingPanel.setRgbValue(colorChooser.getColor());
            rgbDisplay.setBackground(new Color(colorChooser.getColor().getRed(), colorChooser.getColor().getGreen(), colorChooser.getColor().getBlue(), colorChooser.getColor().getAlpha()));
        });

        this.fileChooser = new JFileChooser();
        this.fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.fileChooser.addChoosableFileFilter(new ImageFilter());
        this.fileChooser.setAcceptAllFileFilterUsed(false);

        if (isAdmin) {
            filePopup.add(new JMenuItem(new AbstractAction("New") {
                public void actionPerformed(ActionEvent e) {
                    Object[] options = {"Save and continue",
                            "Continue without saving",
                            "Cancel"};
                    int n = JOptionPane.showOptionDialog(drawingPanel,
                            "This will override all unsaved changes. Do you wish to save before opening a new image?", "New File",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (n == 0) {
                        boolean res = drawingPanel.saveFile("");
                        if (res) {
                            JOptionPane.showMessageDialog(mainPanel, "File saved successfully.");
                            drawingPanel.initializeBlankCanvas(true);
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, "Could not save file, try again.");
                        }
                    } else if (n == 1) {
                        drawingPanel.initializeBlankCanvas(true);
                    }
                }
            }));
            filePopup.add(new JMenuItem(new AbstractAction("Open") {
                public void actionPerformed(ActionEvent e) {
                    Object[] options = {"Save and continue",
                            "Continue without saving",
                            "Cancel"};
                    int n = JOptionPane.showOptionDialog(drawingPanel,
                            "This will override all unsaved changes. Do you wish to save before opening a new image?", "Open File",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (n == 0) {
                        boolean res = drawingPanel.saveFile("");
                        if (res) {
                            JOptionPane.showMessageDialog(mainPanel, "File saved successfully.");
                            int returnValue = fileChooser.showOpenDialog(mainPanel);
                            if (returnValue == JFileChooser.APPROVE_OPTION) {
                                File openedFile = fileChooser.getSelectedFile();
                                drawingPanel.openFile(openedFile, null);
                            }
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, "Could not save file, try again.");
                        }
                    } else if (n == 1) {
                        int returnValue = fileChooser.showOpenDialog(mainPanel);
                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            File openedFile = fileChooser.getSelectedFile();
                            drawingPanel.openFile(openedFile, null);
                        }
                    }
                }
            }));
            filePopup.add(new JMenuItem(new AbstractAction("Save") {
                public void actionPerformed(ActionEvent e) {
                    boolean res = drawingPanel.saveFile("");
                    if (res) {
                        JOptionPane.showMessageDialog(mainPanel, "File saved successfully.");
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "Could not save file, try again.");
                    }
                }
            }));
            filePopup.add(new JMenuItem(new AbstractAction("Save As") {
                public void actionPerformed(ActionEvent e) {
                    int returnValue = fileChooser.showSaveDialog(mainPanel);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        String customSaveFileName = null;
                        try {
                            customSaveFileName = fileChooser.getSelectedFile().getCanonicalPath();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        boolean res = drawingPanel.saveFile(customSaveFileName);
                        if (res) {
                            JOptionPane.showMessageDialog(mainPanel, "File saved successfully.");
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, "Could not save file, try again using the supported extensions bmp, jpg, png.");
                        }
                    }
                }
            }));
            filePopup.add(new JMenuItem(new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    Object[] options = {"Save and exit",
                            "Exit without saving",
                            "Cancel"};
                    int n = JOptionPane.showOptionDialog(drawingPanel,
                            "You will lose all unsaved changes. Do you wish to save before exiting?", "Close",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (n == 0) {
                        boolean res = drawingPanel.saveFile("");
                        if (res) {
                            JOptionPane.showMessageDialog(mainPanel, "File saved successfully.");
                            System.exit(0);
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, "Could not save file, try again.");
                        }
                    } else if (n == 1) {
                        System.exit(0);
                    }
                }
            }));
        }

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
//        fillForm = new JCheckBox("Fill form?", false);
        fillForm.addActionListener(e -> {
            drawingPanel.setFillForm(fillForm.isSelected());
        });

        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isAdmin) {
                    chat.sendMessage(chatInput.getText(), drawingPanel.getUsername());
                } else {
                    chat.sendMessage(chatInput.getText());
                }
                chatInput.setText("");
            }
        });

        btnKick.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String username = connectedUsers.getSelectedValue().toString();
                System.out.println(username);
                boolean admin = Pattern.matches("\\w+\\s\\(admin\\)", username);
                if (!admin) {
                    drawingPanel.kickUser(username);
                }
            }
        });

        btnFile.setVisible(isAdmin);
        btnKick.setVisible(isAdmin);
        btnKick.setText("Kick user");

        JSlider lineWidthSlider = new JSlider();
        lineWidthSlider.setMinimum(5);
        lineWidthSlider.setMaximum(40);
        lineWidthSlider.setValue((int) this.drawingPanel.getLineWidth());
        lineWidthSlider.addChangeListener(e -> drawingPanel.setLineWidth(lineWidthSlider.getValue()));
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

        colorPopup.add(colorChooser);
        btnColor.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                colorPopup.show(e.getComponent(), e.getX(), e.getY() + 10);
            }
        });

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

    public Chat getChat() {
        return this.chat;
    }

    public JList getConnectedUsers() {
        return this.connectedUsers;
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
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 11, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        btnFile.setMaximumSize(new Dimension(80, 20));
        btnFile.setMinimumSize(new Dimension(80, 20));
        btnFile.setText("File");
        toolBar1.add(btnFile);
        btnColor = new JButton();
        btnColor.setText("Color");
        panel1.add(btnColor, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLineWidth = new JButton();
        btnLineWidth.setText("Line width");
        panel1.add(btnLineWidth, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rgbDisplay = new JPanel();
        rgbDisplay.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rgbDisplay.setOpaque(true);
        panel1.add(rgbDisplay, new GridConstraints(0, 9, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 30), null, 1, false));
        fillForm = new JCheckBox();
        fillForm.setEnabled(true);
        fillForm.setText("Fill Form?");
        panel1.add(fillForm, new GridConstraints(0, 10, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectedUsersContainer = new JPanel();
        connectedUsersContainer.setLayout(new GridLayoutManager(2, 1, new Insets(0, 5, 0, 0), -1, -1));
        panel2.add(connectedUsersContainer, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnKick = new JButton();
        btnKick.setText("Kick user");
        connectedUsersContainer.add(btnKick, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectedUsers = new JList();
        connectedUsersContainer.add(connectedUsers, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 600), null, 0, false));
        drawingPanelContainer = new JPanel();
        drawingPanelContainer.setLayout(new GridBagLayout());
        panel2.add(drawingPanelContainer, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(800, 600), new Dimension(800, 600), new Dimension(800, 600), 1, false));
        chatContainer = new JPanel();
        chatContainer.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(chatContainer, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 625), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        chatContainer.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        chatInput = new JTextField();
        panel3.add(chatInput, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        btnSend = new JButton();
        btnSend.setText("Send");
        panel3.add(btnSend, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chatScrollContainer = new JScrollPane();
        chatContainer.add(chatScrollContainer, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatPanel = new JEditorPane();
        chatPanel.setContentType("text/html");
        chatPanel.setEditable(false);
        chatScrollContainer.setViewportView(chatPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
