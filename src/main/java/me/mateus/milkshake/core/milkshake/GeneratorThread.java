package me.mateus.milkshake.core.milkshake;

import me.mateus.milkshake.MilkshakeSimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class GeneratorThread extends Thread {

    private final Queue<Milkshake> milkshakes;
    private final Random random = new Random();
    private final Map<Integer, String> originalPoints = new HashMap<>();

    public GeneratorThread() {
        this.milkshakes = new LinkedList<>();
        originalPoints.put(0,"0,0");
        originalPoints.put(1,"0,%height%");
        originalPoints.put(2,"%width%,0");
        originalPoints.put(3,"%width%,%height%");
    }

    public void addToQueue(Milkshake milkshake) {
        if (milkshakes.size() >= 3) {
            return;
        }
        milkshakes.add(milkshake);
    }

    public Milkshake generateForceful(Template template) {
        Milkshake milkshake = milkshakes.peek();
        if (milkshake != null && milkshake.getTemplate().getName().equals(template.getName())) {
            return milkshakes.poll();
        }
        return createMilkshake(template);
    }

    public Milkshake getMilkshake() {
        Milkshake milkshake = milkshakes.poll();
        if (milkshake == null ) {
            MilkshakeManager manager = MilkshakeManager.getInstance();
            int templateCount = manager.getTemplates().size();
            if (templateCount > 0) {
                Template template = manager.getTemplates().get(random.nextInt(manager.getTemplates().size()));
                return createMilkshake(template);
            }
        }
        return milkshake;
    }

    @Override
    public void run() {
        while (MilkshakeSimulator.running) {
            if (milkshakes.size() < 3) {
                MilkshakeManager manager = MilkshakeManager.getInstance();
                int templateCount = manager.getTemplates().size();
                if (templateCount > 0) {
                    Template template = manager.getTemplates().get(random.nextInt(templateCount));
                    addToQueue(createMilkshake(template));
                }
            }
        }
    }

    private Milkshake createMilkshake(Template template) {
        MilkshakeManager manager = MilkshakeManager.getInstance();
        int width = template.getWidth();
        int height = template.getHeight();

        Map<Integer, Source> sources = new HashMap<>();
        List<SourceRegion> sourceRegions = template.getSourceRegions();
        for (SourceRegion sourceRegion : sourceRegions) {
            int sourceIdx = sourceRegion.getSourceName();
            if (sources.containsKey(sourceIdx)) {
                continue;
            }
            Source source = manager.getSources().get(random.nextInt(manager.getSources().size()));
            if (!sourceRegion.isText()) {
                while (source.getImagePath().isEmpty()) {
                    source = manager.getSources().get(random.nextInt(manager.getSources().size()));
                }
            }
            sources.put(sourceIdx, source);
        }

        List<SourceRegion> perspectiveRegions = sourceRegions.stream().filter(this::isPoints).collect(Collectors.toList());
        List<SourceRegion> textRegions = sourceRegions.stream().filter(r -> r.isText() && !isPoints(r)).collect(Collectors.toList());
        Map<String, File> textBoxesFile = new HashMap<>();
        Map<String, File> perspectivesFile = new HashMap<>();
        for (SourceRegion textRegion : textRegions) {
            try {
                File tempFile = File.createTempFile("text",".png");
                tempFile.deleteOnExit();
                String text = sources.get(textRegion.getSourceName()).getText();
                String color = textRegion.getColor().isEmpty() ? "black" : textRegion.getColor();
                String font = textRegion.getFont().isEmpty() ? "Arial" : textRegion.getFont();
                String orientation = textRegion.getOrientation().isEmpty() ? "center" : textRegion.getOrientation();
                String strokeParam = textRegion.getStrokeColor().isEmpty() ? "" : "-stroke " + textRegion.getStrokeColor() + " -strokewidth " + textRegion.getStrokeWidth();
                ProcessBuilder processBuilder = new ProcessBuilder("magick convert " +
                        String.format("-background transparent -fill %s -font %s -gravity %s %s -size %dx%d caption:\"%s\" %s",
                                color,font,orientation,strokeParam,textRegion.getWidth(), textRegion.getHeight(), text, tempFile.getPath()));
                processBuilder.redirectErrorStream(true);
                Process p = processBuilder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    System.out.println(line);
                }
                textBoxesFile.put(textRegion.getX() + ":" + textRegion.getY(), tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (SourceRegion perspectiveRegion : perspectiveRegions) {
            try {
                File tempFile = File.createTempFile("pers", ".png");
                tempFile.deleteOnExit();

                StringBuilder stringBuilder = new StringBuilder();
                if (perspectiveRegion.isText()) {
                    String text = sources.get(perspectiveRegion.getSourceName()).getText();
                    String color = perspectiveRegion.getColor().isEmpty() ? "black" : perspectiveRegion.getColor();
                    String font = perspectiveRegion.getFont().isEmpty() ? "Arial" : perspectiveRegion.getFont();
                    String orientation = perspectiveRegion.getOrientation().isEmpty() ? "center" : perspectiveRegion.getOrientation();
                    String strokeParam = perspectiveRegion.getStrokeColor().isEmpty() ? "" : "-stroke " + perspectiveRegion.getStrokeColor() + " -strokewidth " + perspectiveRegion.getStrokeWidth();
                    stringBuilder.append("-background transparent -fill ").append(color).append(" -font ").append(font).append(" -gravity ").append(orientation).append(" ").append(strokeParam)
                            .append(" -size ").append(width).append('x').append(height).append(" caption:\"").append(text).append("\" ");
                } else {
                    String imagePath = sources.get(perspectiveRegion.getSourceName()).getImagePath();
                    stringBuilder.append(imagePath).append(" -resize ").append(width).append("x").append(height).append("! -matte -virtual-pixel transparent ");
                }

                stringBuilder.append("-distort Perspective \"");
                for (int i = 0; i < 4; i++) {
                    Point point = perspectiveRegion.getPoints()[i];
                    String original = originalPoints.get(i).replace("%width%", String.valueOf(width))
                            .replace("%height%", String.valueOf(height));
                    stringBuilder.append(original).append(",").append(point.getX()).append(",").append(point.getY());
                    if (i == 3) {
                        stringBuilder.append("\" ");
                    } else {
                        stringBuilder.append(" ");
                    }
                }
                stringBuilder.append(tempFile.getPath());
                ProcessBuilder processBuilder = new ProcessBuilder("magick convert " + stringBuilder);
                processBuilder.redirectErrorStream(true);
                Process p = processBuilder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    System.out.println(line);
                }
                int x = perspectiveRegion.getPoints()[0].getX();
                int y = perspectiveRegion.getPoints()[3].getY();
                perspectivesFile.put(x + "%" + y, tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        sourceRegions.stream().filter(s -> s.getPriority() < 0).forEach(s -> {
            if (isPoints(s)) {
                int x = s.getPoints()[0].getX();
                int y = s.getPoints()[3].getY();
                File file = perspectivesFile.get(x + "%" + y);
                stringBuilder.append(" -draw \"image over ").append(0).append(",").append(0).append(" ")
                        .append(template.getWidth()).append(",").append(template.getHeight()).append(" '").append(file.getPath()).append("'\"");
            } else if (s.isText()) {

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
                .append(template.getWidth()).append(",").append(template.getHeight()).append(" '").append(template.getImagePath()).append("'\"");
        sourceRegions.stream().filter(s -> s.getPriority() >= 0).forEach(s -> {
            if (isPoints(s)) {
                int x = s.getPoints()[0].getX();
                int y = s.getPoints()[3].getY();
                File file = perspectivesFile.get(x + "%" + y);
                stringBuilder.append(" -draw \"image over ").append(0).append(",").append(0).append(" ")
                        .append(template.getWidth()).append(",").append(template.getHeight()).append(" '").append(file.getPath()).append("'\"");
            } else if (s.isText()) {
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

        try {
            File generatedImage = File.createTempFile("milkshake", ".png");
            generatedImage.deleteOnExit();
            String command = "-size "+ width + "x" + height + " xc:white -font Arial" + string + " " + generatedImage.getPath();
            ProcessBuilder processBuilder = new ProcessBuilder("magick convert " + command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
            }
            Milkshake milkshake = new Milkshake(template, sources);
            milkshake.setImagePath(generatedImage.getPath());
            return milkshake;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isPoints(SourceRegion sourceRegion) {
        Point[] points = sourceRegion.getPoints();
        for (int i = 0; i < 4; i++) {
            if (points[i] == null) {
                return false;
            }
        }
        return true;
    }
}
