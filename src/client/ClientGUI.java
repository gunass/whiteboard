package client;

import util.UserIdentity;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static java.awt.Image.SCALE_FAST;

/**
 * Default graphical user interface for the whiteboard application. Begins with a login screen which prompts
 * the user for server hostname, username and password. On "connect", the user requests a join to the server.
 * If approved, the login screen is replaced by an InteractiveCanvas bound to the server.
 * @author Alex Epstein
 */
public class ClientGUI {

    private final int _CANVAS_HEIGHT = 500;
    private final int _CANVAS_WIDTH = 500;
    private final int _BORDER = 10;
    private final int _MENU_AREA_HEIGHT = 50;
    private final int _TEXT_HEIGHT = 20;
    private final int _USERS_DISPLAY_WIDTH = 200;
    private final int _USERS_DISPLAY_HEIGHT = _CANVAS_HEIGHT;
    private final int _WINDOW_WIDTH = _CANVAS_WIDTH + 3*_BORDER + _USERS_DISPLAY_WIDTH;
    private final int _WINDOW_HEIGHT = _CANVAS_HEIGHT + 3*_BORDER + _MENU_AREA_HEIGHT + _TEXT_HEIGHT;
    private final int _MENU_AREA_ROOT_Y = _CANVAS_HEIGHT;

    private final String[] toolsAvailable = {"Free Line", "Line", "Triangle", "Rectangle", "Circle", "Text"};
    private JButton disconnectButton;
    private int menuNextX = _BORDER;

    private final Color _DEFAULT_COLOUR = Color.BLUE;
    private final String _DEFAULT_TOOL = "Free Line";
    protected JFrame mainWindow;
    private JPanel canvasPanel;
    private JComboBox<String> drawables;
    private DefaultListModel<String> usersListModel = new DefaultListModel<>();
    private JList<String> usersList;
    private JLabel usersListLabel;
    private StyledDocument chatDocument = new DefaultStyledDocument();
    private SimpleAttributeSet usernameAttributes = new SimpleAttributeSet();
    private SimpleAttributeSet chatTextAttributes = new SimpleAttributeSet();
    private JTextPane chatBox;
    private JScrollPane chatBoxScrollPane;
    private JTextField chatEntry;
    private JButton colourButton;
    private JPanel colourPanel;
    private JButton clearButton;
    private JButton kickButton;


    private InteractiveCanvasManager canvasMgr;
    private ToolBar toolbar;
    private UsersList usersListPanel;
    private FileMenuBar fileMenuBar;
    private File workingFile;
    private String kickUser;

    /**
     * Creates a client GUI with all the default features (no refunds)
     */
    public ClientGUI() {

        this.mainWindow = new JFrame();
        mainWindow.setResizable(false);
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.canvasPanel = new ConnectionPanel();
        this.toolbar = new ToolBar();
        this.usersListPanel = new UsersList();
        this.fileMenuBar = new FileMenuBar();

        fileMenuBar.setEnables(false, false, false);

        canvasPanel.setBounds(0, 0, _CANVAS_WIDTH, _CANVAS_HEIGHT);
        toolbar.setBounds(0, _MENU_AREA_ROOT_Y, _CANVAS_WIDTH, _MENU_AREA_HEIGHT);
        usersListPanel.setBounds(_CANVAS_WIDTH + 2*_BORDER, 0, _USERS_DISPLAY_WIDTH, _USERS_DISPLAY_HEIGHT);

        mainWindow.add(canvasPanel);
        mainWindow.add(toolbar);
        mainWindow.add(usersListPanel);
        mainWindow.setJMenuBar(fileMenuBar);

        mainWindow.setSize(_WINDOW_WIDTH,_WINDOW_HEIGHT);
        mainWindow.setLayout(null);
        mainWindow.setVisible(true);

        usernameAttributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

    }

