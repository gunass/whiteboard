package client;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI {

    private final int _CANVAS_HEIGHT = 500;
    private final int _CANVAS_WIDTH = 500;
    private final int _BORDER = 10;
    private final int _MENU_AREA_HEIGHT = 20;
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

    public ClientGUI(String hostname, int port, String username) {

        JFrame mainWindow = new JFrame();

        // A "loading" screen! Highly temporary.
        JTextArea connectionTextArea = new JTextArea("Connecting to server " + hostname + ":" + port
        + " with username " + username);
        connectionTextArea.setBounds(_BORDER,_BORDER,_CANVAS_WIDTH,_CANVAS_HEIGHT);
        mainWindow.add(connectionTextArea);

        // Combo box to select the tool in use
        String drawings[] = toolsAvailable;
        JComboBox<String> drawables = new JComboBox<String>(drawings);
        drawables.addActionListener(e -> {
            toolSelected = drawables.getItemAt(drawables.getSelectedIndex());
        });
        drawables.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _DRAWABLES_WIDTH, _MENU_AREA_HEIGHT);
        menuNextX += _DRAWABLES_WIDTH + _BORDER;
        mainWindow.add(drawables);

        // A button and a panel to select and display the pen colour
        JButton colourButton = new JButton("Colour");
        colourButton.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _COLOUR_BUTTON_WIDTH, _MENU_AREA_HEIGHT);
        menuNextX += _COLOUR_BUTTON_WIDTH + _BORDER;

        JPanel colourDisplay = new JPanel();
        colourDisplay.setOpaque(true);
        colourDisplay.setBounds(menuNextX, _MENU_AREA_ROOT_Y, _MENU_AREA_HEIGHT, _MENU_AREA_HEIGHT);
        menuNextX += _MENU_AREA_HEIGHT + _BORDER;
        colourDisplay.setBackground(selectedColour);

        colourButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(colourButton, "Select colour", colourDisplay.getBackground());
            colourDisplay.setBackground(color);
        }
        );

        mainWindow.add(colourButton);
        mainWindow.add(colourDisplay);

        // A list to show the current users in the server
        JList<String> usersList = new JList<>(users);
        usersList.setBounds(_BORDER*2 + _CANVAS_WIDTH, _BORDER, _USERS_DISPLAY_WIDTH, _USERS_DISPLAY_HEIGHT);
        mainWindow.add(usersList);

        JLabel usersListLabel = new JLabel("Active users");
        usersListLabel.setBounds(_BORDER*2 + _CANVAS_WIDTH, _BORDER + _USERS_DISPLAY_HEIGHT, _USERS_DISPLAY_WIDTH, _USERS_DISPLAY_LABEL_HEIGHT);
        mainWindow.add(usersListLabel);

        mainWindow.setSize(_WINDOW_WIDTH,_WINDOW_HEIGHT);
        mainWindow.setLayout(null);
        mainWindow.setVisible(true);
    }



}
