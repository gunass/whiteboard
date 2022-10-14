package client;

import drawing.Drawing;
import drawing.FreeLine;
import drawing.Text;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Stack;

/**
 * Interactive canvas object responsible for drawing and displaying all shapes.
 * Using the GUI, user selects a tool and colour, then by interacting with the canvas the drawing is
 * rendered. When the drawing is finished (e.g. mouse released) it is sent to the server.
 * @author Alex Epstein
 */
public class InteractiveCanvas extends Canvas implements Serializable {

    // Stack of complete drawings not yet drawn to the canvas flat
    Stack<Drawing> drawings;

    // Tool selected in the GUI
    String toolSelected = "Free Line";
    // Colour selected in the GUI
    Color colourSelected;
    // Enabled when the user is drawing a Shape (i.e. not a FreeLine or Text)
    boolean isDrawing;
    // Enabled when the user is drawing a FreeLine
    boolean isFreeDrawing;
    // Enabled when the user is typing
    boolean isTyping;
    // A raw representation of the mouse path when drawing FreeLine
    FreeLine pendingFreeDrawing;
    // The pending representation of the Shape being drawn
    Drawing pendingDrawing;
    // The user's name (used to sign drawings)
    TextEntryDialog pendingText;
    String username;
    // The ICM
    InteractiveCanvasManager manager;
    // The image file that stores all the drawn drawings
    BufferedImage canvasFlat;


    /**
     * Create a new interactive canvas with default size, colour, and listeners
     * @param width
     * @param height
     * @param username
     * @param manager
     */
    public InteractiveCanvas(int width, int height, String username, InteractiveCanvasManager manager) {
        this.manager = manager;
        this.setBounds(0,0,width, height);
        this.setBackground(Color.WHITE);
        this.addMouseListener(new InteractiveCanvas.CanvasMouseListener());
        this.addMouseMotionListener(new InteractiveCanvas.CanvasMouseMotionListener());
        this.username = username;
        this.canvasFlat = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        System.out.println("Canvas created successfully");
    }

    /**
     * Renders each drawing in sequence, including incomplete/pending drawings
     * @param g   the specified Graphics context
     */
    @Override
    public void paint(Graphics g) {

        // Paint all new drawings to the canvas flat
        while (!drawings.isEmpty()) {
            Drawing d = drawings.pop();
            d.drawToGraphics(canvasFlat.getGraphics());
        }

        g.drawImage(canvasFlat, 0, 0, this);

        // Draw the pending drawing to the canvas flat (live!)
        if (isDrawing) {
            g.setColor(pendingDrawing.colour);
            pendingDrawing.drawToGraphics(g);
        }
        if (isFreeDrawing) {
            g.setColor(pendingFreeDrawing.colour);
            pendingFreeDrawing.drawToGraphics(g);
        }
    }

    public class TextEntryDialog extends JDialog {

        public int x;
        public int y;
        private JTextField textEntryField;
        public TextEntryDialog() {
            setSize(300,50);
            textEntryField = new JTextField();
            textEntryField.addKeyListener(new EnterListener());
            this.add(textEntryField);
            this.setVisible(false);
        }

        public void submit() {
            Text text = new Text(username, System.currentTimeMillis(), colourSelected);
            text.startx = x;
            text.starty = y;
            text.setCharArray(textEntryField.getText());
            manager.sendDrawing(text);
        }

        public class EnterListener implements KeyListener {

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    TextEntryDialog.this.submit();
                    textEntryField.setText("");
                    TextEntryDialog.this.setVisible(false);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        }
    }

    /**
     * Listens for clicks and releases (to draw)
     */
    private class CanvasMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {

            if (toolSelected.equals("Text")) {
                isTyping = true;
                if (pendingText == null) {
                    pendingText = new TextEntryDialog();
                }
                pendingText.x = e.getX();
                pendingText.y = e.getY();
                pendingText.setVisible(true);
            }
        }

        public void mousePressed(MouseEvent e) {

            if (toolSelected.equals("Text")) {
                return;
            } else if (toolSelected.equals("Free Line")) {
                pendingFreeDrawing = new drawing.FreeLine(username, 0, colourSelected);
                pendingFreeDrawing.colour = colourSelected;
                isFreeDrawing = true;
                return;
            } else {
                isDrawing = true;
                switch (toolSelected) {
                    case "Circle":
                        pendingDrawing = new drawing.Circle(username, 0, colourSelected);
                        break;
                    case "Triangle":
                        pendingDrawing = new drawing.Triangle(username, 0, colourSelected);
                        break;
                    case "Line":
                        pendingDrawing = new drawing.Line(username, 0, colourSelected);
                        break;
                    case "Rectangle":
                        pendingDrawing = new drawing.Rectangle(username, 0, colourSelected);
                        break;
                }
                pendingDrawing.colour = colourSelected;
                pendingDrawing.startx = e.getX();
                pendingDrawing.starty = e.getY();
                pendingDrawing.endx = e.getX();
                pendingDrawing.endy = e.getY();
            }
        }

        // If drawing, mouse release => finish drawing. Sends to server instantly
        public void mouseReleased(MouseEvent e) {
            if (toolSelected.equals("Text")) {
                return;
            }
            if (isFreeDrawing) {
                pendingFreeDrawing.timestamp = System.currentTimeMillis();
                // De-interpolate the drawing to minimise object size (4 bytes per point!)
                pendingFreeDrawing.optimise(5);
                manager.sendDrawing(pendingFreeDrawing);
                isFreeDrawing = false;
                return;
            }
            if (isDrawing) {
                pendingDrawing.endx = e.getX();
                pendingDrawing.endy = e.getY();

                pendingDrawing.timestamp = System.currentTimeMillis();
                manager.sendDrawing(pendingDrawing);
                isDrawing = false;
                return;
            }
        }

        public void mouseEntered(MouseEvent e) {

        }

        // If mouse exits, stop drawing
        // Note that this has no effect on FreeLines
        public void mouseExited(MouseEvent e) {
            //isDrawing = false;
        }

    }


    /**
     * Listens for mouse movement to draw FreeLine objects or to continuously render pending Shape objects
     */
    private class CanvasMouseMotionListener implements MouseMotionListener {

        public void mouseMoved(MouseEvent e) {
            // If the mouse is simply moved please do nothing
        }

        public void mouseDragged(MouseEvent e) {

            if (isFreeDrawing) {
                pendingFreeDrawing.addPoint((short) e.getX(), (short) e.getY());
                InteractiveCanvas.this.repaint();
            }

            if (isDrawing && !toolSelected.equals("Free Line")) {
                pendingDrawing.endx = e.getX();
                pendingDrawing.endy = e.getY();
                InteractiveCanvas.this.repaint();
            }
        }
    }




}