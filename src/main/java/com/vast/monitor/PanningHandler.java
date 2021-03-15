package com.vast.monitor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class PanningHandler extends MouseAdapter implements MouseListener, MouseMotionListener {
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
