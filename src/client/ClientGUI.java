package client;

import util.UserIdentity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;

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
    private final int _DRAWABLES_WIDTH = 125;
    private final int _COLOUR_BUTTON_WIDTH = 60;
    private final int _USERS_DISPLAY_LABEL_HEIGHT = 25;
    private final int _USERS_DISPLAY_WIDTH = 100;
    private final int _USERS_DISPLAY_HEIGHT = _CANVAS_HEIGHT;
    private final int _WINDOW_WIDTH = _CANVAS_WIDTH + 3*_BORDER + _USERS_DISPLAY_WIDTH;
    private final int _WINDOW_HEIGHT = _CANVAS_HEIGHT + 3*_BORDER + _MENU_AREA_HEIGHT;
    private final int _MENU_AREA_ROOT_Y = _CANVAS_HEIGHT;
    private final String[] toolsAvailable = {"Free Line", "Line", "Triangle", "Rectangle", "Circle", "Text"};
    private JButton disconnectButton;
    private int menuNextX = _BORDER;

    private final Color _DEFAULT_COLOUR = Color.BLUE;
    private final String _DEFAULT_TOOL = "Free Line";
    private JFrame mainWindow;
    private JPanel canvasPanel;
    private JComboBox<String> drawables;
    private JList<String> usersList;
    private JLabel usersListLabel;
    private JButton colourButton;
    private JPanel colourPanel;
    private JButton clearButton;

    private Socket socket;

    private InteractiveCanvasManager canvasMgr;
    private ToolBar toolbar;
    private UsersList usersListPanel;

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

        toolbar.setBounds(0, _MENU_AREA_ROOT_Y, _CANVAS_WIDTH, _MENU_AREA_HEIGHT);
        usersListPanel.setBounds(_CANVAS_WIDTH + _BORDER, 0, _USERS_DISPLAY_WIDTH, _WINDOW_HEIGHT);

        mainWindow.add(canvasPanel);
        mainWindow.add(toolbar);
        mainWindow.add(usersListPanel);

        // A list to show the current users in the server


        mainWindow.setSize(_WINDOW_WIDTH,_WINDOW_HEIGHT);
        mainWindow.setLayout(null);
        mainWindow.setVisible(true);
    }

    private class UsersList extends JPanel {

        public UsersList() {

            this.setLayout(new FlowLayout());
            usersList = new JList<String>(new String[] {"Jerry", "Terry"});
            usersList.setFixedCellWidth(_USERS_DISPLAY_WIDTH);
            usersList.setBounds(0,0,_USERS_DISPLAY_WIDTH, _USERS_DISPLAY_HEIGHT);

            usersListLabel = new JLabel("Active users");

            this.add(usersList);
            this.add(usersListLabel);
        }

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
            drawables.setEnabled(false);

            // A button and a panel to select and display the pen colour
            colourButton = new JButton("Colour");
            colourButton.setEnabled(false);

            colourPanel = new JPanel();
            colourPanel.setOpaque(true);
            colourPanel.setBackground(_DEFAULT_COLOUR);

            colourButton.addActionListener(e -> {
                        Color colour = JColorChooser.showDialog(colourButton, "Select colour", canvasMgr.canvas.colourSelected);
                        colourPanel.setBackground(colour);
                        canvasMgr.canvas.colourSelected = colour;
                    }
            );

            clearButton = new JButton("Clear");
            clearButton.setEnabled(false);
            clearButton.addActionListener(e -> {
                canvasMgr.requestClearCanvas();
            });

            disconnectButton = new JButton("Disconnect");
            disconnectButton.setEnabled(false);
            disconnectButton.addActionListener(e -> {
                // FIXME: actually disconnect from the server
                mainWindow.remove(canvasMgr.canvas);
                mainWindow.add(new ConnectionPanel());
            });

            this.add(drawables);
            this.add(colourButton);
            this.add(colourPanel);
            this.add(clearButton);
            this.add(disconnectButton);
        }

    }

    /**
     * The default starting panel of the application which allows the user to submit their credentials as a form.
     * @author Alex Epstein
     */
    private class ConnectionPanel extends JPanel {

        public ConnectionPanel() {
            this.setLayout(null);
            this.setBounds(_BORDER,_BORDER,_CANVAS_WIDTH,_CANVAS_HEIGHT);

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
     * with a blank canvas. During the action event handling, the InteractiveCanvasManager is spawned.
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
                canvasMgr = new InteractiveCanvasManager(rmiRef, credentials, _CANVAS_WIDTH, _CANVAS_HEIGHT);
                canvasMgr.canvas.colourSelected = _DEFAULT_COLOUR;
                canvasMgr.canvas.toolSelected = _DEFAULT_TOOL;
                mainWindow.remove(canvasPanel);
                mainWindow.add(canvasMgr.canvas);
                drawables.setEnabled(true);
                colourButton.setEnabled(true);
                clearButton.setEnabled(canvasMgr.isAdmin());
                disconnectButton.setEnabled(true);

            } catch (Exception f) {
                passwordField.setText("");
                errorTextArea.setText(f.getMessage());
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

}
