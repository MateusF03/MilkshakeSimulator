package me.mateus.milkshake.core.milkshake;

import java.util.Map;

public class Milkshake {

    private final Template template;
    private final Map<Integer, Source> sources;
    private String imagePath;

    public Milkshake(Template template, Map<Integer, Source> sources) {
        this.template = template;
        this.sources = sources;
    }

    public Template getTemplate() {
        return template;
    }

    public Map<Integer, Source> getSources() {
        return sources;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
