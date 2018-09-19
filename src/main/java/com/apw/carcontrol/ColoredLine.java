package com.apw.carcontrol;

import com.apw.steering.steeringclasses.Point;

import java.awt.*;

/**
 * <Code>ColoredLine</Code> stores the start and end points of a line and an associated <Code>Color</Code>.
 * Used to draw lines over TrakSim.
 */
public class ColoredLine {

    /**
     * The start point of the line.
     */
    private final Point start;

    /**
     * The end point of the line.
     */
    private final Point end;

    /**
     * The color of the line.
     */
    private final Color color;

    /**
     * Creates a new <Code>ColoredLine</Code> from two Points and a <Code>Color</Code>.
     * @param start The start point of the line.
     * @param end The end point of the line.
     * @param color The color of the line as a <Code>Color</Code>.
     */
    public ColoredLine(Point start, Point end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    /**
     * Creates a new black <Code>ColoredLine</Code> from two Points.
     * @param start The start point of the line.
     * @param end The end point of the line.
     */
    public ColoredLine(Point start, Point end) {
        this(start, end, Color.black);
    }

    /**
     * Creates a new <Code>ColoredLine</Code> from two Points and a color in hex.
     * @param start The start point of the line.
     * @param end The end point of the line.
     * @param hexColor The color of the line in hex.
     */
    public ColoredLine(Point start, Point end, int hexColor) {
        this(start, end, Color.decode(Integer.toString(hexColor)));
    }

    /**
     * Creates a new <Code>ColoredLine</Code> from two sets of integer positions and a color in hex.
     * @param x1 The x position of the start point of the line.
     * @param y1 The y position of the start point of the line.
     * @param x2 The x position of the end point of the line.
     * @param y2 The y position of the end point of the line.
     * @param hexColor The color of the line in hex.
     */
    public ColoredLine(int x1, int y1, int x2, int y2, int hexColor) {
        this(new Point(x1, y1), new Point(x2, y2), Color.decode(Integer.toString(hexColor)));
    }

    /**
     * Creates a new <Code>ColoredLine</Code> from two sets of integer positions and a <Code>Color</Code>.
     * @param x1 The x position of the start point of the line.
     * @param y1 The y position of the start point of the line.
     * @param x2 The x position of the end point of the line.
     * @param y2 The y position of the end point of the line.
     * @param color The color of the line as a <Code>Color</Code>.
     */
    public ColoredLine(int x1, int y1, int x2, int y2, Color color) {
        this(new Point(x1, y1), new Point(x2, y2), color);
    }

    /**
     * Creates a new black <Code>ColoredLine</Code> from two sets of integer positions.
     * @param x1 The x position of the start point of the line.
     * @param y1 The y position of the start point of the line.
     * @param x2 The x position of the end point of the line.
     * @param y2 The y position of the end point of the line.
     */
    public ColoredLine(int x1, int y1, int x2, int y2) {
        this(new Point(x1, y1), new Point(x2, y2), Color.black);
    }

    /**
     * Returns the start point of the line.
     * @return The lines start point as a <Code>Point</Code>.
     */
    public Point getStart() {
        return start;
    }

    /**
     * Returns the end point of the line.
     * @return The lines end point as a <Code>Point</Code>.
     */
    public Point getEnd() {
        return end;
    }

    /**
     * Returns the color of the line.
     * @return The lines color as a <Code>Color</Code>.
     */
    public Color getColor() {
        return color;
    }
}
