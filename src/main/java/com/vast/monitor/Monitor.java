package com.vast.monitor;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.Component;
import com.artemis.components.SerializationTag;
import com.artemis.utils.Bag;
import com.nhnent.haste.transport.QoS;
import com.vast.VastWorld;
import com.vast.component.*;
import com.vast.component.Observer;
import com.vast.data.SystemMetrics;
import com.vast.monitor.model.EntityModel;
import com.vast.monitor.model.ModelData;
import com.vast.monitor.model.SystemMetricsModel;
import com.vast.monitor.model.InfoModel;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Monitor extends JFrame implements ActionListener {
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;

    private VastWorld vastWorld;

    private final String[] PROPERTY_NAMES = new String[]{
        "Position",
        "Rotation",
        "Active",
        "Progress",
        "State",
        "Inventory",
        "Fueled",
        "Home",
        "Configuration",
        "Skill",
        "Valid"
    };

    private final String[] MESSAGE_NAMES = new String[]{
        "EntityCreated",
        "EntityDestroyed",
        "UpdateProperties",
        "Event"
    };

    private MonitorCanvas monitorCanvas;
    private JSlider zoomSlider;

    private final ModelData modelData;
    private InfoModel<String> worldInfoModel;
    private EntityModel entityModel;
    private SystemMetricsModel systemMetricsModel;
    private InfoModel<String> messageInfoModel;
    private InfoModel<Integer> propertyInfoModel;
    private InfoModel<Integer> eventInfoModel;

    private Timer timer;

    private final Map<String, Boolean> debugSettings;
    private final MonitorWorld monitorWorld;
    private Point2D clickPoint;
    private Point2D movePoint;
    private int entityToSelect;

    public Monitor(VastWorld vastWorld) {
        super("Vast Monitor");
        this.vastWorld = vastWorld;

        debugSettings = new HashMap<>();
        monitorWorld = new MonitorWorld(debugSettings);
        modelData = new ModelData();
        entityToSelect = -1;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        createCanvas();
        setupUI();
        setVisible(true);
        startTimer();
    }

    private void createCanvas() {
        monitorCanvas = new MonitorCanvas(monitorWorld);
        monitorCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                synchronized (monitorWorld) {
                    clickPoint = monitorCanvas.convertPointToWorld(mouseEvent.getPoint());
                }
            }
        });
        monitorCanvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                synchronized (monitorWorld) {
                    movePoint = monitorCanvas.convertPointToWorld(mouseEvent.getPoint());
                }
            }
        });
        monitorCanvas.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                int wheelRotation = mouseWheelEvent.getWheelRotation();
                if (wheelRotation < 0) {
                    zoomSlider.setValue(zoomSlider.getValue() + 5);
                } else if (wheelRotation > 0) {
                    zoomSlider.setValue(zoomSlider.getValue() - 5);
                }
            }
        });
    }

    private void setupUI() {
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
            monitorCanvas.setScale(Math.max(0.00001, zoomPercent / 100.0));
            monitorCanvas.repaint();
        });
        topPanel.add(zoomSlider, BorderLayout.EAST);

        worldInfoModel = new InfoModel<>(new String[]{"Name", "Value"}, String.class);
        JTable worldInfoTable = new JTable(worldInfoModel);
        worldInfoTable.setFocusable(false);
        worldInfoTable.getColumn("Name").setPreferredWidth(130);
        worldInfoTable.getColumn("Value").setPreferredWidth(120);
        JScrollPane worldInfoScrollPanel = new JScrollPane(worldInfoTable);
        worldInfoScrollPanel.setPreferredSize(new Dimension(250, HEIGHT));
        worldInfoScrollPanel.setMinimumSize(new Dimension(250, 220));
        worldInfoTable.setPreferredScrollableViewportSize(new Dimension(250, HEIGHT));

        entityModel = new EntityModel();
        JTable entityTable = new JTable(entityModel);
        entityTable.setFocusable(false);
        sortTable(entityTable);
        entityTable.getColumn("Component").setPreferredWidth(110);
        entityTable.getColumn("Detail").setPreferredWidth(140);
        JScrollPane entityScrollPanel = new JScrollPane(entityTable);
        entityScrollPanel.setPreferredSize(new Dimension(250, HEIGHT));
        entityScrollPanel.setMinimumSize(new Dimension(250, 300));
        entityTable.setPreferredScrollableViewportSize(new Dimension(250, HEIGHT));

        systemMetricsModel = new SystemMetricsModel();
        JTable systemMetricsTable = new JTable(systemMetricsModel);
        systemMetricsTable.setFocusable(false);
        sortTable(systemMetricsTable);
        systemMetricsTable.getColumn("System").setPreferredWidth(120);
        systemMetricsTable.getColumn("Time").setPreferredWidth(40);
        systemMetricsTable.getColumn("Entities").setPreferredWidth(60);
        JScrollPane systemMetricsScrollPanel = new JScrollPane(systemMetricsTable);
        systemMetricsScrollPanel.setPreferredSize(new Dimension(220, HEIGHT));
        systemMetricsScrollPanel.setMinimumSize(new Dimension(220, HEIGHT));
        systemMetricsTable.setPreferredScrollableViewportSize(new Dimension(220, HEIGHT));

        messageInfoModel = new InfoModel<>(new String[]{"Message", "Detail"}, String.class);
        JTable messageInfoTable = new JTable(messageInfoModel);
        messageInfoTable.setFocusable(false);
        sortTable(messageInfoTable);
        messageInfoTable.getColumn("Message").setPreferredWidth(130);
        messageInfoTable.getColumn("Detail").setPreferredWidth(90);
        JScrollPane messageInfoScrollPanel = new JScrollPane(messageInfoTable);
        messageInfoScrollPanel.setPreferredSize(new Dimension(220, 200));
        messageInfoScrollPanel.setMinimumSize(new Dimension(220, 200));
        messageInfoTable.setPreferredScrollableViewportSize(new Dimension(220, 200));

        propertyInfoModel = new InfoModel<>(new String[]{"Property", "Count"}, Integer.class);
        JTable propertyInfoTable = new JTable(propertyInfoModel);
        propertyInfoTable.setFocusable(false);
        sortTable(propertyInfoTable);
        propertyInfoTable.getColumn("Property").setPreferredWidth(130);
        propertyInfoTable.getColumn("Count").setPreferredWidth(90);
        JScrollPane propertyInfoScrollPanel = new JScrollPane(propertyInfoTable);
        propertyInfoScrollPanel.setPreferredSize(new Dimension(220, 200));
        propertyInfoScrollPanel.setMinimumSize(new Dimension(220, 200));
        propertyInfoTable.setPreferredScrollableViewportSize(new Dimension(220, 200));

        eventInfoModel = new InfoModel<>(new String[]{"Event", "Count"}, Integer.class);
        JTable eventInfoTable = new JTable(eventInfoModel);
        eventInfoTable.setFocusable(false);
        sortTable(eventInfoTable);
        eventInfoTable.getColumn("Event").setPreferredWidth(130);
        eventInfoTable.getColumn("Count").setPreferredWidth(90);
        JScrollPane eventInfoScrollPanel = new JScrollPane(eventInfoTable);
        eventInfoScrollPanel.setPreferredSize(new Dimension(220, 200));
        eventInfoScrollPanel.setMinimumSize(new Dimension(220, 200));
        eventInfoTable.setPreferredScrollableViewportSize(new Dimension(220, 200));

        JSplitPane networkSplitPanel1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane networkSplitPanel2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        networkSplitPanel1.setTopComponent(messageInfoScrollPanel);
        networkSplitPanel1.setBottomComponent(networkSplitPanel2);
        networkSplitPanel2.setTopComponent(propertyInfoScrollPanel);
        networkSplitPanel2.setBottomComponent(eventInfoScrollPanel);

        JTabbedPane tabbedPanel = new JTabbedPane();
        tabbedPanel.setFocusable(false);
        tabbedPanel.setPreferredSize(new Dimension(220, HEIGHT));
        tabbedPanel.setMinimumSize(new Dimension(220, HEIGHT));
        tabbedPanel.add("Systems", systemMetricsScrollPanel);
        tabbedPanel.add("Network", networkSplitPanel1);

        JSplitPane splitPanel1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JSplitPane splitPanel2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane splitPanel3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane splitPanel4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        splitPanel3.setLeftComponent(monitorCanvas);
        splitPanel3.setRightComponent(tabbedPanel);

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

    private void sortTable(JTable table) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    private void startTimer() {
        timer = new Timer(1000 / 30, this);
        timer.start();
    }

    public void sync() {
        int selectedEntity;
        // vastWorld -> monitorWorld
        synchronized (monitorWorld) {
            monitorWorld.sync(vastWorld, monitorCanvas, clickPoint, movePoint, entityToSelect);
            selectedEntity = monitorWorld.getSelectedMonitorEntity();
            clickPoint = null;
            entityToSelect = -1;
        }

        // vastWorld -> modelData
        synchronized (modelData) {
            Map<String, String> worldInfo = new HashMap<>();
            worldInfo.put("World size", "" + vastWorld.getWorldConfiguration().width + " x " + vastWorld.getWorldConfiguration().height);
            worldInfo.put("Total entities", "" + vastWorld.getWorld().getAspectSubscriptionManager().get(Aspect.all()).getEntities().size());
            worldInfo.put("Static entities", "" + vastWorld.getWorld().getAspectSubscriptionManager().get(Aspect.all(Static.class)).getEntities().size());
            worldInfo.put("Moving entities", "" + vastWorld.getWorld().getAspectSubscriptionManager().get(Aspect.all(Transform.class).exclude(Static.class)).getEntities().size());
            worldInfo.put("Scanning entities", "" + vastWorld.getWorld().getAspectSubscriptionManager().get(Aspect.all(Scan.class)).getEntities().size());
            if (vastWorld.getMetrics() != null) {
                worldInfo.put("Fps", vastWorld.getMetrics().getFps() + " / " + vastWorld.getMetrics().getTimePerFrameMs() + " ms");
                worldInfo.put("Total processing time", "" + vastWorld.getMetrics().getSystemMetrics().values().stream().mapToInt(SystemMetrics::getProcessingTime).sum() + " ms");
                worldInfo.put("Collisions", vastWorld.getMetrics().getNumberOfCollisions() + " / " + vastWorld.getMetrics().getNumberOfCollisionChecks());
                worldInfo.put("Synced properties", "" + vastWorld.getMetrics().getSyncedProperties().values().stream().reduce(0, Integer::sum));
            }

            modelData.worldInfo = worldInfo;

            if (vastWorld.getMetrics() != null) {
                if (modelData.messagesInfo != null) {
                    modelData.messagesInfo.clear();
                } else {
                    modelData.messagesInfo = new HashMap<>();
                }
                for (short messageCode : vastWorld.getMetrics().getSentMessages().keySet()) {
                    Map<QoS, int[]> sentMessages = vastWorld.getMetrics().getSentMessages().get(messageCode);
                    for (QoS qos : sentMessages.keySet()) {
                        String qosString = qos == QoS.RELIABLE_SEQUENCED ? "reliable" : "unreliable";
                        int count = sentMessages.get(qos)[0];
                        int bytes = sentMessages.get(qos)[1];
                        String sizeString;
                        if (bytes < 1000) {
                            sizeString = bytes + " B";
                        } else if (bytes < 1000000) {
                            sizeString = (bytes / 1000) + " KB";
                        } else {
                            sizeString = (bytes / 1000000) + " MB";
                        }
                        modelData.messagesInfo.put(MESSAGE_NAMES[messageCode] + " (" + qosString + ")", count + " (" + sizeString + ")");
                    }
                }

                if (modelData.propertyInfo != null) {
                    modelData.propertyInfo.clear();
                } else {
                    modelData.propertyInfo = new HashMap<>();
                }
                for (byte property : vastWorld.getMetrics().getSyncedProperties().keySet()) {
                    modelData.propertyInfo.put(PROPERTY_NAMES[property], vastWorld.getMetrics().getSyncedProperties().get(property));
                }

                if (modelData.eventsInfo != null) {
                    modelData.eventsInfo.clear();
                } else {
                    modelData.eventsInfo = new HashMap<>();
                }
                for (String event : vastWorld.getMetrics().getSentEvents().keySet()) {
                    modelData.eventsInfo.put(event, vastWorld.getMetrics().getSentEvents().get(event));
                }
            }

            if (vastWorld.getMetrics() != null) {
                modelData.systemMetrics = vastWorld.getMetrics().getSystemMetrics().entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.comparing((BaseSystem system) -> system.getClass().getSimpleName())))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));
            }

            if (selectedEntity >= 0) {
                Map<String, String> entityData = new HashMap<>();
                Bag<com.artemis.Component> components = new Bag<>();
                vastWorld.getWorld().getEntity(selectedEntity).getComponents(components);
                for (int i = 0; i < components.size(); i++) {
                    Component component = components.get(i);
                    String componentName = component.getClass().getSimpleName();
                    String detail = null;
                    if (component instanceof com.vast.component.Type) {
                        detail = ((com.vast.component.Type) component).type;
                    } else if (component instanceof SubType) {
                        detail = "" + ((SubType) component).subType;
                    } else if (component instanceof Interact) {
                        Interact interact = (Interact) component;
                        detail = interact.entity + ", " + interact.phase;
                    } else if (component instanceof Scan) {
                        detail = "" + ((Scan) component).nearbyEntities.size();
                    } else if (component instanceof Known) {
                        detail = "" + ((Known) component).knownByEntities.size();
                    } else if (component instanceof AI) {
                        detail = ((AI) component).behaviourName;
                    } else if (component instanceof State) {
                        String stateName = ((State) component).name;
                        detail = stateName != null ? stateName : "";
                    } else if (component instanceof Harvestable) {
                        detail = "" + (Math.round(((Harvestable) component).durability * 100.0f) / 100.0f);
                    } else if (component instanceof Growing) {
                        detail = "" + (Math.round(((Growing) component).timeLeft * 100.0f) / 100.0f);
                    } else if (component instanceof Constructable) {
                        Constructable constructable = (Constructable) component;
                        detail = Math.round(constructable.buildTime * 10f) / 10f + " / " + Math.round(constructable.buildDuration * 10f) / 10f;
                    } else if (component instanceof Collision) {
                        detail = "" + (Math.round(((Collision) component).radius * 100.0f) / 100.0f);
                    } else if (component instanceof Owner) {
                        detail = ((Owner) component).name;
                    } else if (component instanceof Avatar) {
                        detail = ((Avatar) component).name;
                    } else if (component instanceof Observer) {
                        Observer observer = (Observer) component;
                        detail = observer.observedEntity + ", " + observer.peer.getName() + ", " + observer.knowEntities.size();
                    } else if (component instanceof Observed) {
                        detail = "" + ((Observed) component).observerEntity;
                    } else if (component instanceof Follow) {
                        Follow follow = (Follow) component;
                        detail = follow.entity + ", " + (Math.round((follow.distance * 100.0f) / 100.0f));
                    } else if (component instanceof Group) {
                        detail = "" + ((Group) component).id;
                    } else if (component instanceof Parent) {
                        detail = "" + ((Parent) component).parentEntity;
                    } else if (component instanceof Order) {
                        Order order = (Order) component;
                        if (order.handler != null) {
                            detail = "" + order.handler.getClass().getSimpleName();
                        }
                    } else if (component instanceof OrderQueue) {
                        detail = "" + ((OrderQueue) component).requests.size();
                    } else if (component instanceof Speed) {
                        detail = "" + (Math.round(((Speed) component).getModifiedSpeed() * 10f) / 10f);
                    } else if (component instanceof Transform) {
                        detail = "" + (Math.round(((Transform) component).position.x * 100f) / 100f) + ", " + (Math.round(((Transform) component).position.y * 100.0f) / 100.0f);
                    } else if (component instanceof Path) {
                        Path path = (Path) component;
                        detail = "" + (Math.round(path.targetPosition.x * 100f) / 100f) + ", " + (Math.round(path.targetPosition.y * 100f) / 100f) + " " + (Math.round(path.timeInSamePosition * 10f) / 10f);
                    } else if (component instanceof Inventory) {
                        Inventory inventory = (Inventory) component;
                        StringBuilder s = new StringBuilder();
                        for (int j = 0; j < inventory.items.length; j++) {
                            s.append(inventory.items[j]);
                            if (j < inventory.items.length - 1) {
                                s.append(", ");
                            }
                        }
                        detail = s.toString();
                    } else if (component instanceof Skill) {
                        Skill skill = (Skill) component;
                        StringBuilder skillString = new StringBuilder();
                        for (int j = 0; j < skill.names.length; j++) {
                            skillString.append(skill.names[j]);
                            skillString.append(": ");
                            skillString.append(skill.xp[j]);
                            if (j < skill.names.length - 1) {
                                skillString.append(", ");
                            }
                        }
                        detail = skillString.toString();
                    } else if (component instanceof Lifetime) {
                        detail = "" + (Math.round(((Lifetime) component).timeLeft * 100.0f) / 100.0f);
                    } else if (component instanceof SerializationTag) {
                        SerializationTag serializationTag = (SerializationTag) component;
                        detail = serializationTag.tag;
                    } else if (component instanceof Configuration) {
                        Configuration configuration = (Configuration) component;
                        detail = "" + configuration.version;
                    } else if (component instanceof Producer) {
                        Producer producer = (Producer) component;
                        detail = producer.recipeId + " " + (Math.round(producer.time * 10f) / 10f);
                    } else if (component instanceof SyncHistory) {
                        SyncHistory syncHistory = (SyncHistory) component;
                        detail = "" + syncHistory.syncedValues.size();
                    } else if (component instanceof SyncPropagation) {
                        SyncPropagation syncPropagation = (SyncPropagation) component;
                        detail = syncPropagation.unreliableProperties + ", " + syncPropagation.ownerPropagationProperties +
                                ", " + syncPropagation.blockedProperties;
                    } else if (component instanceof Used) {
                        detail = "" + ((Used) component).usedByEntity;
                    } else if (component instanceof Fueled) {
                        detail = "" + (Math.round(((Fueled) component).timeLeft * 10f) / 10f);
                    } else if (component instanceof Layer) {
                        detail = ((Layer) component).name;
                    }

                    entityData.put(componentName, detail != null ? detail : "");
                }

                modelData.selectedEntity = entityData;
            } else {
                modelData.selectedEntity = null;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // table models -> Monitor
        synchronized (this) {
            if (entityModel.clickedEntity >= 0) {
                entityToSelect = entityModel.clickedEntity;
                entityModel.clickedEntity = -1;
            }
        }

        // modelData -> table models
        synchronized (modelData) {
            worldInfoModel.refresh(modelData.worldInfo);
            entityModel.refresh(modelData.selectedEntity);
            systemMetricsModel.refresh(modelData.systemMetrics);
            messageInfoModel.refresh(modelData.messagesInfo);
            propertyInfoModel.refresh(modelData.propertyInfo);
            eventInfoModel.refresh(modelData.eventsInfo);
        }

        monitorCanvas.repaint();
    }
}
