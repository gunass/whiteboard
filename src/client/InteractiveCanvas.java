package client;

import drawing.Drawing;
import drawing.FreeLine;
import drawing.Text;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class InteractiveCanvas extends Canvas {

    ArrayList<Drawing> drawings;
    String toolSelected = "Free Line";
    Color colourSelected;
    boolean isDrawing;
    boolean isFreeDrawing;
    FreeLine pendingFreeDrawing;
    Drawing pendingDrawing;
    String username;
    InteractiveCanvasManager manager;


    public InteractiveCanvas(int width, int height, String username, InteractiveCanvasManager manager) {
        this.manager = manager;
        this.setBounds(0,0,width, height);
        this.setBackground(Color.WHITE);
        this.addMouseListener(new InteractiveCanvas.CanvasMouseListener());
        this.addMouseMotionListener(new InteractiveCanvas.CanvasMouseMotionListener());
        this.username = username;
        System.out.println("Canvas created successfully");
    }

    @Override
    public void paint(Graphics g) {
        for (Drawing d : drawings) {
            g.setColor(d.colour);
            d.drawToGraphics(g);
        }
        if (isDrawing) {
            g.setColor(pendingDrawing.colour);
            pendingDrawing.drawToGraphics(g);
        }
        if (isFreeDrawing) {
            g.setColor(pendingFreeDrawing.colour);
            pendingFreeDrawing.drawToGraphics(g);
        }
    }

    private class CanvasMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {

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

        public void mouseReleased(MouseEvent e) {
            if (toolSelected.equals("Text")) {
                return;
            }
            if (isFreeDrawing) {
                pendingFreeDrawing.timestamp = System.currentTimeMillis();
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

        public void mouseExited(MouseEvent e) {
            isDrawing = false;
        }

    }



    private class CanvasMouseMotionListener implements MouseMotionListener {

        public void mouseMoved(MouseEvent e) {
            // If the mouse is simply moved please do nothing
        }

        public void mouseDragged(MouseEvent e) {

            if (isFreeDrawing) {
                pendingFreeDrawing.addPoint(e.getX(), e.getY());
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