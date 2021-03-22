package com.vast.monitor;

import javax.imageio.ImageIO;
import javax.vecmath.Point2i;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    public int scanDistance;
    public Point2i pathPosition;
    public Point2i interactPosition;
    public String name;
    public boolean colored = true;
    public List<MonitorComponent> components;

    private static Map<String, Image> coloredEntityImages;
    private static Map<String, Image> grayEntityImages;

    static {
        coloredEntityImages = new HashMap<>();
        coloredEntityImages.put("player", colorImage("images/User-Icon.png", Color.WHITE));
        coloredEntityImages.put("animal", colorImage("images/Demon.png", Color.CYAN));
        coloredEntityImages.put("pickup", colorImage("images/Chest-Icon.png", Color.BLUE));
        coloredEntityImages.put("tree", colorImage("images/Tree-Icon.png", Color.GREEN));
        coloredEntityImages.put("rock", colorImage("images/Diamond-Icon.png", Color.GRAY));
        coloredEntityImages.put("building", colorImage("images/Home-Icon.png", Color.YELLOW));

        grayEntityImages = new HashMap<>();
        grayEntityImages.put("player", colorImage("images/User-Icon.png", Color.DARK_GRAY));
        grayEntityImages.put("animal", colorImage("images/Demon.png", Color.DARK_GRAY));
        grayEntityImages.put("pickup", colorImage("images/Chest-Icon.png", Color.DARK_GRAY));
        grayEntityImages.put("tree", colorImage("images/Tree-Icon.png", Color.DARK_GRAY));
        grayEntityImages.put("rock", colorImage("images/Diamond-Icon.png", Color.DARK_GRAY));
        grayEntityImages.put("building", colorImage("images/Home-Icon.png", Color.DARK_GRAY));
    }

    private static BufferedImage colorImage(String imageName, Color color) {
        try {
            BufferedImage image = ImageIO.read(new File(imageName));
            int w = image.getWidth();
            int h = image.getHeight();
            BufferedImage dyed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dyed.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.setComposite(AlphaComposite.SrcAtop);
            g.setColor(color);
            g.fillRect(0, 0, w, h);
            g.dispose();
            return dyed;
        } catch (IOException exception) {
            return null;
        }
    }

    public MonitorEntity(int entity) {
        this.entity = entity;
    }

    public void paint(Graphics g) {
        Image image = colored ? coloredEntityImages.get(type) : grayEntityImages.get(type);
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
        if (debugSettings.get("Collision") && collisionRadius > 0) {
            g.setColor(Color.BLUE);
            g.drawArc(position.x - collisionRadius, position.y - collisionRadius,
                    collisionRadius * 2, collisionRadius * 2, 0, 360);
        }

        if (debugSettings.get("Scan") && scanDistance > 0) {
            g.setColor(Color.BLUE);
            g.drawRect(position.x - scanDistance, position.y - scanDistance, 2 * scanDistance, 2 * scanDistance);
        }

        if (debugSettings.get("Path") && pathPosition != null) {
            g.setColor(Color.YELLOW);
            g.drawLine(position.x, position.y, pathPosition.x, pathPosition.y);
        }

        if (debugSettings.get("Interact") && interactPosition != null) {
            g.setColor(Color.MAGENTA);
            drawDashedLine(g, position.x, position.y, interactPosition.x, interactPosition.y);
        }

        if (debugSettings.get("Name") && name != null) {
            g.setColor(Color.WHITE);
            g.drawString(name, position.x + 7, position.y + 5);
        }
    }

    public void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2) {
        Graphics2D g2d = (Graphics2D) g.create();
        Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
        g2d.setStroke(dashed);
        g2d.drawLine(x1, y1, x2, y2);
        g2d.dispose();
    }
}
