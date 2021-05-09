package me.mateus.milkshake.core.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.mateus.milkshake.core.SourceRegion;
import me.mateus.milkshake.core.Template;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TemplateParser {

    public static Template parseContent(String content, File originalFile) throws IOException {
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
        
        String name = jsonObject.get("name").getAsString().trim();
        String imagePath = jsonObject.get("image-path").getAsString();

        File imageFile = Paths.get(imagePath).toFile();

        BufferedImage image = ImageIO.read(imageFile);

        List<SourceRegion> sourceRegions = new ArrayList<>();

        JsonArray regionArray = jsonObject.get("regions").getAsJsonArray();
        for (JsonElement jsonElement : regionArray) {
            JsonObject regionObject = jsonElement.getAsJsonObject();
            int x = regionObject.get("x").getAsInt();
            int y = regionObject.get("y").getAsInt();
            int width = regionObject.get("width").getAsInt();
            int height = regionObject.get("height").getAsInt();
            int priority = regionObject.get("priority").getAsInt();
            boolean isText = regionObject.get("is-text").getAsBoolean();
            int sourceName = -1;
            String color = "";
            String orientation = "";
            String font = "";
            if (regionObject.has("source-name")) {
                sourceName = regionObject.get("source-name").getAsInt();
            }
            if (regionObject.has("color")) {
                color = regionObject.get("color").getAsString();
            }
            if (regionObject.has("orientation")) {
                orientation = regionObject.get("orientation").getAsString();
            }
            if (regionObject.has("font")) {
                font = regionObject.get("font").getAsString();
            }
            sourceRegions.add(new SourceRegion(x, y, width, height, priority, isText, sourceName, color, orientation, font));
        }
        Template template = new Template(name, image, sourceRegions, imageFile);
        template.setOriginalFile(originalFile);
        return template;
    }
}
