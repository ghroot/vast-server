package com.vast.monitor;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.components.SerializationTag;
import com.artemis.utils.Bag;
import com.vast.VastWorld;
import com.vast.component.*;
import com.vast.data.WorldConfiguration;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class MonitorWorld {
    private final int SCALE = 5;

    private DebugSettings debugSettings;
    private Point2i size;
    private List<MonitorEntity> monitorEntities;
    private static int selectedEntity = -1;

    public MonitorWorld(WorldConfiguration worldConfiguration, VastWorld vastWorld, DebugSettings debugSettings, Point2D clickPoint) {
        this.debugSettings = debugSettings;

        size = new Point2i(worldConfiguration.width * SCALE, worldConfiguration.height * SCALE);
        monitorEntities = new ArrayList<>();

        MonitorEntity monitorEntityClosestToClick = null;
        float closestDistance = Float.MAX_VALUE;

        int[] entities = vastWorld.getEntities(Aspect.all(Transform.class));
        for (int entity : entities) {
            MonitorEntity monitorEntity = new MonitorEntity();
            monitorEntity.entity = entity;
            monitorEntity.type = vastWorld.getComponentMapper(com.vast.component.Type.class).get(entity).type;
            Point2f position = vastWorld.getComponentMapper(Transform.class).get(entity).position;
            monitorEntity.position = new Point2i(
                    size.x / 2 + (int) (position.x * SCALE),
                    size.y / 2 - (int) (position.y * SCALE));

            if (clickPoint != null) {
                float dx = Math.abs((float) (monitorEntity.position.x - clickPoint.getX()));
                float dy = Math.abs((float) (monitorEntity.position.y - clickPoint.getY()));
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (monitorEntityClosestToClick == null || distance < closestDistance) {
                    monitorEntityClosestToClick = monitorEntity;
                    closestDistance = distance;
                }
            }

            if (vastWorld.getComponentMapper(Collision.class).has(entity)) {
                monitorEntity.collisionRadius = (int) (vastWorld.getComponentMapper(Collision.class).get(entity).radius * SCALE);
            }

            if (vastWorld.getComponentMapper(Path.class).has(entity)) {
                Point2f targetPosition = vastWorld.getComponentMapper(Path.class).get(entity).targetPosition;
                monitorEntity.pathPosition = new Point2i(
                        size.x / 2 + (int) (targetPosition.x * SCALE),
                        size.y / 2 - (int) (targetPosition.y * SCALE));
            }

            if (vastWorld.getComponentMapper(Player.class).has(entity)) {
                monitorEntity.name = vastWorld.getComponentMapper(Player.class).get(entity).name;
            }

            monitorEntities.add(monitorEntity);
        }

        if (monitorEntityClosestToClick != null) {
            selectedEntity = monitorEntityClosestToClick.entity;
        }

        if (selectedEntity >= 0) {
            MonitorEntity selectedMonitorEntity = null;
            for (MonitorEntity monitorEntity : monitorEntities) {
                if (monitorEntity.entity == selectedEntity) {
                    selectedMonitorEntity = monitorEntity;
                    break;
                }
            }
            if (selectedMonitorEntity != null) {
                Bag<Component> components = new Bag<>();
                vastWorld.getWorld().getEntity(selectedEntity).getComponents(components);
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
                        detail = "" + ((Follow) component).entity;
                    } else if (component instanceof Group) {
                        detail = "" + ((Group) component).id;
                    } else if (component instanceof Order) {
                        Order order = (Order) component;
                        if (order.handler != null) {
                            detail = "" + order.handler.getClass().getSimpleName();
                        }
                    } else if (component instanceof Speed) {
                        detail = "" + (Math.round(((Speed) component).getModifiedSpeed() * 100.0f) / 100.0f);
                    } else if (component instanceof Transform) {
                        detail = "" + (Math.round(((Transform) component).position.x * 100.0f) / 100.0f) + ", " + (Math.round(((Transform) component).position.y * 100.0f) / 100.0f);
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
                    }
                    if (detail != null) {
                        selectedMonitorEntity.components.add(componentName + " (" + detail + ")");
                    } else {
                        selectedMonitorEntity.components.add(componentName);
                    }
                }
            }
        }
    }

    public Point2i getSize() {
        return size;
    }

    public void paint(Graphics g) {
        for (MonitorEntity monitorEntity : monitorEntities) {
            monitorEntity.paint(g, debugSettings);
        }
    }
}
