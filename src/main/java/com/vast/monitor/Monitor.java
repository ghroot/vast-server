package com.vast.monitor;

import com.vast.VastWorld;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class Monitor extends JFrame implements ActionListener {
    private VastWorld vastWorld;

    private MonitorCanvas canvas;
    private JSlider zoomSlider;
    private SystemMetricsModel systemMetricsTableModel;
    private JTable systemMetricsTable;

    private Timer timer;

    private Map<String, Boolean> debugSettings;
    private final MonitorWorld monitorWorld;
    private Point2D clickPoint;

    public Monitor(VastWorld vastWorld) {
        super("Vast Monitor");
        this.vastWorld = vastWorld;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 800);

        monitorWorld = createMonitorWorld();

        setupUI();
        setupMenu();
        setVisible(true);
        startTimer();
    }

    private MonitorWorld createMonitorWorld() {
        debugSettings = new HashMap<>();
        return new MonitorWorld(vastWorld, debugSettings);
    }

    private void setupUI() {
        canvas = new MonitorCanvas(monitorWorld);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    clickPoint = canvas.at.inverseTransform(e.getPoint(), null);
                    clickPoint.setLocation(clickPoint.getX(), clickPoint.getY() + zoomSlider.getHeight() / canvas.scale);
                } catch (NoninvertibleTransformException noninvertibleTransformException) {
                    noninvertibleTransformException.printStackTrace();
                }
            }
        });
        getContentPane().add(canvas, BorderLayout.CENTER);

        zoomSlider = new JSlider(JSlider.HORIZONTAL, 50, 500, 100);
        zoomSlider.setMajorTickSpacing(25);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            int zoomPercent = slider.getValue();
            canvas.scale = Math.max(0.00001, zoomPercent / 100.0);
            canvas.repaint();
        });
        getContentPane().add(zoomSlider, BorderLayout.NORTH);

        systemMetricsTableModel = new SystemMetricsModel(vastWorld.getMetrics());
        systemMetricsTable = new JTable(systemMetricsTableModel);
        systemMetricsTable.getColumn("System").setPreferredWidth(150);
        systemMetricsTable.getColumn("Time").setPreferredWidth(30);
        systemMetricsTable.getColumn("Entities").setPreferredWidth(50);
    }

    private void setupMenu() {
        MenuBar menuBar = new MenuBar();

        Menu viewMenu = new Menu("View");
        CheckboxMenuItem systemMetricsMenuItem = new CheckboxMenuItem("System Metrics");
        systemMetricsMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_S));
        systemMetricsMenuItem.addItemListener(e -> {
            if (systemMetricsTable.isShowing()) {
                getContentPane().remove(systemMetricsTable);
            } else {
                getContentPane().add(systemMetricsTable, BorderLayout.EAST);
            }
            getContentPane().revalidate();
        });
        viewMenu.add(systemMetricsMenuItem);
        menuBar.add(viewMenu);

        Menu debugMenu = new Menu("Debug");
        addDebugMenuItem(debugMenu, "Collision", KeyEvent.VK_C);
        addDebugMenuItem(debugMenu, "Path", KeyEvent.VK_P);
        addDebugMenuItem(debugMenu, "Name", KeyEvent.VK_N);
        addDebugMenuItem(debugMenu, "Quad", KeyEvent.VK_U);
        menuBar.add(debugMenu);

        setMenuBar(menuBar);
    }

    private void addDebugMenuItem(Menu debugMenu, String name, int shortcut) {
        CheckboxMenuItem debugMenuItem = new CheckboxMenuItem(name);
        debugMenuItem.setShortcut(new MenuShortcut(shortcut));
        debugMenuItem.addItemListener(e -> debugSettings.put(name, debugMenuItem.getState()));
        debugMenu.add(debugMenuItem);
        debugSettings.put(name, false);
    }

    private void startTimer() {
        timer = new Timer(1000 / 30, this);
        timer.start();
    }

    public void sync() {
        synchronized (monitorWorld) {
            monitorWorld.sync(clickPoint);
            clickPoint = null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (systemMetricsTable.isShowing()) {
            systemMetricsTableModel.refresh();
        }

        repaint();
    }
}
