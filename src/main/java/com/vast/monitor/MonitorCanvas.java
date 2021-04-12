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
    private final MonitorWorld monitorWorld;

    private Dimension preferredSize;
    private double translateX;
    private double translateY;
    private double scale;
    private AffineTransform at;
    private Rectangle lastClipBounds;

    public MonitorCanvas(MonitorWorld monitorWorld) {
        this.monitorWorld = monitorWorld;

        preferredSize = new Dimension(monitorWorld.getSize().x, monitorWorld.getSize().y);
        translateX = 0;
        translateY = 0;
        scale = 1;

        PanningHandler panningHandler = new PanningHandler(this);
        addMouseListener(panningHandler);
        addMouseMotionListener(panningHandler);
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Rectangle getLastClipBounds() {
        return lastClipBounds;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        AffineTransform saveTransform = g2d.getTransform();

        g2d.setColor(new Color(0x2b, 0x2b, 0x2b));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        at = new AffineTransform(saveTransform);

        at.translate(getWidth() / 2f, getHeight() / 2f);
        at.scale(scale, scale);
        at.translate(-getWidth() / 2f, -getHeight() / 2f);

        at.translate(translateX, translateY);

        g2d.setTransform(at);

        synchronized (monitorWorld) {
            monitorWorld.paint(g2d);
        }

        lastClipBounds = g2d.getClipBounds();

        g2d.setTransform(saveTransform);
    }

    public Point convertPointToWorld(Point point) {
        double x = point.x;
        double y = point.y;

        x -= (1f - scale) * getWidth() / 2;
        y -= (1f - scale) * getHeight() / 2;

        x -= translateX * scale;
        y -= translateY * scale;

        x /= scale;
        y /= scale;

        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    class PanningHandler extends MouseAdapter implements MouseListener, MouseMotionListener {
        private MonitorCanvas monitorCanvas;

        private AffineTransform initialTransform;
        private Point2D pressedPoint;
        private Point2D canvasStartPoint;

        public PanningHandler(MonitorCanvas monitorCanvas) {
            this.monitorCanvas = monitorCanvas;

            canvasStartPoint = new Point();
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            try {
                pressedPoint = monitorCanvas.at.inverseTransform(mouseEvent.getPoint(), null);
            }
            catch (NoninvertibleTransformException exception) {
                return;
            }

            initialTransform = monitorCanvas.at;
            canvasStartPoint.setLocation(monitorCanvas.translateX, monitorCanvas.translateY);
        }

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            Point2D mousePoint;
            try {
                mousePoint = initialTransform.inverseTransform(mouseEvent.getPoint(), null);
            }
            catch (NoninvertibleTransformException exception) {
                return;
            }

            monitorCanvas.translateX = canvasStartPoint.getX() +
                    (mousePoint.getX() - pressedPoint.getX()) * initialTransform.getScaleX() / monitorCanvas.scale;
            monitorCanvas.translateY = canvasStartPoint.getY() +
                    (mousePoint.getY() - pressedPoint.getY()) * initialTransform.getScaleY() / monitorCanvas.scale;

            monitorCanvas.repaint();
        }
    }
}
