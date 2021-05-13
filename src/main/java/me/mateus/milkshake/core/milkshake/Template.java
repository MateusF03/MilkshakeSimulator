package me.mateus.milkshake.core.milkshake;

import com.google.gson.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Template {

    private final String name;
    private final List<SourceRegion> regions;
    private final String imagePath;
    private transient String originalFilePath;
    private transient int width;
    private transient int height;

    public Template(String name, List<SourceRegion> sourceRegions, String imagePath) {
        this.name = name;
        this.regions = sourceRegions;
        this.imagePath = imagePath;
    }

    public Template(JsonObject jsonObject) {
        this.name = jsonObject.get("name").getAsString();
        this.imagePath = jsonObject.get("image-path").getAsString();
        JsonArray array = jsonObject.get("regions").getAsJsonArray();
        List<SourceRegion> regions = new ArrayList<>();
        for (JsonElement jsonElement : array) {
            SourceRegion region = MilkshakeManager.GSON.fromJson(jsonElement, SourceRegion.class);
            regions.add(region);
        }
        this.regions = regions;
    }

    public JsonObject toJsonObject(Gson gson) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("image-path", imagePath);
        JsonArray jsonArray = new JsonArray();
        for (SourceRegion region : regions) {
            String json = gson.toJson(region, SourceRegion.class);
            JsonObject regionObject = JsonParser.parseString(json).getAsJsonObject();
            jsonArray.add(regionObject);
        }
        jsonObject.add("regions", jsonArray);
        return jsonObject;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public List<SourceRegion> getSourceRegions() {
        return regions;
    }

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public boolean delete() {
        File imageFile = new File(getImagePath());
        if (!imageFile.delete()) {
            return false;
        }
        if (originalFilePath != null) {
            File originalFile = new File(originalFilePath);
            return originalFile.delete();
        }
        return true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
