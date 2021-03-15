package com.vast.monitor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ScaleHandler implements ChangeListener {
    private MonitorCanvas canvas;

    public ScaleHandler(MonitorCanvas canvas) {
        this.canvas = canvas;
    }

    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider) e.getSource();
        int zoomPercent = slider.getValue();
        // make sure zoom never gets to actual 0, or else the objects will
        // disappear and the matrix will be non-invertible.
        canvas.scale = Math.max(0.00001, zoomPercent / 100.0);
        canvas.repaint();
    }
}
