package com.apw.carcontrol;

import com.apw.steering.Point;

import java.awt.*;

/**
 * <Code>ColoredRect</Code> stores the position, width, and height of a rectangle and an associated <Code>Color</Code>.
 * Used to draw rectangles over TrakSim.
 */
public class ColoredRect {

    /**
     * The color of the rect.
     */
    private final Color color;

    /**
     * The position of the rect.
     */
    private final Point position;

    /**
     * The width of the rect.
     */
    private final int width;

    /**
     * The height of the rect.
     */
    private final int height;

    /**
     * Creates a new <Code>ColoredRect</Code> from a point, width, height, and <Code>Color</Code>.
     * @param position The position of the rect as a <Code>Point</Code>.
     * @param width The width of the rect.
     * @param height The height of the rect.
     * @param color The color of the rect as a <Code>Color</Code>.
     */
    public ColoredRect(Point position, int width, int height, Color color) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    /**
     * Creates a new <Code>ColoredRect</Code> from a point, width, height, and hex color.
     * @param position The position of the rect as a <Code>Point</Code>.
     * @param width The width of the rect.
     * @param height The height of the rect.
     * @param hexColor The color of the rect in hex.
     */
    public ColoredRect(Point position, int width, int height, int hexColor) {
        this(position, width, height, Color.decode(Integer.toString(hexColor)));
    }

    /**
     * Creates a new black <Code>ColoredRect</Code> from a point, width, and height.
     * @param position The position of the rect as a <Code>Point</Code>.
     * @param width The width of the rect.
     * @param height The height of the rect.
     */
    public ColoredRect(Point position, int width, int height) {
        this(position, width, height, Color.BLACK);
    }

    /**
     * Creates a new <Code>ColoredRect</Code> from a set of integer positions, a width, a height, and a <Code>Color</Code>.
     * @param x The x coordinate of the rect.
     * @param y The y coordinate of the rect.
     * @param width The width of the rect.
     * @param height The height of the rect.
     * @param color The color of the rect as a <Code>Color</Code>.
     */
    public ColoredRect(int x, int y, int width, int height, Color color) {
        this(new Point(x, y), width, height, color);
    }

    /**
     * Creates a new <Code>ColoredRect</Code> from a set of integer positions, a width, a height, and a hex color.
     * @param x The x coordinate of the rect.
     * @param y The y coordinate of the rect.
     * @param width The width of the rect.
     * @param height The height of the rect.
     * @param hexColor The color of the rect in hex.
     */
    public ColoredRect(int x, int y, int width, int height, int hexColor) {
        this(new Point(x, y), width, height, Color.decode(Integer.toString(hexColor)));
    }

    /**
     * Creates a new black <Code>ColoredRect</Code> from a set of integer positions, a width, and a height.
     * @param x The x coordinate of the rect.
     * @param y The y coordinate of the rect.
     * @param width The width of the rect.
     * @param height The height of the rect.
     */
    public ColoredRect(int x, int y, int width, int height) {
        this(new Point(x, y), width, height, Color.BLACK);
    }

    /**
     * Returns the color of the rect.
     * @return The rect's color as a <Code>Color</Code>.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the position of the rect.
     * @return The rect's position as a <Code>Point</Code>.
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Returns the width of the rect.
     * @return The rect's width as an int.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the rect.
     * @return The rect's height as an int.
     */
    public int getHeight() {
        return height;
    }
}
