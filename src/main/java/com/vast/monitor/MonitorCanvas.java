package com.vast.monitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class MonitorCanvas extends JComponent {
    public double translateX;
    public double translateY;
    public double scale;
    public AffineTransform at;

    private final MonitorWorld monitorWorld;

    public MonitorCanvas(MonitorWorld monitorWorld) {
        this.monitorWorld = monitorWorld;

        translateX = 0;
        translateY = 0;
        scale = 1;

        PanningHandler panningHandler = new PanningHandler(this);
        addMouseListener(panningHandler);
        addMouseMotionListener(panningHandler);
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
        at.translate(getWidth() / 2f, getHeight() / 2f);
        at.scale(scale, scale);
        at.translate(-getWidth() / 2f, -getHeight() / 2f);

        // The panning transformation
        at.translate(translateX, translateY);

        ourGraphics.setTransform(at);

        // Draw the objects
        synchronized (monitorWorld) {
            monitorWorld.paint(ourGraphics);
        }

        // make sure you restore the original transform or else the drawing
        // of borders and other components might be messed up
        ourGraphics.setTransform(saveTransform);
    }

    public Dimension getPreferredSize() {
        return new Dimension(monitorWorld.getSize().x, monitorWorld.getSize().y);
    }

    class PanningHandler extends MouseAdapter implements MouseListener, MouseMotionListener {
        private MonitorCanvas canvas;

        private double referenceX;
        private double referenceY;
        // saves the initial transform at the beginning of the pan interaction
        private AffineTransform initialTransform;
        private Point2D XFormedPoint; // storage for a transformed mouse point

        public PanningHandler(MonitorCanvas canvas) {
            this.canvas = canvas;
        }

        // capture the starting point
        public void mousePressed(MouseEvent e) {
            // first transform the mouse point to the pan and zoom
            // coordinates
            try {
                XFormedPoint = canvas.at.inverseTransform(e.getPoint(), null);
            }
            catch (NoninvertibleTransformException ignored) {
            }

            // save the transformed starting point and the initial
            // transform
            referenceX = XFormedPoint.getX();
            referenceY = XFormedPoint.getY();
            initialTransform = canvas.at;
        }

        public void mouseDragged(MouseEvent e) {

            // first transform the mouse point to the pan and zoom
            // coordinates. We must take care to transform by the
            // initial transform, not the updated transform, so that
            // both the initial reference point and all subsequent
            // reference points are measured against the same origin.
            try {
                XFormedPoint = initialTransform.inverseTransform(e.getPoint(), null);
            }
            catch (NoninvertibleTransformException ignored) {
            }

            // the size of the pan translations
            // are defined by the current mouse location subtracted
            // from the reference location
            double deltaX = XFormedPoint.getX() - referenceX;
            double deltaY = XFormedPoint.getY() - referenceY;

            // make the reference point be the new mouse point.
            referenceX = XFormedPoint.getX();
            referenceY = XFormedPoint.getY();

            canvas.translateX += deltaX;
            canvas.translateY += deltaY;

            // schedule a repaint.
            canvas.repaint();
        }
    }
}
