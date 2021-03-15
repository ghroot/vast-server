package com.vast.monitor;

import com.vast.VastWorld;
import com.vast.data.Metrics;
import com.vast.data.WorldConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class Monitor extends JFrame implements ActionListener {
    private WorldConfiguration worldConfiguration;

    private MonitorCanvas canvas;
    private JSlider zoomSlider;
    private SystemMetricsModel systemMetricsTableModel;
    private JTable systemMetricsTable;

    private CheckboxMenuItem showCollisionMenuItem;
    private CheckboxMenuItem showPathMenuItem;
    private CheckboxMenuItem showNameMenuItem;

    private Timer timer;

    private DebugSettings debugSettings;
    private Point2D clickPoint;

    public Monitor(WorldConfiguration worldConfiguration, Metrics metrics) {
        super("Vast Monitor");
        this.worldConfiguration = worldConfiguration;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setupUI(metrics);
        setupMenu();
        setVisible(true);
        startTimer();
    }

    private void setupUI(Metrics metrics) {
        canvas = new MonitorCanvas();
        PanningHandler panner = new PanningHandler(canvas);
        canvas.addMouseListener(panner);
        canvas.addMouseMotionListener(panner);
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
        zoomSlider.addChangeListener(new ScaleHandler(canvas));
        getContentPane().add(zoomSlider, BorderLayout.NORTH);

        systemMetricsTableModel = new SystemMetricsModel(metrics);
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

        debugSettings = new DebugSettings();

        Menu debugMenu = new Menu("Debug");
        showCollisionMenuItem = new CheckboxMenuItem("Collision");
        showCollisionMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_C));
        showCollisionMenuItem.addItemListener(e -> debugSettings.collision = showCollisionMenuItem.getState());
        debugMenu.add(showCollisionMenuItem);
        showPathMenuItem = new CheckboxMenuItem("Path");
        showPathMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_P));
        showPathMenuItem.addItemListener(e -> debugSettings.path = showPathMenuItem.getState());
        debugMenu.add(showPathMenuItem);
        showNameMenuItem = new CheckboxMenuItem("Name");
        showNameMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_N));
        showNameMenuItem.addItemListener(e -> debugSettings.name = showNameMenuItem.getState());
        debugMenu.add(showNameMenuItem);
        menuBar.add(debugMenu);

        setMenuBar(menuBar);
    }

    private void startTimer() {
        timer = new Timer(1000 / 30, this);
        timer.start();
    }

    public void sync(VastWorld vastWorld) {
        canvas.setMonitorWorld(new MonitorWorld(worldConfiguration, vastWorld, debugSettings, clickPoint));
        clickPoint = null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (systemMetricsTable.isShowing()) {
            systemMetricsTableModel.refresh();
        }

        repaint();
    }
}
