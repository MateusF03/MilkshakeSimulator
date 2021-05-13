package me.mateus.milkshake.core.milkshake;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Source {
    private String name;
    @SerializedName("image-path")
    private final String imagePath;
    private transient final String text;
    private transient String filePath;

    public Source(String name, String imagePath, String filePath, String text) {
        this.name = name;
        this.imagePath = imagePath;
        this.filePath = filePath;
        this.text = text;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void rename(String name) throws IOException {
        this.name = name;
        File file = new File(filePath);
        String json = MilkshakeManager.GSON.toJson(this);
        Files.write(file.toPath(),json.getBytes(StandardCharsets.UTF_8));
    }

    public boolean errorWhileDeleting() {
        File file = new File(filePath);
        File imageFile = new File(imagePath);
        if (!imageFile.delete()) {
            return true;
        }
        return !file.delete();
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        if (text == null) {
            return name;
        }
        if (text.isEmpty()) {
            return name;
        }
        return text;
    }
}