    /**
     * A menubar with a "File" menu embedded.
     * @author Alex Epstein
     */
    public class FileMenuBar extends JMenuBar {
        JMenuItem open;
        JMenuItem save;
        JMenuItem saveAs;
        public FileMenuBar() {

            JMenu fileMenu = new JMenu("File");

            open = new JMenuItem("Open...");
            save = new JMenuItem("Save");
            saveAs = new JMenuItem("Save...");

            open.addActionListener(e -> {
                //https://www.javatpoint.com/java-jfilechooser
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Open file...");
                chooser.setFileFilter(new FileNameExtensionFilter(null, "canvas"));
                int i = chooser.showOpenDialog(this);
                if (i == JFileChooser.APPROVE_OPTION) {
                    File selected = chooser.getSelectedFile();
                    if (!selected.getName().endsWith(".canvas")) {
                        System.out.println("File selected is not a .canvas file");
                        return;
                    } else {
                        workingFile = selected;
                        canvasMgr.upload(selected);
                        mainWindow.setTitle(selected.getName());
                        setEnables(true, true, true);
                    }
                }
            });

            save.addActionListener(e -> {
                canvasMgr.download(workingFile);
            });

            saveAs.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save canvas as...");
                chooser.setFileFilter(new FileNameExtensionFilter(null, "canvas"));
                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                int i = chooser.showSaveDialog(this);
                if (i == JFileChooser.APPROVE_OPTION) {
                    File newSave = chooser.getSelectedFile();
                    if (!newSave.getName().endsWith(".canvas")) {
                        System.out.println("Must save with .canvas extension");
                    } else {
                        workingFile = newSave;
                        canvasMgr.download(newSave);
                        mainWindow.setTitle(newSave.getName());
                        setEnables(true, true, true);
                    }
                }

            });

            fileMenu.add(open);
            fileMenu.add(save);
            fileMenu.add(saveAs);

