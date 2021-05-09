package me.mateus.milkshake.core.runnable;

import me.mateus.milkshake.MilkshakeSimulator;
import me.mateus.milkshake.core.Source;
import me.mateus.milkshake.core.SourceRegion;
import me.mateus.milkshake.core.utils.ImageUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateImage implements Runnable {

    private final GuildMessageReceivedEvent event;
    private final Map<Integer, Source> sources;
    private final Message message;
    private final List<SourceRegion> sourceRegions;
    private final String templateImage;
    private final int width;
    private final int height;

    public GenerateImage(GuildMessageReceivedEvent event, Map<Integer, Source> sources, Message message, List<SourceRegion> sourceRegions, String templateImage, int width, int height) {

        this.event = event;
        this.sources = sources;
        this.message = message;
        this.sourceRegions = sourceRegions;
        this.templateImage = templateImage;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
        List<SourceRegion> textRegions = sourceRegions.stream().filter(SourceRegion::isText).collect(Collectors.toList());
        Map<String, File> textBoxesFile = new HashMap<>();
        for (SourceRegion textRegion : textRegions) {
            String fileName = UUID.randomUUID() + ".png";
            String text = sources.get(textRegion.getSourceName()).getText();
            String color = textRegion.getColor().isEmpty() ? "black" : textRegion.getColor();
            String font = textRegion.getFont().isEmpty() ? "Arial" : textRegion.getFont();
            String orientation = textRegion.getOrientation().isEmpty() ? "center" : textRegion.getOrientation();
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c",
                    String.format("magick convert -background transparent -fill %s -font %s -gravity %s -size %dx%d caption:\"%s\" %s",
                            color,font,orientation,textRegion.getWidth(), textRegion.getHeight(), text, fileName));
            processBuilder.redirectErrorStream(true);
            try {
                Process p = processBuilder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    System.out.println(line);
                }
                textBoxesFile.put(textRegion.getX() + ":" + textRegion.getY(), new File(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        sourceRegions.stream().filter(s -> s.getPriority() < 0).forEach(s -> {
            if (s.isText()) {

                File file = textBoxesFile.get(s.getX() + ":" + s.getY());
                if (file == null) {
                    System.out.println("NULL");
                } else {
                    stringBuilder.append(" -draw \"image over ").append(s.getX()).append(",").append(s.getY()).append(" ")
                    .append(s.getWidth()).append(",").append(s.getHeight()).append(" '").append(file.getPath()).append("'\"");
                }
            } else {
                Source source = sources.get(s.getSourceName());

                stringBuilder.append(" -draw \"image over ").append(s.getX()).append(",").append(s.getY()).append(" ")
                        .append(s.getWidth()).append(",").append(s.getHeight()).append(" '").append(source.getImagePath()).append("'\"");
            }
        });
        stringBuilder.append(" -draw \"image over ").append(0).append(",").append(0).append(" ")
                .append(width).append(",").append(height).append(" '").append(templateImage).append("'\"");
        sourceRegions.stream().filter(s -> s.getPriority() >= 0).forEach(s -> {
            if (s.isText()) {
                File file = textBoxesFile.get(s.getX() + ":" + s.getY());
                if (file == null) {
                    System.out.println("NULL");
                } else {
                    stringBuilder.append(" -draw \"image over ").append(s.getX()).append(",").append(s.getY()).append(" ")
                            .append(s.getWidth()).append(",").append(s.getHeight()).append(" '").append(file.getPath()).append("'\"");
                }
            } else {
                Source source = sources.get(s.getSourceName());

                stringBuilder.append(" -draw \"image over ").append(s.getX()).append(",").append(s.getY()).append(" ")
                        .append(s.getWidth()).append(",").append(s.getHeight()).append(" '").append(source.getImagePath()).append("'\"");
            }
        });
        String string = stringBuilder.toString();
        //System.out.println(string);
        String generatedImage = UUID.randomUUID() + ".png";
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "magick convert -size "+ width + "x" + height + " xc:white -font Arial" + string + " " + generatedImage);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
            }
            File generatedFile = new File(generatedImage);
            BufferedImage image = ImageIO.read(generatedFile);
            message.delete().queue();
            event.getChannel().sendFile(Objects.requireNonNull(ImageUtils.bufferedImageToBytes(image)), "generated.png").queue();
            generatedFile.delete();
            textBoxesFile.forEach((s, f) -> f.delete());
            MilkshakeSimulator.processing = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
