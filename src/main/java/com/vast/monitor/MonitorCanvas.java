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

    public double translateX;
    public double translateY;
    public double scale;
    public AffineTransform at;
    private Monitor monitor;

    public MonitorCanvas(Monitor monitor, MonitorWorld monitorWorld) {
        this.monitor = monitor;
        this.monitorWorld = monitorWorld;

        translateX = 0;
        translateY = 0;
        scale = 1;

        PanningHandler panningHandler = new PanningHandler(this);
        addMouseListener(panningHandler);
        addMouseMotionListener(panningHandler);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        AffineTransform saveTransform = g2d.getTransform();

        g2d.setColor(new Color(0x2b, 0x2b, 0x2b));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        at = new AffineTransform(saveTransform);

        at.translate(monitor.getWidth() / 2f, monitor.getHeight() / 2f);
        at.scale(scale, scale);
        at.translate(-monitor.getWidth() / 2f, -monitor.getHeight() / 2f);

        at.translate(translateX, translateY);

        g2d.setTransform(at);

        synchronized (monitorWorld) {
            monitorWorld.paint(g2d);
        }

        g2d.setTransform(saveTransform);
    }

    public Dimension getPreferredSize() {
        return new Dimension(monitorWorld.getSize().x, monitorWorld.getSize().y);
    }

    class PanningHandler extends MouseAdapter implements MouseListener, MouseMotionListener {
        private MonitorCanvas canvas;

        private AffineTransform initialTransform;
        private Point2D pressedPoint;
        private Point2D canvasStartPoint;

        public PanningHandler(MonitorCanvas canvas) {
            this.canvas = canvas;

            canvasStartPoint = new Point();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            try {
                pressedPoint = canvas.at.inverseTransform(e.getPoint(), null);
            }
            catch (NoninvertibleTransformException exception) {
                return;
            }

            initialTransform = canvas.at;
            canvasStartPoint.setLocation(canvas.translateX, canvas.translateY);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point2D mousePoint;
            try {
                mousePoint = initialTransform.inverseTransform(e.getPoint(), null);
            }
            catch (NoninvertibleTransformException exception) {
                return;
            }

            canvas.translateX = canvasStartPoint.getX() +
                    (mousePoint.getX() - pressedPoint.getX()) * initialTransform.getScaleX() / canvas.scale;
            canvas.translateY = canvasStartPoint.getY() +
                    (mousePoint.getY() - pressedPoint.getY()) * initialTransform.getScaleY() / canvas.scale;

            canvas.repaint();
        }
    }
}
