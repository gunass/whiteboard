package client;

import util.UserIdentity;

import javax.swing.*;
import java.awt.*;
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
    private final String[] toolsAvailable = {"Free Line", "Line", "Triangle", "Rectangle", "Circle", "Text"};
    private int menuNextX = _BORDER;

    private Color selectedColour = Color.BLUE;
    private JFrame mainWindow;
    private JPanel canvasPanel;
    private JComboBox<String> drawables;
    private JList<String> usersList;
    private JLabel usersListLabel;
    private JButton colourButton;
    private JPanel colourPanel;
    private JButton clearButton;

    private UserIdentity uid = new UserIdentity("NOUSER", "NOPASS");
    private Socket socket;

    private InteractiveCanvasManager canvasMgr;

    /**
     * Creates a client GUI with all the default features (no refunds)
     */
    public ClientGUI() {

        this.mainWindow = new JFrame();
        mainWindow.setResizable(false);
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.canvasPanel = new JPanel();
        canvasPanel.setLayout(null);
        canvasPanel.setBounds(_BORDER,_BORDER,_CANVAS_WIDTH,_CANVAS_HEIGHT);

        JTextArea connectionTextArea = new JTextArea("Connect to server:");
        connectionTextArea.setBounds(0, 0, _CANVAS_WIDTH, _TEXT_HEIGHT);
        connectionTextArea.setBackground(mainWindow.getBackground());
        canvasPanel.add(connectionTextArea);

        JLabel hostnameFieldLabel = new JLabel("Hostname: ");
        hostnameFieldLabel.setBounds(0, (_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
        JTextField hostnameField = new JTextField("localhost");
        hostnameField.setBounds(100, (_BORDER + _TEXT_HEIGHT), 200, _TEXT_HEIGHT);
        canvasPanel.add(hostnameFieldLabel);
        canvasPanel.add(hostnameField);

        JLabel nameFieldLabel = new JLabel("Username: ");
        nameFieldLabel.setBounds(0, 2*(_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
        JTextField nameField = new JTextField("user");
        nameField.setBounds(100, 2*(_BORDER + _TEXT_HEIGHT), 200, _TEXT_HEIGHT);
        canvasPanel.add(nameFieldLabel);
        canvasPanel.add(nameField);

        JLabel passwordFieldLabel = new JLabel("Password: ");
        passwordFieldLabel.setBounds(0, 3*(_BORDER + _TEXT_HEIGHT), 100, _TEXT_HEIGHT);
        JTextField passwordField = new JPasswordField("pass");
        passwordField.setBounds(100, 3*(_TEXT_HEIGHT + _BORDER), 200, _TEXT_HEIGHT);
        canvasPanel.add(passwordField);
        canvasPanel.add(passwordFieldLabel);

        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(0, 4*(_TEXT_HEIGHT + _BORDER), 100, _TEXT_HEIGHT);

        JTextArea errorTextArea = new JTextArea();
        errorTextArea.setBounds(100, 5*(_TEXT_HEIGHT + _BORDER), 400, 5*_TEXT_HEIGHT);
        errorTextArea.setBackground(mainWindow.getBackground());
        canvasPanel.add(errorTextArea);

        /**
         * Action listener handles the initial socket connection to the server
         * Username, password submitted to server pending a review by the admin
         * FIXME: Currently, as a stub, all join requests are approved
         */
        connectButton.addActionListener(e -> {

            try {
                String rmiRef = "//"+hostnameField.getText()+"/Whiteboard";
                this.uid = new UserIdentity(nameField.getText(), passwordField.getText());
                canvasMgr = new InteractiveCanvasManager(rmiRef, uid, _CANVAS_WIDTH, _CANVAS_HEIGHT);
                canvasMgr.canvas.colourSelected = selectedColour;
                mainWindow.remove(canvasPanel);
                mainWindow.add(canvasMgr.canvas);
                drawables.setEnabled(true);
                colourButton.setEnabled(true);
                clearButton.setEnabled(canvasMgr.isAdmin());

            } catch (Exception f) {
                passwordField.setText("");
                errorTextArea.setText(f.getMessage());
            }

        });

        canvasPanel.add(connectButton);
        mainWindow.add(canvasPanel);

        // Combo box to select the tool in use
        this.drawables = new JComboBox<String>(toolsAvailable);
        drawables.addActionListener(e -> {
            canvasMgr.canvas.toolSelected = (String) drawables.getSelectedItem();
        });
        drawables.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _DRAWABLES_WIDTH, _MENU_AREA_HEIGHT);
        drawables.setEnabled(false);
        menuNextX += _DRAWABLES_WIDTH + _BORDER;
        mainWindow.add(drawables);

        // A button and a panel to select and display the pen colour
        this.colourButton = new JButton("Colour");
        colourButton.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _COLOUR_BUTTON_WIDTH, _MENU_AREA_HEIGHT);
        menuNextX += _COLOUR_BUTTON_WIDTH + _BORDER;
        colourButton.setEnabled(false);

        this.colourPanel = new JPanel();
        colourPanel.setOpaque(true);
        colourPanel.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _MENU_AREA_HEIGHT, _MENU_AREA_HEIGHT);
        menuNextX += _MENU_AREA_HEIGHT + _BORDER;
        colourPanel.setBackground(selectedColour);

        colourButton.addActionListener(e -> {
            selectedColour = JColorChooser.showDialog(colourButton, "Select colour", colourPanel.getBackground());
            colourPanel.setBackground(selectedColour);
            canvasMgr.canvas.colourSelected = selectedColour;
        }
        );

        this.clearButton = new JButton("Clear");
        clearButton.setBounds(menuNextX, _MENU_AREA_ROOT_Y, 50, _MENU_AREA_HEIGHT);
        menuNextX += _MENU_AREA_HEIGHT + _BORDER;
        clearButton.setEnabled(false);
        clearButton.addActionListener(e -> {
            canvasMgr.requestClearCanvas();
        });

        mainWindow.add(clearButton);
        mainWindow.add(colourButton);
        mainWindow.add(colourPanel);

        // A list to show the current users in the server
        this.usersList = new JList<>();
        usersList.setBounds(_BORDER*2 + _CANVAS_WIDTH, _BORDER, _USERS_DISPLAY_WIDTH, _USERS_DISPLAY_HEIGHT);
        mainWindow.add(usersList);

        this.usersListLabel = new JLabel("Active users");
        usersListLabel.setBounds(_BORDER*2 + _CANVAS_WIDTH, _BORDER + _USERS_DISPLAY_HEIGHT, _USERS_DISPLAY_WIDTH, _USERS_DISPLAY_LABEL_HEIGHT);
        mainWindow.add(usersListLabel);

        mainWindow.setSize(_WINDOW_WIDTH,_WINDOW_HEIGHT);
        mainWindow.setLayout(null);
        mainWindow.setVisible(true);
    }


}
