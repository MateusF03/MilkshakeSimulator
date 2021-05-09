package me.mateus.milkshake.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.mateus.milkshake.MilkshakeSimulator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Source {
    private String name;
    private final String imagePath;
    private final String text;
    private final String filePath;

    public Source(String name, String imagePath, String filePath, String text) {
        this.name = name;
        this.imagePath = imagePath;
        this.filePath = filePath;
        this.text = text;
    }

    public void rename(String name) throws IOException {
        this.name = name;
        File file = new File(filePath);
        String content = new String(Files.readAllBytes(file.toPath()));
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
        jsonObject.addProperty("name", name);
        Files.write(file.toPath(), MilkshakeSimulator.GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text.isEmpty() ? name : text;
    }
}
