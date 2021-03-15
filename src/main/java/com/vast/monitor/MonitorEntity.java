package com.vast.monitor;

import javax.imageio.ImageIO;
import javax.vecmath.Point2i;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorEntity {
    public final int entity;
    public String type;
    public Point2i position;
    public int collisionRadius;
    public Point2i pathPosition;
    public String name;
    public List<String> components;

    private static Map<String, Image> entityImages;

    static {
        try {
            entityImages = new HashMap<>();
            entityImages.put("player", ImageIO.read(new File("images/User-Icon.png")));
            entityImages.put("animal", ImageIO.read(new File("images/Demon.png")));
            entityImages.put("pickup", ImageIO.read(new File("images/Chest-Icon.png")));
            entityImages.put("tree", ImageIO.read(new File("images/Tree-Icon.png")));
            entityImages.put("rock", ImageIO.read(new File("images/Diamond-Icon.png")));
            entityImages.put("building", ImageIO.read(new File("images/Home-Icon.png")));
        } catch (IOException ignored) {
        }
    }

    public MonitorEntity(int entity) {
        this.entity = entity;
    }

    public void paint(Graphics g) {
        if (!g.getClipBounds().contains(position.x, position.y)) {
            return;
        }

        Image image = entityImages.get(type);
        if (image != null) {
            int width = 12;
            int height = (int) (((float) image.getHeight(null) / image.getWidth(null)) * width);
            g.drawImage(image, position.x - width / 2, position.y - height / 2, width, height, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(position.x - 5, position.y - 5, 10, 10);
        }
    }

    public void paintDebug(Graphics g, Map<String, Boolean> debugSettings) {
        if (!g.getClipBounds().contains(position.x, position.y)) {
            return;
        }

        if (debugSettings.get("Collision") && collisionRadius > 0) {
            g.setColor(Color.BLUE);
            g.drawArc(position.x - collisionRadius, position.y - collisionRadius,
                    collisionRadius * 2, collisionRadius * 2, 0, 360);
        }

        if (debugSettings.get("Path") && pathPosition != null) {
            g.setColor(Color.YELLOW);
            g.drawLine(position.x, position.y, pathPosition.x, pathPosition.y);
        }

        if (debugSettings.get("Name") && name != null) {
            g.setColor(Color.WHITE);
            g.drawString(name, position.x + 7, position.y + 5);
        }

        if (components != null) {
            g.setColor(Color.WHITE);
            g.drawArc(position.x - 15, position.y - 15, 30, 30, 0, 360);

            int y = position.y - 20;
            for (String componentInfo : components) {
                g.drawString(componentInfo, position.x + 20, y);
                y += 14;
            }
        }
    }
}
