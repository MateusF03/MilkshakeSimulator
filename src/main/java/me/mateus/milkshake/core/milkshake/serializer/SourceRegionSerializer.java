package me.mateus.milkshake.core.milkshake.serializer;

import com.google.gson.*;
import me.mateus.milkshake.core.milkshake.Point;
import me.mateus.milkshake.core.milkshake.SourceRegion;
import me.mateus.milkshake.core.milkshake.builder.SourceRegionBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SourceRegionSerializer implements JsonSerializer<SourceRegion>, JsonDeserializer<SourceRegion> {
    @Override
    public SourceRegion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject regionObject = json.getAsJsonObject();
        int sourceIdx = regionObject.get("source-name").getAsInt();
        SourceRegionBuilder builder;
        if (regionObject.has("point-1")
                && regionObject.has("point-2")
                && regionObject.has("point-3")
                && regionObject.has("point-4")) {
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                points.add(arrayToPoint(regionObject.get("point-" + (i + 1)).getAsJsonArray()));
            }
            builder = new SourceRegionBuilder(sourceIdx, points.toArray(new Point[0]));
        } else {
            int x = regionObject.get("x").getAsInt();
            int y = regionObject.get("y").getAsInt();
            int width = regionObject.get("width").getAsInt();
            int height = regionObject.get("height").getAsInt();
            builder = new SourceRegionBuilder(sourceIdx,x,y,width,height);
        }
        builder.setPriority(regionObject.get("priority").getAsInt());
        builder.setText(regionObject.get("is-text").getAsBoolean());
        if (regionObject.has("color")) {
            builder.setColor(regionObject.get("color").getAsString());
        }
        if (regionObject.has("orientation")) {
            builder.setOrientation(regionObject.get("orientation").getAsString());
        }
        if (regionObject.has("font")) {
            builder.setFont(regionObject.get("font").getAsString());
        }
        if (regionObject.has("stroke-color")) {
            builder.setStrokeColor(regionObject.get("stroke-color").getAsString());
        }
        if (regionObject.has("stroke-width")) {
            builder.setStrokeWidth(regionObject.get("stroke-width").getAsInt());
        }
        return builder.build();
    }

    @Override
    public JsonElement serialize(SourceRegion sourceRegion, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject regionObject = new JsonObject();
        regionObject.addProperty("x", sourceRegion.getX());
        regionObject.addProperty("y", sourceRegion.getY());
        regionObject.addProperty("width", sourceRegion.getWidth());
        regionObject.addProperty("height", sourceRegion.getHeight());
        regionObject.addProperty("priority", sourceRegion.getPriority());
        regionObject.addProperty("is-text", sourceRegion.isText());
        regionObject.addProperty("source-name", sourceRegion.getSourceName());
        regionObject.addProperty("color", sourceRegion.getColor());
        regionObject.addProperty("orientation", sourceRegion.getOrientation());
        regionObject.addProperty("font", sourceRegion.getFont());
        regionObject.addProperty("stroke-color", sourceRegion.getStrokeColor());
        regionObject.addProperty("stroke-width", sourceRegion.getStrokeWidth());
        int idx = 1;
        for (Point point : sourceRegion.getPoints()) {
            if (point == null)
                continue;
            regionObject.add("point-" + idx, pointToArray(point));
            idx++;
        }
        return regionObject;
    }

    private JsonArray pointToArray(Point point) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(point.getX());
        jsonArray.add(point.getY());
        return jsonArray;
    }

    private Point arrayToPoint(JsonArray jsonArray) {
        if (jsonArray.size() != 2) {
            return null;
        }
        int x = jsonArray.get(0).getAsInt();
        int y = jsonArray.get(1).getAsInt();
        return new Point(x,y);
    }
}
