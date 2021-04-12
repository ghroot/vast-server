package com.vast.monitor;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.VastWorld;
import com.vast.component.Observer;
import com.vast.component.*;
import net.mostlyoriginal.api.utils.QuadTree;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MonitorWorld {
    private final int SCALE = 5;

    private Map<String, Boolean> debugSettings;

    private Point2i size;
    private Map<Integer, MonitorEntity> monitorEntities;
    private MonitorEntity selectedMonitorEntity;
    private long selectedTime;
    private MonitorEntity hoveredMonitorEntity;
    private List<Rectangle> quadRects;

    public MonitorWorld(Map<String, Boolean> debugSettings) {
        this.debugSettings = debugSettings;

        size = new Point2i();
        monitorEntities = new HashMap<>();
        quadRects = new ArrayList<>();
    }

    public int getSelectedMonitorEntity() {
        return selectedMonitorEntity != null ? selectedMonitorEntity.entity : -1;
    }

    public void sync(VastWorld vastWorld, MonitorCanvas monitorCanvas, Point2D clickPoint, Point2D movePoint, int entityToSelect) {
        size.set(vastWorld.getWorldConfiguration().width * SCALE, vastWorld.getWorldConfiguration().height * SCALE);

        Rectangle lastCanvasClipBounds = monitorCanvas.getLastClipBounds();
        Set<Integer> entities;
        if (lastCanvasClipBounds != null) {
            float distanceX = lastCanvasClipBounds.width / 2f;
            float distanceY = lastCanvasClipBounds.height / 2f;
            distanceX /= SCALE;
            distanceY /= SCALE;

            float x = (lastCanvasClipBounds.width / 2f) / SCALE * (float) monitorCanvas.getScale() - (float) monitorCanvas.getTranslateX() / SCALE;
            float y = (lastCanvasClipBounds.height / 2f) / SCALE * (float) monitorCanvas.getScale() - (float) monitorCanvas.getTranslateY() / SCALE;
            y = -y;
            x -= vastWorld.getWorldConfiguration().width / 2f;
            y += vastWorld.getWorldConfiguration().height / 2f;

            IntBag nearbyEntities = new IntBag();
            vastWorld.getQuadTree().get(nearbyEntities, x + vastWorld.getWorldConfiguration().width / 2f - distanceX,
                    y + vastWorld.getWorldConfiguration().height / 2f - distanceY, 2f * distanceX, 2f * distanceY);

            // Include all observers for now
            IntBag observerEntities = vastWorld.getWorld().getAspectSubscriptionManager().get(Aspect.all(Observer.class)).getEntities();
            nearbyEntities.addAll(observerEntities);

            int[] nearbyEntitiesData = nearbyEntities.getData();
            int[] nearbyEntitiesArray = new int[nearbyEntities.size()];
            System.arraycopy(nearbyEntitiesData, 0, nearbyEntitiesArray, 0, nearbyEntitiesArray.length);
            entities = Arrays.stream(nearbyEntitiesArray).boxed().collect(Collectors.toSet());
        } else {
            entities = new HashSet<>();
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

        if (entityToSelect >= 0) {
            selectedMonitorEntity = monitorEntities.get(entityToSelect);
            selectedTime = System.currentTimeMillis();
        }

        if (movePoint != null) {
            hoveredMonitorEntity = getMonitorEntityClosestTo(movePoint);
        }

        ComponentMapper<Type> typeMapper = vastWorld.getWorld().getMapper(Type.class);
        ComponentMapper<Transform> transformMapper = vastWorld.getWorld().getMapper(Transform.class);
        ComponentMapper<Collision> collisionMapper = vastWorld.getWorld().getMapper(Collision.class);
        ComponentMapper<Scan> scanMapper = vastWorld.getWorld().getMapper(Scan.class);
        ComponentMapper<Path> pathMapper = vastWorld.getWorld().getMapper(Path.class);
        ComponentMapper<Interact> interactMapper = vastWorld.getWorld().getMapper(Interact.class);
        ComponentMapper<Observed> observedMapper = vastWorld.getWorld().getMapper(Observed.class);
        ComponentMapper<Avatar> avatarMapper = vastWorld.getWorld().getMapper(Avatar.class);
        ComponentMapper<Observer> observerMapper = vastWorld.getWorld().getMapper(Observer.class);
        ComponentMapper<Static> staticMapper = vastWorld.getWorld().getMapper(Static.class);

        monitorEntities.entrySet().removeIf(entry -> !entities.contains(entry.getValue().entity));
        for (int entity : entities) {
            MonitorEntity monitorEntity;
            if (monitorEntities.containsKey(entity)) {
                monitorEntity = monitorEntities.get(entity);
            } else {
                monitorEntity = new MonitorEntity(entity);

                monitorEntity.type = typeMapper.get(entity).type;

                Collision collision = collisionMapper.get(entity);
                if (collision != null) {
                    monitorEntity.collisionRadius = (int) (collision.radius * SCALE);
                } else {
                    monitorEntity.collisionRadius = 0;
                }

                monitorEntity.isStatic = staticMapper.has(entity);

                monitorEntities.put(entity, monitorEntity);
            }

            if (monitorEntity.position == null || !monitorEntity.isStatic) {
                Transform transform = transformMapper.get(entity);
                Point2f position = transform.position;
                monitorEntity.position = new Point2i(size.x / 2 + (int) (position.x * SCALE),
                        size.y / 2 - (int) (position.y * SCALE));
            }

            if (!monitorEntity.isStatic) {
                Scan scan = scanMapper.get(entity);
                if (scan != null && (selectedMonitorEntity == null || monitorEntity == selectedMonitorEntity ||
                        !scanMapper.has(selectedMonitorEntity.entity))) {
                    monitorEntity.scanDistance = (int) (scan.distance * SCALE);
                } else {
                    monitorEntity.scanDistance = 0;
                }

                Path path = pathMapper.get(entity);
                if (path != null) {
                    Point2f targetPosition = path.targetPosition;
                    monitorEntity.pathPosition = new Point2i(size.x / 2 + (int) (targetPosition.x * SCALE),
                            size.y / 2 - (int) (targetPosition.y * SCALE));
                } else {
                    monitorEntity.pathPosition = null;
                }

                Interact interact = interactMapper.get(entity);
                if (interact != null) {
                    if (interact.entity >= 0) {
                        Point2f interactPosition = transformMapper.get(interact.entity).position;
                        monitorEntity.interactPosition = new Point2i(size.x / 2 + (int) (interactPosition.x * SCALE),
                                size.y / 2 - (int) (interactPosition.y * SCALE));
                    } else {
                        monitorEntity.interactPosition = null;
                    }
                } else {
                    monitorEntity.interactPosition = null;
                }

                Avatar avatar = avatarMapper.get(entity);
                if (avatar != null) {
                    monitorEntity.name = avatar.name;

                    Observed observed = observedMapper.get(entity);
                    if (observed != null && observed.observerEntity >= 0) {
                        Point2f observedPosition = transformMapper.get(observed.observerEntity).position;
                        monitorEntity.observedPosition = new Point2i(size.x / 2 + (int) (observedPosition.x * SCALE),
                                size.y / 2 - (int) (observedPosition.y * SCALE));
                    } else {
                        monitorEntity.observedPosition = null;
                    }
                } else {
                    monitorEntity.name = null;
                }
            }

            if (selectedMonitorEntity != null) {
                if (observerMapper.has(selectedMonitorEntity.entity)) {
                    Observer observer = observerMapper.get(selectedMonitorEntity.entity);
                    monitorEntity.colored = observer.knowEntities.contains(entity);
                } else if (scanMapper.has(selectedMonitorEntity.entity)) {
                    monitorEntity.colored = scanMapper.get(selectedMonitorEntity.entity).nearbyEntities.contains(entity);
                } else {
                    monitorEntity.colored = true;
                }
            } else {
                monitorEntity.colored = true;
            }
        }

        if (debugSettings.get("Quad")) {
            quadRects.clear();
            addQuadContainers(vastWorld.getQuadTree(), quadRects);
        }
    }

    private void addQuadContainers(QuadTree quadTree, List<Rectangle> quadRectsToAddTo) {
        int x = (int) (quadTree.getBounds().getX() * SCALE);
        int y = (int) -(quadTree.getBounds().getY() * SCALE);
        y -= quadTree.getBounds().getHeight() * SCALE;
        y += size.y;
        quadRectsToAddTo.add(new Rectangle(x, y,
                (int) (quadTree.getBounds().getWidth() * SCALE), (int) (quadTree.getBounds().getHeight() * SCALE)));
        for (QuadTree child : quadTree.getNodes()) {
            if (child != null) {
                addQuadContainers(child, quadRectsToAddTo);
            }
        }
    }

    private MonitorEntity getMonitorEntityClosestTo(Point2D point) {
        MonitorEntity monitorEntityClosestToClick = null;
        float closestDistance2 = Float.MAX_VALUE;
        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            float dx = Math.abs((float) (monitorEntity.position.x - point.getX()));
            float dy = Math.abs((float) (monitorEntity.position.y - point.getY()));
            float distance2 = dx * dx + dy * dy;
            if (monitorEntityClosestToClick == null || distance2 < closestDistance2) {
                monitorEntityClosestToClick = monitorEntity;
                closestDistance2 = distance2;
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
            monitorEntity.paintDebugBottom(g, debugSettings);
        }

        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            monitorEntity.paint(g);
        }

        if (selectedMonitorEntity != null) {
            int size;
            float secondsSinceSelected = (System.currentTimeMillis() - selectedTime) / 1000f;
            if (secondsSinceSelected < 0.2f) {
                size = 30 + (int) ((0.2f - secondsSinceSelected) * 40);
            } else {
                size = 30;
            }

            g.setColor(Color.WHITE);
            g.drawArc(selectedMonitorEntity.position.x - size / 2, selectedMonitorEntity.position.y - size / 2, size, size, 0, 360);
        }

        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            monitorEntity.paintDebugTop(g, debugSettings);
        }
    }
}
