package me.mateus.milkshake.core;

public class SourceRegion {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int priority;
    private final boolean isText;
    private final int sourceName;
    private final String color;
    private final String orientation;
    private final String font;
    private final String strokeColor;
    private final int strokeWidth;

    public SourceRegion(int x, int y, int width, int height, int priority, boolean isText, int sourceName, String color, String orientation, String font, String strokeColor, int strokeWidth) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.priority = priority;
        this.isText = isText;
        this.sourceName = sourceName;
        this.color = color;
        this.orientation = orientation;
        this.font = font;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isText() {
        return isText;
    }

    public int getSourceName() {
        return sourceName;
    }

    public String getColor() {
        return color;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getFont() {
        return font;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }
}
