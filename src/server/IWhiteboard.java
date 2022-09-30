package server;

import drawing.Drawing;

import java.awt.image.BufferedImage;

public interface IWhiteboard {

    public BufferedImage getCanvas();

    public void drawToCanvas(Drawing drawing);

}
