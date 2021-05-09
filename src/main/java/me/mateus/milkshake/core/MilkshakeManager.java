package me.mateus.milkshake.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.mateus.milkshake.MilkshakeSimulator;
import me.mateus.milkshake.core.parser.TemplateParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MilkshakeManager {

    private static MilkshakeManager instance;
    private MilkshakeManager() {}

    public static MilkshakeManager getInstance() {
        if (instance == null) {
            synchronized (MilkshakeManager.class) {
                instance = new MilkshakeManager();
            }
        }
        return instance;
    }

    private final Logger LOGGER = LoggerFactory.getLogger(MilkshakeManager.class);
    private final List<Template> templates = new ArrayList<>();
    private final List<Source> sources = new ArrayList<>();

    public void setupMilkshakes() throws IOException {
        File templatesFolder = new File("templates");
        if (!templatesFolder.exists() && !templatesFolder.mkdirs()) {
            LOGGER.error("Não foi possível criar a pasta de template");
            return;
        }
        File sourcesFolder = new File("sources");
        if (!sourcesFolder.exists() && !sourcesFolder.mkdirs()) {
            LOGGER.error("Não foi possível criar a pasta de sources");
            return;
        }
        File sourceTexts = new File("texts.json");
        if (!sourceTexts.exists() && !sourceTexts.createNewFile()) {
            LOGGER.error("Não foi possível criar o arquivo de textos");
            return;
        }
        loadTemplates(templatesFolder);
        loadSources(sourcesFolder);
        loadSourceTexts(sourceTexts);


    }

    public void addText(String text) throws IOException {
        File sourceTextsFile = new File("texts.json");
        if (!sourceTextsFile.exists() && !sourceTextsFile.createNewFile()) {
            LOGGER.error("Não foi possível criar o arquivo de textos");
            return;
        }
        String content = new String(Files.readAllBytes(sourceTextsFile.toPath()));
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
        JsonArray array = jsonObject.get("texts").getAsJsonArray();

        sources.add(new Source("","","",text));
        array.add(text);
        jsonObject.add("texts", array);
        Files.write(sourceTextsFile.toPath(), MilkshakeSimulator.GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }

    public Template getTemplateByName(String name) {
        return templates.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
    }

    public Source getSourceByName(String name) {
        return sources.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }

    private void loadTemplates(File templatesFolder) throws IOException {
        if (!templatesFolder.isDirectory()) {
            return;
        }
        File[] files = templatesFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    loadTemplates(file);
                } else {
                    String extension = getFileExtension(file);
                    if (extension.equals(".milkshake")) {
                        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                        templates.add(TemplateParser.parseContent(content, file));
                    }
                }
            }
        }
    }

    private void loadSourceTexts(File sourceTextsFile) throws IOException {
        String content = new String(Files.readAllBytes(sourceTextsFile.toPath()), StandardCharsets.UTF_8);
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
        JsonArray array = jsonObject.get("texts").getAsJsonArray();

        for (JsonElement jsonElement : array) {
            String text = jsonElement.getAsString().trim();
            sources.add(new Source("", "", "", text));
        }
    }

    private void loadSources(File sourceFolder) throws IOException {
        if (!sourceFolder.isDirectory()) {
            return;
        }
        File[] files = sourceFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    loadSources(file);
                } else {
                    String extension = getFileExtension(file);
                    if (extension.equals(".sundae")) {
                        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                        String name = jsonObject.get("name").getAsString();
                        sources.add(new Source(name, jsonObject.get("image-path").getAsString(),file.getPath(), ""));
                    }
                }
            }
        }
    }


    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public List<Source> getSources() {
        return sources;
    }

}
