package com.vast.monitor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class MonitorCanvas extends JComponent {
    public double translateX;
    public double translateY;
    public double scale;
    public AffineTransform at;

    private MonitorWorld monitorWorld;

    public MonitorCanvas() {
        translateX = 0;
        translateY = 0;
        scale = 1;

        PanningHandler panner = new PanningHandler(this);
        addMouseListener(panner);
        addMouseMotionListener(panner);
    }

    public void setMonitorWorld(MonitorWorld monitorWorld) {
        this.monitorWorld = monitorWorld;
    }

    public void paintComponent(Graphics g) {
        Graphics2D ourGraphics = (Graphics2D) g;
        // save the original transform so that we can restore
        // it later
        AffineTransform saveTransform = ourGraphics.getTransform();

        // blank the screen. If we do not call super.paintComponent, then
        // we need to blank it ourselves
        ourGraphics.setColor(new Color(0x2b, 0x2b, 0x2b));
        ourGraphics.fillRect(0, 0, getWidth(), getHeight());

        // We need to add new transforms to the existing
        // transform, rather than creating a new transform from scratch.
        // If we create a transform from scratch, we will
        // will start from the upper left of a JFrame,
        // rather than from the upper left of our component
        at = new AffineTransform(saveTransform);

        // The zooming transformation. Notice that it will be performed
        // after the panning transformation, zooming the panned scene,
        // rather than the original scene
        at.translate(getWidth() / 2, getHeight() / 2);
        at.scale(scale, scale);
        at.translate(-getWidth() / 2, -getHeight() / 2);

        // The panning transformation
        at.translate(translateX, translateY);

        ourGraphics.setTransform(at);

        // Draw the objects
        if (monitorWorld != null) {
            monitorWorld.paint(ourGraphics);
        }

        // make sure you restore the original transform or else the drawing
        // of borders and other components might be messed up
        ourGraphics.setTransform(saveTransform);
    }

    public Dimension getPreferredSize() {
        if (monitorWorld != null) {
            return new Dimension(monitorWorld.getSize().x, monitorWorld.getSize().y);
        } else {
            return new Dimension();
        }
    }
}
