package com.vast.monitor;

import com.artemis.BaseSystem;
import com.vast.VastWorld;
import com.vast.monitor.model.EntityModel;
import com.vast.monitor.model.ModelData;
import com.vast.monitor.model.SystemMetricsModel;
import com.vast.monitor.model.WorldInfoModel;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

public class Monitor extends JFrame implements ActionListener {
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;

    private VastWorld vastWorld;

    private MonitorCanvas canvas;
    private JSlider zoomSlider;

    private final ModelData modelData;
    private SystemMetricsModel systemMetricsTableModel;
    private JTable systemMetricsTable;
    private EntityModel entityTableModel;
    private JTable entityTable;
    private WorldInfoModel worldInfoModel;
    private JTable worldInfoTable;

    private Timer timer;

    private final Map<String, Boolean> debugSettings = new HashMap<>();
    private final MonitorWorld monitorWorld;
    private Point2D clickPoint;
    private Point2D movePoint;

    public Monitor(VastWorld vastWorld) {
        super("Vast Monitor");
        this.vastWorld = vastWorld;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);

        monitorWorld = new MonitorWorld(debugSettings);
        modelData = new ModelData();

        setupUI();
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
        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                double x = e.getPoint().x;
                double y = e.getPoint().y;

                x -= (1f - canvas.scale) * getWidth() / 2;
                y -= (1f - canvas.scale) * getHeight() / 2;

                x -= canvas.translateX * canvas.scale;
                y -= canvas.translateY * canvas.scale;

                x /= canvas.scale;
                y /= canvas.scale;

                movePoint = new Point((int) Math.round(x), (int) Math.round(y));
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel toggleContainer = new JPanel(new GridBagLayout());
        JPanel togglePanel = new JPanel(new FlowLayout());
        togglePanel.add(createDebugSettingToggleButton("Collision"));
        togglePanel.add(createDebugSettingToggleButton("Scan"));
        togglePanel.add(createDebugSettingToggleButton("Quad"));
        togglePanel.add(createDebugSettingToggleButton("Name"));
        togglePanel.add(createDebugSettingToggleButton("Path"));
        togglePanel.add(createDebugSettingToggleButton("Interact"));
        toggleContainer.add(togglePanel, new GridBagConstraints());
        toggleContainer.setPreferredSize(new Dimension(450, 40));
        topPanel.add(toggleContainer, BorderLayout.WEST);

        zoomSlider = new JSlider(JSlider.HORIZONTAL, 50, 500, 100);
        zoomSlider.setFocusable(false);
        zoomSlider.setPreferredSize(new Dimension(WIDTH - toggleContainer.getPreferredSize().width, 40));
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.setFocusable(false);
        zoomSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            int zoomPercent = slider.getValue();
            canvas.scale = Math.max(0.00001, zoomPercent / 100.0);
            canvas.repaint();
        });
        topPanel.add(zoomSlider, BorderLayout.EAST);

        systemMetricsTableModel = new SystemMetricsModel();
        systemMetricsTable = new JTable(systemMetricsTableModel);
        systemMetricsTable.setFocusable(false);
        systemMetricsTable.getColumn("System").setPreferredWidth(120);
        systemMetricsTable.getColumn("Time").setPreferredWidth(40);
        systemMetricsTable.getColumn("Entities").setPreferredWidth(60);
        JScrollPane systemMetricsScrollPanel = new JScrollPane(systemMetricsTable);
        systemMetricsScrollPanel.setPreferredSize(new Dimension(220, HEIGHT));
        systemMetricsScrollPanel.setMinimumSize(new Dimension(220, HEIGHT));
        systemMetricsTable.setPreferredScrollableViewportSize(new Dimension(220, HEIGHT));

        worldInfoModel = new WorldInfoModel();
        worldInfoTable = new JTable(worldInfoModel);
        worldInfoTable.setFocusable(false);
        worldInfoTable.getColumn("Name").setPreferredWidth(130);
        worldInfoTable.getColumn("Value").setPreferredWidth(120);
        JScrollPane worldInfoScrollPanel = new JScrollPane(worldInfoTable);
        worldInfoScrollPanel.setPreferredSize(new Dimension(250, HEIGHT));
        worldInfoScrollPanel.setMinimumSize(new Dimension(250, 150));
        worldInfoTable.setPreferredScrollableViewportSize(new Dimension(250, HEIGHT));

        entityTableModel = new EntityModel();
        entityTable = new JTable(entityTableModel);
        entityTable.setFocusable(false);
        entityTable.getColumn("Component").setPreferredWidth(110);
        entityTable.getColumn("Details").setPreferredWidth(140);
        JScrollPane entityScrollPanel = new JScrollPane(entityTable);
        entityScrollPanel.setPreferredSize(new Dimension(250, HEIGHT));
        entityScrollPanel.setMinimumSize(new Dimension(250, 300));
        entityTable.setPreferredScrollableViewportSize(new Dimension(250, HEIGHT));

        JSplitPane splitPanel1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JSplitPane splitPanel2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane splitPanel3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane splitPanel4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        splitPanel3.setLeftComponent(canvas);
        splitPanel3.setRightComponent(systemMetricsScrollPanel);

        splitPanel2.setLeftComponent(splitPanel4);
        splitPanel2.setRightComponent(splitPanel3);

        splitPanel1.setTopComponent(topPanel);
        splitPanel1.setBottomComponent(splitPanel2);

        splitPanel4.setTopComponent(worldInfoScrollPanel);
        splitPanel4.setBottomComponent(entityScrollPanel);

        splitPanel3.setResizeWeight(1);

        getContentPane().add(splitPanel1);

        getContentPane().requestFocusInWindow();
    }

    private JToggleButton createDebugSettingToggleButton(String name) {
        JToggleButton toggleButton = new JToggleButton(name);
        toggleButton.setFocusable(false);
        toggleButton.putClientProperty("JButton.buttonType", "roundRect");
        toggleButton.addItemListener(e -> {
            JToggleButton changedToggleButton = (JToggleButton) e.getSource();
            debugSettings.put(name, changedToggleButton.isSelected());
        });
        debugSettings.put(name, false);
        return toggleButton;
    }

    private void startTimer() {
        timer = new Timer(1000 / 30, this);
        timer.start();
    }

    public void sync() {
        monitorWorld.sync(vastWorld, clickPoint, movePoint);
        clickPoint = null;

        Map<String, String> worldInfo = new HashMap<>();
        worldInfo.put("World size", "" + monitorWorld.getWorldSize().x + " x " + monitorWorld.getWorldSize().y);
        worldInfo.put("Total entities", "" + monitorWorld.getNumberOfEntities());
        worldInfo.put("Static entities", "" + monitorWorld.getNumberOfStaticEntities());
        worldInfo.put("Moving entities", "" + monitorWorld.getNumberOfMovingEntities());
        modelData.worldInfo = worldInfo;

        modelData.systemMetrics = vastWorld.getMetrics().getSystemMetrics().entrySet()
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

    @Override
    public void actionPerformed(ActionEvent e) {
        systemMetricsTableModel.refresh(modelData.systemMetrics);

        worldInfoModel.refresh(modelData.worldInfo);

        if (modelData.entity != null) {
            entityTableModel.refresh(modelData.entity);
        } else {
            entityTableModel.clear();
        }

        repaint();
    }
}
