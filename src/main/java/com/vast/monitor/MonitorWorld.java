package com.vast.monitor;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.components.SerializationTag;
import com.artemis.utils.Bag;
import com.vast.VastWorld;
import com.vast.component.*;
import net.mostlyoriginal.api.utils.QuadTree;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MonitorWorld {
    private final int SCALE = 5;

    private Map<String, Boolean> debugSettings;

    private Point2i size = new Point2i();
    private Map<Integer, MonitorEntity> monitorEntities;
    private int numberOfStaticEntities;
    private int numberOfMovingEntities;
    private Point2i worldSize;
    private MonitorEntity selectedMonitorEntity;
    private long selectedTime;
    private MonitorEntity hoveredMonitorEntity;
    private List<Rectangle> quadRects;

    public MonitorWorld(Map<String, Boolean> debugSettings) {
        this.debugSettings = debugSettings;

        monitorEntities = new HashMap<>();
    }

    public MonitorEntity getSelectedMonitorEntity() {
        return selectedMonitorEntity;
    }

    public int getNumberOfEntities() {
        return monitorEntities != null ? monitorEntities.size() : 0;
    }

    public int getNumberOfStaticEntities() {
        return numberOfStaticEntities;
    }

    public int getNumberOfMovingEntities() {
        return numberOfMovingEntities;
    }

    public Point2i getWorldSize() {
        return worldSize;
    }

    public void sync(VastWorld vastWorld, Point2D clickPoint, Point2D movePoint) {
        size.set(vastWorld.getWorldConfiguration().width * SCALE, vastWorld.getWorldConfiguration().height * SCALE);

        worldSize = new Point2i(vastWorld.getWorldConfiguration().width, vastWorld.getWorldConfiguration().height);

        Set<Integer> entities = Arrays.stream(vastWorld.getEntities(Aspect.all(Transform.class))).boxed().collect(Collectors.toSet());
        monitorEntities.entrySet().removeIf(entry -> !entities.contains(entry.getValue().entity));
        numberOfStaticEntities = 0;
        numberOfMovingEntities = 0;
        for (int entity : entities) {
            MonitorEntity monitorEntity;
            if (monitorEntities.containsKey(entity)) {
                monitorEntity = monitorEntities.get(entity);
            } else {
                monitorEntity = new MonitorEntity(entity);
                monitorEntities.put(entity, monitorEntity);
            }
            monitorEntity.type = vastWorld.getComponentMapper(com.vast.component.Type.class).get(entity).type;
            Point2f position = vastWorld.getComponentMapper(Transform.class).get(entity).position;
            monitorEntity.position = new Point2i(size.x / 2 + (int) (position.x * SCALE),
                    size.y / 2 - (int) (position.y * SCALE));

            if (vastWorld.getComponentMapper(Collision.class).has(entity)) {
                monitorEntity.collisionRadius = (int) (vastWorld.getComponentMapper(Collision.class).get(entity).radius * SCALE);
            } else {
                monitorEntity.collisionRadius = 0;
            }

            if (vastWorld.getComponentMapper(Player.class).has(entity) && vastWorld.getComponentMapper(Scan.class).has(entity)) {
                monitorEntity.scanDistance = (int) (vastWorld.getComponentMapper(Scan.class).get(entity).distance * SCALE);
            } else {
                monitorEntity.scanDistance = 0;
            }

            if (vastWorld.getComponentMapper(Path.class).has(entity)) {
                Point2f targetPosition = vastWorld.getComponentMapper(Path.class).get(entity).targetPosition;
                monitorEntity.pathPosition = new Point2i(size.x / 2 + (int) (targetPosition.x * SCALE),
                        size.y / 2 - (int) (targetPosition.y * SCALE));
            } else {
                monitorEntity.pathPosition = null;
            }

            if (vastWorld.getComponentMapper(Interact.class).has(entity)) {
                int interactEntity = vastWorld.getComponentMapper(Interact.class).get(entity).entity;
                if (interactEntity >= 0) {
                    Point2f interactPosition = vastWorld.getComponentMapper(Transform.class).get(interactEntity).position;
                    monitorEntity.interactPosition = new Point2i(size.x / 2 + (int) (interactPosition.x * SCALE),
                            size.y / 2 - (int) (interactPosition.y * SCALE));
                }
                else {
                    monitorEntity.interactPosition = null;
                }
            } else {
                monitorEntity.interactPosition = null;
            }

            if (vastWorld.getComponentMapper(Player.class).has(entity)) {
                monitorEntity.name = vastWorld.getComponentMapper(Player.class).get(entity).name;
            } else {
                monitorEntity.name = null;
            }

            if (selectedMonitorEntity != null && vastWorld.getComponentMapper(Player.class).has(selectedMonitorEntity.entity)
                && vastWorld.getComponentMapper(Scan.class).has(selectedMonitorEntity.entity)) {
                Scan scan = vastWorld.getComponentMapper(Scan.class).get(selectedMonitorEntity.entity);
                monitorEntity.colored = scan.nearbyEntities.contains(entity);
            } else {
                monitorEntity.colored = true;
            }

            if (vastWorld.getComponentMapper(Static.class).has(entity)) {
                numberOfStaticEntities++;
            } else {
                numberOfMovingEntities++;
            }
        }

        if (clickPoint != null) {
            MonitorEntity closestMonitorEntity = getMonitorEntityClosestTo(clickPoint);
            if (selectedMonitorEntity != null) {
                if (selectedMonitorEntity.entity == closestMonitorEntity.entity) {
                    // Clicked selected
                    selectedMonitorEntity.components = null;
                    selectedMonitorEntity = null;
                } else {
                    // Clicked other
                    selectedMonitorEntity.components = null;
                    selectedMonitorEntity = closestMonitorEntity;
                    selectedTime = System.currentTimeMillis();
                }
            } else {
                // Clicked first
                selectedMonitorEntity = closestMonitorEntity;
                selectedTime = System.currentTimeMillis();
            }
        } else if (selectedMonitorEntity != null) {
            // Selected entity was removed
            if (!entities.contains(selectedMonitorEntity.entity)) {
                selectedMonitorEntity = null;
            }
        }

        if (movePoint != null) {
            hoveredMonitorEntity = getMonitorEntityClosestTo(movePoint);
        }

        if (selectedMonitorEntity != null) {
            Bag<Component> components = new Bag<>();
            vastWorld.getWorld().getEntity(selectedMonitorEntity.entity).getComponents(components);
            selectedMonitorEntity.components = new ArrayList<>();
            for (int i = 0; i < components.size(); i++) {
                Component component = components.get(i);
                String componentName = component.getClass().getSimpleName();
                String detail = null;
                if (component instanceof Type) {
                    detail = ((com.vast.component.Type) component).type;
                } else if (component instanceof SubType) {
                    detail = "" + ((SubType) component).subType;
                } else if (component instanceof Interact) {
                    detail = "" + ((Interact) component).phase;
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
                    int progress = (int) Math.floor(100.0f * constructable.buildTime / constructable.buildDuration);
                    detail = "" + progress;
                } else if (component instanceof Collision) {
                    detail = "" + (Math.round(((Collision) component).radius * 100.0f) / 100.0f);
                } else if (component instanceof Owner) {
                    detail = ((Owner) component).name;
                } else if (component instanceof Player) {
                    detail = ((Player) component).name;
                } else if (component instanceof  Active) {
                    detail = Integer.toString(((Active) component).knowEntities.size());
                } else if (component instanceof Follow) {
                    Follow follow = (Follow) component;
                    detail = follow.entity + ", " + (Math.round((follow.distance * 100.0f) / 100.0f));
                } else if (component instanceof Group) {
                    detail = "" + ((Group) component).id;
                } else if (component instanceof Order) {
                    Order order = (Order) component;
                    if (order.handler != null) {
                        detail = "" + order.handler.getClass().getSimpleName();
                    }
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
                } else if (component instanceof Lifetime) {
                    detail = "" + (Math.round(((Lifetime) component).timeLeft * 100.0f) / 100.0f);
                } else if (component instanceof Skill) {
                    Skill skill = (Skill) component;
                    StringBuilder wordsString = new StringBuilder();
                    for (int j = 0; j < skill.words.length; j++) {
                        if (skill.wordLevels[j] >= 100) {
                            wordsString.append(skill.words[j].toUpperCase());
                        } else {
                            wordsString.append(skill.words[j]);
                            wordsString.append(": ");
                            wordsString.append(skill.wordLevels[j]);
                        }
                        if (j < skill.words.length - 1) {
                            wordsString.append(", ");
                        }
                    }
                    detail = wordsString.toString();
                } else if (component instanceof SerializationTag) {
                    SerializationTag serializationTag = (SerializationTag) component;
                    detail = serializationTag.tag;
                } else if (component instanceof Configuration) {
                    Configuration configuration = (Configuration) component;
                    detail = "" + configuration.version;
                } else if (component instanceof Teach) {
                    Teach teach = (Teach) component;
                    StringBuilder wordsString = new StringBuilder();
                    for (int j = 0; j < teach.words.length; j++) {
                        wordsString.append(teach.words[j]);
                        if (j < teach.words.length - 1) {
                            wordsString.append(", ");
                        }
                    }
                    detail = wordsString.toString();
                } else if (component instanceof Producer) {
                    Producer producer = (Producer) component;
                    detail = producer.recipeId + " " + (Math.round(producer.time * 10f) / 10f);
                } else if (component instanceof SyncHistory) {
                    SyncHistory syncHistory = (SyncHistory) component;
                    detail = "" + syncHistory.syncedValues.size();
                } else if (component instanceof SyncPropagation) {
                    SyncPropagation syncPropagation = (SyncPropagation) component;
                    detail = syncPropagation.unreliableProperties + ", " + syncPropagation.ownerPropagationProperties;
                }
                MonitorComponent monitorComponent = new MonitorComponent();
                monitorComponent.name = componentName;
                if (detail != null) {
                    monitorComponent.details = detail;
                } else {
                    monitorComponent.details = "";
                }
                selectedMonitorEntity.components.add(monitorComponent);
            }
        }

        List<Rectangle> newQuadRects = new ArrayList<>();
        addQuadContainers(newQuadRects, vastWorld.getQuadTree());
        quadRects = newQuadRects;
    }

    private void addQuadContainers(List<Rectangle> quadRectsToAddTo, QuadTree quadTree) {
        int x = (int) (quadTree.getBounds().getX() * SCALE);
        int y = (int) -(quadTree.getBounds().getY() * SCALE);
        y -= quadTree.getBounds().getHeight() * SCALE;
        y += size.y;
        quadRectsToAddTo.add(new Rectangle(x, y,
                (int) (quadTree.getBounds().getWidth() * SCALE), (int) (quadTree.getBounds().getHeight() * SCALE)));
        for (QuadTree child : quadTree.getNodes()) {
            if (child != null) {
                addQuadContainers(quadRectsToAddTo, child);
            }
        }
    }

    private MonitorEntity getMonitorEntityClosestTo(Point2D point) {
        MonitorEntity monitorEntityClosestToClick = null;
        float closestDistance = Float.MAX_VALUE;
        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            float dx = Math.abs((float) (monitorEntity.position.x - point.getX()));
            float dy = Math.abs((float) (monitorEntity.position.y - point.getY()));
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (monitorEntityClosestToClick == null || distance < closestDistance) {
                monitorEntityClosestToClick = monitorEntity;
                closestDistance = distance;
            }
        }

        return monitorEntityClosestToClick;
    }

    public Point2i getSize() {
        return size;
    }

    public void paint(Graphics g) {
        if (debugSettings.get("Quad")) {
            g.setColor(new Color(0x3f, 0x3f, 0x3f));
            for (Rectangle rect : quadRects) {
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            if (monitorEntity == hoveredMonitorEntity && monitorEntity != selectedMonitorEntity) {
                g.setColor(Color.DARK_GRAY);
                g.drawArc(monitorEntity.position.x - 15, monitorEntity.position.y - 15, 30, 30, 0, 360);
            }
        }

        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            monitorEntity.paint(g);
        }

        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            if (monitorEntity == selectedMonitorEntity) {
                int size;
                float secondsSinceSelected = (System.currentTimeMillis() - selectedTime) / 1000f;
                if (secondsSinceSelected < 0.2f) {
                    size = 30 + (int) ((0.2f - secondsSinceSelected) * 40);
                } else {
                    size = 30;
                }

                g.setColor(Color.WHITE);
                g.drawArc(monitorEntity.position.x - size / 2, monitorEntity.position.y - size / 2, size, size, 0, 360);
            }
        }

        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            monitorEntity.paintDebug(g, debugSettings);
        }
    }
}
