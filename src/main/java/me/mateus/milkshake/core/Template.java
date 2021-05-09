package me.mateus.milkshake.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.mateus.milkshake.MilkshakeSimulator;
import me.mateus.milkshake.core.runnable.GenerateImage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

public class Template {


    private final String name;
    private final BufferedImage templateImage;
    private final List<SourceRegion> sourceRegions;
    private final File imageFile;
    private File originalFile;

    public Template(String name, BufferedImage templateImage, List<SourceRegion> sourceRegions, File imageFile) {
        this.name = name;
        this.templateImage = templateImage;
        this.sourceRegions = sourceRegions;
        this.imageFile = imageFile;
    }

    public JsonObject toJsonObject(String filePath) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("image-path", filePath);
        JsonArray jsonArray = new JsonArray();

        for (SourceRegion sourceRegion : sourceRegions) {
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
            jsonArray.add(regionObject);
        }

        jsonObject.add("regions", jsonArray);
        return jsonObject;
    }

    public List<SourceRegion> getSourceRegions() {
        return sourceRegions;
    }

    public void createMilkshake(Map<Integer, Source> sources, GuildMessageReceivedEvent event, Message message, boolean forceful) {
        if (!forceful && MilkshakeSimulator.processing) {
            return;
        }
        MilkshakeSimulator.processing = true;
        Thread thread = new Thread(new GenerateImage(event, sources, message, sourceRegions, imageFile.getPath(), templateImage.getWidth(), templateImage.getHeight()));
        thread.start();
    }

    public void setOriginalFile(File originalFile) {
        this.originalFile = originalFile;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public File getImageFile() {
        return imageFile;
    }

    public String getName() {
        return name;
    }
}
