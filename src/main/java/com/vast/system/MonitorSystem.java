package com.vast.system;

import com.artemis.BaseSystem;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.vast.VastWorld;
import com.vast.monitor.Monitor;

import javax.swing.*;

public class MonitorSystem extends BaseSystem {
    private VastWorld vastWorld;
    private float interval;

    private float acc;
    private float intervalDelta;
    private Monitor monitor;

    public MonitorSystem(VastWorld vastWorld, float interval) {
        this.vastWorld = vastWorld;
        this.interval = interval;
    }

    @Override
    protected void initialize() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                FlatDarkLaf.install();
                FlatInspector.install("ctrl shift alt X");
                monitor = new Monitor(vastWorld);
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void dispose() {
        if (monitor != null) {
            monitor.dispose();
        }
    }

    @Override
    protected void processSystem() {
        if (monitor != null) {
            monitor.sync();
        }
    }

    @Override
    protected boolean checkProcessing() {
        acc += getTimeDelta();
        if(acc >= interval) {
            acc -= interval;
            intervalDelta = (acc - intervalDelta);

            return true;
        }
        return false;
    }

    protected float getTimeDelta() {
        return world.getDelta();
    }
}
