package server;

import drawing.Drawing;

import java.awt.image.BufferedImage;

public interface IWhiteboard {

    public BufferedImage getCanvas(String u, String p);

    public void drawToCanvas(String u, String p, Drawing drawing);

}
