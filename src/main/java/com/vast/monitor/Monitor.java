package com.vast.monitor;

import com.artemis.BaseSystem;
import com.vast.VastWorld;
import com.vast.monitor.model.EntityModel;
import com.vast.monitor.model.ModelData;
import com.vast.monitor.model.SystemMetricsModel;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

public class Monitor extends JFrame implements ActionListener {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;

    private VastWorld vastWorld;

    private MonitorCanvas canvas;
    private JSlider zoomSlider;

    private final ModelData modelData;
    private SystemMetricsModel systemMetricsTableModel;
    private JTable systemMetricsTable;
    private EntityModel entityTableModel;
    private JTable entityTable;

    private Timer timer;

    private final Map<String, Boolean> debugSettings = new HashMap<>();
    private final MonitorWorld monitorWorld;
    private Point2D clickPoint;

    public Monitor(VastWorld vastWorld) {
        super("Vast Monitor");
        this.vastWorld = vastWorld;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);

        monitorWorld = new MonitorWorld(debugSettings);
        modelData = new ModelData();

        setupUI();
        setupMenu();
        setVisible(true);
        startTimer();
    }

    private void setupUI() {
        canvas = new MonitorCanvas(this, monitorWorld);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double x = e.getPoint().x;
                double y = e.getPoint().y;

                x -= (1f - canvas.scale) * getWidth() / 2;
                y -= (1f - canvas.scale) * getHeight() / 2;

                x -= canvas.translateX * canvas.scale;
                y -= canvas.translateY * canvas.scale;

                x /= canvas.scale;
                y /= canvas.scale;

                clickPoint = new Point((int) Math.round(x), (int) Math.round(y));
            }
        });
        getContentPane().add(canvas, BorderLayout.CENTER);

        zoomSlider = new JSlider(JSlider.HORIZONTAL, 50, 500, 100);
        zoomSlider.setMajorTickSpacing(50);
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

        systemMetricsTableModel = new SystemMetricsModel();
        systemMetricsTable = new JTable(systemMetricsTableModel);
        systemMetricsTable.getColumn("System").setPreferredWidth(150);
        systemMetricsTable.getColumn("Time").setPreferredWidth(30);
        systemMetricsTable.getColumn("Entities").setPreferredWidth(50);

        entityTableModel = new EntityModel();
        entityTable = new JTable(entityTableModel);
        entityTable.getColumn("Component").setPreferredWidth(120);
        entityTable.getColumn("Details").setPreferredWidth(100);
    }

    private void setupMenu() {
        MenuBar menuBar = new MenuBar();

        Menu viewMenu = new Menu("View");
        CheckboxMenuItem systemMetricsMenuItem = new CheckboxMenuItem("System Metrics");
        systemMetricsMenuItem.setShortcut(new MenuShortcut(KeyEvent.VK_S));
        systemMetricsMenuItem.addItemListener(e -> {
            if (systemMetricsTable.getParent() != null) {
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
        addDebugMenuItem(debugMenu, "Scan", KeyEvent.VK_A);
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
            monitorWorld.sync(vastWorld, clickPoint);
            clickPoint = null;
        }

        // Seems to work without "synchronized" due to always setting new values?
        synchronized (modelData) {
            modelData.systemMetricsToShow = vastWorld.getMetrics().getSystemMetrics().entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing((BaseSystem system) -> system.getClass().getSimpleName())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            MonitorEntity selected = monitorWorld.getSelectedMonitorEntity();
            if (selected != null) {
                MonitorEntity clone = new MonitorEntity(selected.entity);
                clone.components = new ArrayList<>();
                for (MonitorComponent component : selected.components) {
                    MonitorComponent clonedComponent = new MonitorComponent();
                    clonedComponent.name = component.name;
                    clonedComponent.details = component.details;
                    clone.components.add(clonedComponent);
                }
                modelData.entity = clone;
            } else {
                modelData.entity = null;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        synchronized (modelData) {
            if (systemMetricsTable.isShowing()) {
                systemMetricsTableModel.refresh(modelData.systemMetricsToShow);
            }

            if (modelData.entity != null) {
                entityTableModel.refresh(modelData.entity);
                if (entityTable.getParent() == null) {
                    getContentPane().add(entityTable, BorderLayout.WEST);
                    revalidate();
                }
            } else {
                if (entityTable.getParent() != null) {
                    getContentPane().remove(entityTable);
                    entityTableModel.clear();
                    revalidate();
                }
            }
        }

        repaint();
    }
}