            this.add(fileMenu);

        }


        public void setEnables(boolean bOpen, boolean bSave, boolean bSaveAs) {
            // If not admin, force "open" to be false
            open.setEnabled(bOpen && canvasMgr != null && canvasMgr.isAdmin());
            save.setEnabled(bSave);
            saveAs.setEnabled(bSaveAs);
            this.repaint();
        }

    }

    public void addClientUser(String username) {
        usersListModel.addElement(username);
    }

    public void rmClientUser(String username) {
        for (int i = 0; i < usersListModel.size(); i++) {
            if (usersListModel.get(i).equals(username)) {
                usersListModel.remove(i);
            }
        }
    }

    /**
     * A reactively-sized user list interface constructed mostly through trial and error.
     * Includes the active users view, chat feed, and chat field/button
     * The active users view takes up as much y space as there are users in the server
     * The chat field/button takes up constant y space
     * The chat feed fills up the rest of the y space between the two
     * @author Alex Epstein
     */
    private class UsersList extends JPanel {
        public UsersList() {

            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            usersList = new JList<>(usersListModel);
            usersList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    kickUser = usersList.getSelectedValue();
                }
            });
            usersListLabel = new JLabel("Active users");

            // https://www.javaprogrammingforums.com/awt-java-swing/33838-jtextpane-add-text-bottom-upwards.html
            chatBox = new JTextPane(chatDocument);
            chatBox.setEditable(false);
            JPanel chatBoxContainer = new JPanel(new BorderLayout());
            chatBoxContainer.setBackground(Color.WHITE);
            chatBoxContainer.add(chatBox, BorderLayout.SOUTH);
            chatBoxScrollPane = new JScrollPane(chatBoxContainer);
            chatBoxScrollPane.setBackground(Color.WHITE);

            JLabel chatLabel = new JLabel("Chat: ");
            chatEntry = new JTextField();
            chatEntry.addKeyListener(new ChatEnterListener());
            chatEntry.setEnabled(false);

            gbc.fill = GridBagConstraints.HORIZONTAL;

            //gbc.weightx = 1.0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 4;
            this.add(usersListLabel, gbc);
            gbc.gridy = 1;
            this.add(usersList, gbc);
            gbc.gridy = 2;
            this.add(new JSeparator(), gbc);
            gbc.gridy = 3;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            this.add(chatBoxScrollPane, gbc);
            gbc.weighty = 0;
            gbc.gridy = 4;
            this.add(new JSeparator(), gbc);
            gbc.gridy = 5;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.WEST;
            this.add(chatLabel, gbc);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 3;
            this.add(chatEntry, gbc);

            gbc.anchor = GridBagConstraints.PAGE_END;

        }

    }

    public void postToChat(String username, String message) {
        // http://www.java2s.com/Tutorial/Java/0240__Swing/SimpleAttributeBoldItalic.htm
        try {
            // Insert username in bold, and text in standard face
            chatDocument.insertString(chatDocument.getLength(), username + ": ", usernameAttributes);
            chatDocument.insertString(chatDocument.getLength(), message + "\n", chatTextAttributes);
            // Scroll to bottom of the text by default (simulates a bottom-up feed)
            chatBox.setCaretPosition(chatBox.getText().length());
        } catch (Exception ignored) {}
    }


    /**
     * A bar, for tools
     */
    private class ToolBar extends JPanel {

        public ToolBar() {
            this.setLayout(new FlowLayout());

            drawables = new JComboBox<String>(toolsAvailable);
            drawables.addActionListener(e -> {
                canvasMgr.canvas.toolSelected = (String) drawables.getSelectedItem();
            });

            // A button and a panel to select and display the pen colour
            ImageIcon colourIcon = new ImageIcon("borderpainter.png");
            colourButton = new JButton(colourIcon);
            System.out.println(System.getProperty("user.dir"));
            colourPanel = new JPanel();
            colourPanel.setOpaque(true);
            colourPanel.setBackground(_DEFAULT_COLOUR);

            colourButton.addActionListener(e -> {
                        Color colour = JColorChooser.showDialog(colourButton, "Select colour", canvasMgr.canvas.colourSelected);
                        colourPanel.setBackground(colour);
                        canvasMgr.canvas.colourSelected = colour;
                    }
            );

            ImageIcon clearIcon = new ImageIcon("bqm-remove.png");
            clearButton = new JButton(clearIcon);
            clearButton.addActionListener(e -> {
                canvasMgr.requestClearCanvas();
            });

            ImageIcon disconnectIcon = new ImageIcon("gtk-disconnect.png");
            disconnectButton = new JButton(disconnectIcon);
            disconnectButton.addActionListener(e -> {
                canvasMgr.reset();
            });

            ImageIcon kickIcon = new ImageIcon("bqm-remove.png");
            kickButton = new JButton(kickIcon);
            kickButton.addActionListener(e -> {
                if (kickUser == null){
                    JOptionPane.showMessageDialog(mainWindow, "Please highlight a user from users list");
                } else if (usersList.getSelectedIndex() == 0){
                    JOptionPane.showMessageDialog(mainWindow, "Error: cannot select self");
                } else {
                    canvasMgr.removeUser(usersList.getSelectedValue());
                }
            });

            this.enableAll(false);

            this.add(drawables);
            this.add(colourButton);
            this.add(colourPanel);
            this.add(clearButton);
            this.add(disconnectButton);
            this.add(kickButton);
        }

        public void enableAll(boolean v) {
            drawables.setEnabled(v);
            colourButton.setEnabled(v);
            clearButton.setEnabled(v);
            disconnectButton.setEnabled(v);
            kickButton.setEnabled(v);
        }

    }

    public void reset(){
        // On disconnect, delete the canvas, disable the relevant buttons,
        // and reinstantiate a ConnectionPanel in the GUI
        canvasMgr.notifyDisconnect();
        mainWindow.remove(canvasMgr.canvas);
        canvasPanel = new ConnectionPanel();
        mainWindow.add(canvasPanel);
        usersListModel.clear();
        toolbar.enableAll(false);
        chatEntry.setEnabled(false);
        fileMenuBar.setEnables(false, false, false);
        canvasPanel.repaint();
    }

    /**
     * The default starting panel of the application which allows the user to submit their credentials as a form.
     * @author Alex Epstein
     */
    private class ConnectionPanel extends JPanel {

        public ConnectionPanel() {
            this.setLayout(null);
            this.setBounds(0, 0, _CANVAS_WIDTH, _CANVAS_HEIGHT);

            // Some brief text
            JTextArea connectionTextArea = new JTextArea("Connect to server:");
            connectionTextArea.setBounds(0, 0, _CANVAS_WIDTH, _TEXT_HEIGHT);
            connectionTextArea.setBackground(mainWindow.getBackground());


            JLabel hostnameFieldLabel = new JLabel("Hostname: ");
            hostnameFieldLabel.setBounds(0, (_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
            JTextField hostnameField = new JTextField("localhost");
            hostnameField.setBounds(100, (_BORDER + _TEXT_HEIGHT), 200, _TEXT_HEIGHT);


            JLabel nameFieldLabel = new JLabel("Username: ");
            nameFieldLabel.setBounds(0, 2*(_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
            JTextField nameField = new JTextField("user");
            nameField.setBounds(100, 2*(_BORDER + _TEXT_HEIGHT), 200, _TEXT_HEIGHT);


            JTextArea errorTextArea = new JTextArea();
            errorTextArea.setBounds(100, 5*(_TEXT_HEIGHT + _BORDER), 400, 5*_TEXT_HEIGHT);
            errorTextArea.setBackground(mainWindow.getBackground());


            JLabel passwordFieldLabel = new JLabel("Password: ");
            passwordFieldLabel.setBounds(0, 3*(_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
            JPasswordField passwordField = new JPasswordField("pass");
            passwordField.setBounds(100, 3*(_TEXT_HEIGHT + _BORDER), 200, _TEXT_HEIGHT);

            JButton connectButton = new JButton("Connect");
            connectButton.setBounds(0, 4*(_TEXT_HEIGHT + _BORDER), 100, _TEXT_HEIGHT);


            ConnectButtonListener connectListener = new ConnectButtonListener(hostnameField, nameField, passwordField, errorTextArea);
            connectButton.addActionListener(connectListener);
            passwordField.addKeyListener(new PasswordEnterListener(connectListener));

            this.add(nameFieldLabel);
            this.add(nameField);
            this.add(errorTextArea);
            this.add(passwordField);
            this.add(passwordFieldLabel);
            this.add(connectButton);
            this.add(hostnameFieldLabel);
            this.add(hostnameField);
            this.add(connectionTextArea);
        }

    }

    /**
     * Listens to the "connect" button, and if connect is successful, deletes the ConnectionPanel and replaces it
     * with a blank canvas. During the action event handling, the InteractiveCanvasManager, and therefore the
     * InteractiveCanvas, is spawned.
     * @author Alex Epstein
     */
    class ConnectButtonListener implements ActionListener {

        JTextField hostnameField;
        JTextField nameField;
        JPasswordField passwordField;
        JTextArea errorTextArea;
        public ConnectButtonListener(JTextField hostnameField, JTextField nameField, JPasswordField passwordField, JTextArea errorTextArea) {
            this.hostnameField = hostnameField;
            this.nameField = nameField;
            this.passwordField = passwordField;
            this.errorTextArea = errorTextArea;
        }
        public void actionPerformed(ActionEvent e) {
            try {
                String rmiRef = "//"+hostnameField.getText()+"/Whiteboard";
                UserIdentity credentials = new UserIdentity(nameField.getText(), passwordField.getText());
                canvasMgr = new InteractiveCanvasManager(rmiRef, credentials, _CANVAS_WIDTH, _CANVAS_HEIGHT, ClientGUI.this);
                canvasMgr.canvas.colourSelected = _DEFAULT_COLOUR;
                canvasMgr.canvas.toolSelected = _DEFAULT_TOOL;
                mainWindow.remove(canvasPanel);
                mainWindow.add(canvasMgr.canvas);
                canvasMgr.canvas.setBounds(0, 0, _CANVAS_WIDTH, _CANVAS_HEIGHT);
                clearButton.setEnabled(canvasMgr.isAdmin());
                kickButton.setEnabled(canvasMgr.isAdmin());

                toolbar.enableAll(true);
                chatEntry.setEnabled(true);

                drawables.setToolTipText("Select the tool to draw with");
                colourButton.setToolTipText("Select the colour to draw with");
                clearButton.setToolTipText(canvasMgr.isAdmin() ? "Clear the canvas" : "Administrator only");
                clearButton.setEnabled(canvasMgr.isAdmin());
                kickButton.setToolTipText(canvasMgr.isAdmin() ? "Kick selected user" : "Administrator only");
                kickButton.setEnabled(canvasMgr.isAdmin());
                disconnectButton.setToolTipText(canvasMgr.isAdmin() ? "Close the server" : "Disconnect from server");

                fileMenuBar.setEnables(true, false, true);

            } catch (NullPointerException g) {
                passwordField.setText("");
                errorTextArea.setText("Server not found");
                errorTextArea.repaint();
            } catch (Exception f) {
                passwordField.setText("");
                errorTextArea.setText("Error " + f);
                errorTextArea.repaint();
            }
        }
    }

    /**
     * Listens for "enter" in the password field.
     * @author Alex Epstein with thanks to https://stackoverflow.com/questions/13731710/allowing-the-enter-key-to-press-the-submit-button-as-opposed-to-only-using-mo
     */
    class PasswordEnterListener implements KeyListener {
        ConnectButtonListener connectListener;

        public PasswordEnterListener(ConnectButtonListener c) {
            this.connectListener = c;
        }
        public void keyTyped(KeyEvent e) {}

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                connectListener.actionPerformed(new ActionEvent(this, 0, "enter"));
            }
        }
        public void keyReleased(KeyEvent e) {}
    }

    class ChatEnterListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {}

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                canvasMgr.sendToChat(chatEntry.getText());
                chatEntry.setText("");
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

}
