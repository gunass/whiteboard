package client;
import util.MessageFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ClientGUI {

    private final int _CANVAS_HEIGHT = 500;
    private final int _CANVAS_WIDTH = 500;
    private final int _BORDER = 10;
    private final int _MENU_AREA_HEIGHT = 20;
    private final int _TEXT_HEIGHT = 20;
    private final int _DRAWABLES_WIDTH = 125;
    private final int _COLOUR_BUTTON_WIDTH = 60;
    private final int _USERS_DISPLAY_LABEL_HEIGHT = 25;
    private final int _USERS_DISPLAY_WIDTH = 100;
    private final int _USERS_DISPLAY_HEIGHT = _CANVAS_HEIGHT;
    private final int _WINDOW_WIDTH = _CANVAS_WIDTH + 3*_BORDER + _USERS_DISPLAY_WIDTH;
    private final int _WINDOW_HEIGHT = _CANVAS_HEIGHT + 4*_BORDER + 2*_MENU_AREA_HEIGHT;
    private final int _MENU_AREA_ROOT_Y = _CANVAS_HEIGHT + 2*_BORDER;
    private String[] users = {"Testel", "Testessa", "Testley"};
    private final String[] toolsAvailable = {"Free Line", "Line", "Triangle", "Rectangle", "Circle", "Text"};
    private int menuNextX = _BORDER;
    private String toolSelected = toolsAvailable[0];

    private Color selectedColour = Color.BLUE;
    private JFrame mainWindow;
    private JPanel canvasPanel;
    private JComboBox drawables;
    private JList<String> usersList;
    private JLabel usersListLabel;
    private JButton colourButton;
    private JPanel colourPanel;

    private String username = "_NOUSER_";
    private String password = "_NOPASS_";
    private Socket socket;

    /**
     * Creates a client GUI with all the default features (no refunds)
     * @param hostname
     * @param port
     * @param username
     */
    public ClientGUI(String hostname, int port, String username) {

        this.mainWindow = new JFrame();
        mainWindow.setResizable(false);

        this.canvasPanel = new JPanel();
        canvasPanel.setLayout(null);
        canvasPanel.setBounds(_BORDER,_BORDER,_CANVAS_WIDTH,_CANVAS_HEIGHT);

        JTextArea connectionTextArea = new JTextArea("Connect to server:");
        connectionTextArea.setBounds(0, 0, _CANVAS_WIDTH, _TEXT_HEIGHT);
        connectionTextArea.setBackground(mainWindow.getBackground());
        canvasPanel.add(connectionTextArea);

        JLabel hostnameFieldLabel = new JLabel("Hostname: ");
        hostnameFieldLabel.setBounds(0, (_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
        JTextField hostnameField = new JTextField(hostname);
        hostnameField.setBounds(100, (_BORDER + _TEXT_HEIGHT), 200, _TEXT_HEIGHT);
        canvasPanel.add(hostnameFieldLabel);
        canvasPanel.add(hostnameField);

        JLabel portFieldLabel = new JLabel("Port: ");
        portFieldLabel.setBounds(0, 2*(_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
        JTextField portField = new JTextField(port + "");
        portField.setBounds(100, 2*(_BORDER + _TEXT_HEIGHT), 200, _TEXT_HEIGHT);
        canvasPanel.add(portFieldLabel);
        canvasPanel.add(portField);

        JLabel nameFieldLabel = new JLabel("Username: ");
        nameFieldLabel.setBounds(0, 3*(_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
        JTextField nameField = new JTextField(username);
        nameField.setBounds(100, 3*(_BORDER + _TEXT_HEIGHT), 200, _TEXT_HEIGHT);
        canvasPanel.add(nameFieldLabel);
        canvasPanel.add(nameField);

        JLabel passwordFieldLabel = new JLabel("Password: ");
        passwordFieldLabel.setBounds(0, 4*(_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
        JTextField passwordField = new JPasswordField();
        passwordField.setBounds(100, 4*(_TEXT_HEIGHT + _BORDER), 200, _TEXT_HEIGHT);
        canvasPanel.add(passwordField);
        canvasPanel.add(passwordFieldLabel);

        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(0, 5*(_TEXT_HEIGHT + _BORDER), 100, _TEXT_HEIGHT);

        JTextArea errorTextArea = new JTextArea();
        errorTextArea.setBounds(100, 5*(_TEXT_HEIGHT + _BORDER), 200, _TEXT_HEIGHT);
        errorTextArea.setBackground(mainWindow.getBackground());
        canvasPanel.add(errorTextArea);

        /**
         * Action listener handles the initial socket connection to the server
         * Username, password submitted to server pending a review by the admin
         * FIXME: Currently, as a stub, all join requests are approved
         */
        connectButton.addActionListener(e -> {
            String msg = MessageFactory.createMessage(MessageFactory.MessageType.JOIN_REQUEST, username, passwordField.getText(), "");
            String reply = "";
            try {
                socket = new Socket(hostname, port);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                MessageFactory.writeMsg(bw, msg);
                reply = MessageFactory.readMsg(br);
                socket.close();
                br.close();
                bw.close();
            } catch (IOException ex) {
                System.out.println("Socket received an error");
                errorTextArea.setText("Error connecting to server");
                passwordField.setText("");
                return;
            }
            // Test if the reply starts with SUCCESS
            String expected = MessageFactory.createReply(MessageFactory.MessageType.SUCCESS_REPLY, "");
            if (!reply.startsWith(expected)) {
                System.out.println("Connection rejected");
                errorTextArea.setText("Connection rejected");
                passwordField.setText("");
                return;
            }
            // If success, clear the panel
            mainWindow.remove(canvasPanel);
            // Store the successful credentials
            this.username = username;
            this.password = passwordField.getText();
            // Take the RMI reference from the reply and boot up the interactive canvas
            String rmiRef = reply.replace(expected + ":", "");
            InteractiveCanvas canvas = new InteractiveCanvas(hostname, rmiRef);
            canvas.setBounds(canvasPanel.getBounds());
            mainWindow.add(canvas);
        });

        canvasPanel.add(connectButton);
        mainWindow.add(canvasPanel);

        // Combo box to select the tool in use
        String drawings[] = toolsAvailable;
        this.drawables = new JComboBox<String>(drawings);
        drawables.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _DRAWABLES_WIDTH, _MENU_AREA_HEIGHT);
        menuNextX += _DRAWABLES_WIDTH + _BORDER;
        mainWindow.add(drawables);

        // A button and a panel to select and display the pen colour
        this.colourButton = new JButton("Colour");
        colourButton.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _COLOUR_BUTTON_WIDTH, _MENU_AREA_HEIGHT);
        menuNextX += _COLOUR_BUTTON_WIDTH + _BORDER;

        this.colourPanel = new JPanel();
        colourPanel.setOpaque(true);
        colourPanel.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _MENU_AREA_HEIGHT, _MENU_AREA_HEIGHT);
        menuNextX += _MENU_AREA_HEIGHT + _BORDER;
        colourPanel.setBackground(selectedColour);

        colourButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(colourButton, "Select colour", colourPanel.getBackground());
                    colourPanel.setBackground(color);
        }
        );

        mainWindow.add(colourButton);
        mainWindow.add(colourPanel);

        // A list to show the current users in the server
        this.usersList = new JList<>(users);
        usersList.setBounds(_BORDER*2 + _CANVAS_WIDTH, _BORDER, _USERS_DISPLAY_WIDTH, _USERS_DISPLAY_HEIGHT);
        mainWindow.add(usersList);

        this.usersListLabel = new JLabel("Active users");
        usersListLabel.setBounds(_BORDER*2 + _CANVAS_WIDTH, _BORDER + _USERS_DISPLAY_HEIGHT, _USERS_DISPLAY_WIDTH, _USERS_DISPLAY_LABEL_HEIGHT);
        mainWindow.add(usersListLabel);

        mainWindow.setSize(_WINDOW_WIDTH,_WINDOW_HEIGHT);
        mainWindow.setLayout(null);
        mainWindow.setVisible(true);
    }

    /**
     * FIXME: stub.
     * An interactive canvas JComponent that displays the image obtained from the remote
     * canvas and responds to user interaction by creating drawings.
     */
    public class InteractiveCanvas extends JPanel {

        String hostname;
        String rmiReference;
        public InteractiveCanvas(String hostname, String rmiReference) {
            JTextArea loadingText = new JTextArea("Under construction.");
            this.add(loadingText);
        }

    }


}
