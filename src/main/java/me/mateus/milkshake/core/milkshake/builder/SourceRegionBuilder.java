package me.mateus.milkshake.core.milkshake.builder;

import me.mateus.milkshake.core.milkshake.Point;
import me.mateus.milkshake.core.milkshake.SourceRegion;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class SourceRegionBuilder {

    private final int sourceIdx;

    private final Point[] points = new Point[4];

    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    private int priority = 0;
    private boolean isText = false;
    private String color = "";
    private String orientation = "";
    private String font  = "";
    private String strokeColor = "";
    private int strokeWidth = 0;

    public SourceRegionBuilder(int sourceIdx,int x, int y, int width, int height) {
        this.sourceIdx = sourceIdx;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        Arrays.fill(points, null);
    }

    public SourceRegionBuilder(int sourceIdx, Point... points) {
        if (points.length != 4) {
            throw new InvalidParameterException("Os pontos devem ter o tamanho de 4");
        }
        this.sourceIdx = sourceIdx;
        System.arraycopy(points, 0, this.points, 0, points.length);
    }

    public SourceRegionBuilder setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public SourceRegionBuilder setText(boolean text) {
        this.isText = text;
        return this;
    }

    public SourceRegionBuilder setColor(String color) {
        this.color = color;
        return this;
    }

    public SourceRegionBuilder setOrientation(String orientation) {
        this.orientation = orientation;
        return this;
    }
    public SourceRegionBuilder setFont(String font) {
        this.font = font;
        return this;
    }

    public SourceRegionBuilder setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public SourceRegionBuilder setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public SourceRegion build() {
        return new SourceRegion(x,y,width,height,priority,isText,sourceIdx,color,orientation,font,strokeColor,strokeWidth,points);
    }
}
