package me.mateus.milkshake.core.milkshake;

import com.google.gson.*;
import me.mateus.milkshake.core.milkshake.serializer.SourceRegionSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MilkshakeManager {

    private MilkshakeManager() {}

    private static MilkshakeManager instance;
    public static MilkshakeManager getInstance() {
        if (instance == null) {
            synchronized (MilkshakeManager.class) {
                if (instance == null) {
                    instance = new MilkshakeManager();
                }
            }
        }
        return instance;
    }

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SourceRegion.class, new SourceRegionSerializer())
            .setPrettyPrinting()
            .create();

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
                        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                        Template template = new Template(jsonObject);
                        template.setOriginalFilePath(file.getPath());
                        BufferedImage image = ImageIO.read(new File(template.getImagePath()));
                        template.setWidth(image.getWidth());
                        template.setHeight(image.getHeight());
                        templates.add(template);
                    }
                }
            }
        }
    }

    public void addSource(Source source) {
        sources.add(source);
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
                        Source source = GSON.fromJson(content, Source.class);
                        source.setFilePath(file.getPath());
                        sources.add(source);
                    }
                }
            }
        }
    }

    public void addTemplate(Template template) {
        templates.add(template);
    }
    public Template getTemplateByName(String name) {
        return templates.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public List<Source> getSources() {
        return sources;
    }

    public List<Source> getSourcesByName(String name) {
        return sources.stream().filter(s -> s.getName().equals(name)).collect(Collectors.toList());
    }

    private void loadSourceTexts(File sourceTextsFile) throws IOException {
        String content = new String(Files.readAllBytes(sourceTextsFile.toPath()), StandardCharsets.UTF_8);
        if (content.isEmpty()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("texts", new JsonArray());
            Files.write(sourceTextsFile.toPath(), GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
        } else {
            JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
            JsonArray array = jsonObject.get("texts").getAsJsonArray();

            for (JsonElement jsonElement : array) {
                String text = jsonElement.getAsString().trim();
                text = text.substring(0, Math.min(text.length(), 64));
                sources.add(new Source("", "", "", text));
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
        Files.write(sourceTextsFile.toPath(), GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }
}
