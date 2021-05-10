package me.mateus.milkshake;

import com.google.gson.JsonObject;
import me.mateus.milkshake.core.MilkshakeManager;
import me.mateus.milkshake.core.Source;
import me.mateus.milkshake.core.SourceRegion;
import me.mateus.milkshake.core.Template;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MilkshakeListener extends ListenerAdapter {

    private final Pattern URL_PATTERN = Pattern.compile("((([A-Za-z]{3,9}:(?://)?)(?:[-;:&=+$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=+$,\\w]+@)[A-Za-z0-9.-]+)((?:/[+~%/.\\w-_]*)?\\??[-+=&;%@.\\w_]*#?[\\w]*)?)");
    private final Random RANDOM = new Random();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String content = event.getMessage().getContentRaw();
        TextChannel channel = event.getChannel();
        if (content.startsWith("m!generate")) {
            if (MilkshakeSimulator.processing) {
                return;
            }
            MilkshakeManager manager = MilkshakeManager.getInstance();
            List<Template> templates = manager.getTemplates();

            if (templates.isEmpty()) {
                channel.sendMessage("**O Bot ainda não tem nenhum template!**").queue();
            } else {
                Template template = templates.get(RANDOM.nextInt(templates.size()));

                List<SourceRegion> regions = template.getSourceRegions();


                channel.sendMessage("**Processando..**").queue((m) -> createMilkshake(m, regions, event, template));

            }
        } else if (content.startsWith("m!create")) {
            String[] args = content.replace("m!create", "").split("[|]");
            String INCORRECT_COMMAND = "**Comando incorreto**:\n`m!create [nome do template] | [x] [y] [largura] [altura] [texto] [prioridade] [nº da source] [cor] [orientação] [fonte] | [...]`\n" +
                    "**Lista de cores:** <https://imagemagick.org/script/color.php#color_names>\n**Lista de orientações:** <https://www.imagemagick.org/script/command-line-options.php#gravity>\n" +
                    "**Lista de fontes:** use `magick identify -list font` para pegar todas fontes";
            if (args.length < 2) {
                channel.sendMessage(INCORRECT_COMMAND).queue();
            } else {
                String name = args[0];
                InputStream is = getMostRecentInputStream(event);
                if (is == null) {
                    channel.sendMessage("**Não foi possível encontrar uma imagem**").queue();
                } else {
                    BufferedImage image = inputStreamToImage(is);
                    Template template;
                    List<SourceRegion> sourceRegions = new ArrayList<>();
                    if (image == null) {
                        channel.sendMessage("**Não foi possível encontrar uma imagem**").queue();
                        return;
                    }
                    int sourceIdx = 1;
                    for (int i = 1; i < args.length; i++) {
                        String arg = args[i];
                        String[] values = arg.trim().split(" ");
                        if (values.length < 4) {
                            channel.sendMessage(INCORRECT_COMMAND).queue();
                            channel.sendMessage(String.valueOf(values.length)).queue();
                        } else {
                            try {
                                int x = Integer.parseInt(values[0]);
                                int y = Integer.parseInt(values[1]);
                                int width = Integer.parseInt(values[2]);
                                int height = Integer.parseInt(values[3]);
                                int sourceName;
                                int priority = 0;
                                boolean text = false;
                                String color = "";
                                String orientation = "";
                                String font = "";
                                String strokeColor = "";
                                int strokeWidth = 0;
                                if (values.length >= 5) {
                                    text = Boolean.parseBoolean(values[4]);
                                }
                                if (values.length >= 6) {
                                    priority = Integer.parseInt(values[5]);
                                }
                                if (values.length >= 7) {
                                    sourceName = Integer.parseInt(values[6]);
                                } else {
                                    sourceName = sourceIdx;
                                    sourceIdx++;
                                }
                                if (values.length >= 8) {
                                    color = values[7];
                                }
                                if (values.length >= 9) {
                                    orientation = values[8];
                                }
                                if (values.length >= 10) {
                                    font = values[9];
                                }
                                if (values.length >= 11) {
                                    strokeColor = values[10];
                                }
                                if (values.length >= 12) {
                                    strokeWidth = Integer.parseInt(values[11]);
                                }
                                sourceRegions.add(new SourceRegion(x, y, width, height, priority, text, sourceName, color, orientation, font, strokeColor, strokeWidth));
                            } catch (NumberFormatException e) {
                                channel.sendMessage(INCORRECT_COMMAND).queue();
                                return;
                            }

                        }
                    }

                    String fileName = UUID.randomUUID() + ".png";
                    File file = new File("images", fileName.replace("-",""));
                    try {
                        ImageIO.write(image, "png", file);
                    } catch (IOException e) {
                        channel.sendMessage("**Erro ao salvar imagem**").queue();
                        e.printStackTrace();
                    }
                    name = name.trim();
                    template = new Template(name, image, sourceRegions, file);
                    JsonObject templateObject = template.toJsonObject(file.getPath());

                    File templateFile = new File("templates", name + ".milkshake");
                    try {
                        Files.write(templateFile.toPath(), MilkshakeSimulator.GSON.toJson(templateObject).getBytes(StandardCharsets.UTF_8));
                        template.setOriginalFile(templateFile);
                        channel.sendMessage("**Template criado!**").queue();
                        MilkshakeManager.getInstance().getTemplates().add(template);
                    } catch (IOException e) {
                        channel.sendMessage("**Erro ao criar template**").queue();
                        e.printStackTrace();
                    }
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (content.startsWith("m!add")) {
            String[] args = content.split("\\s+");
            if (args.length > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    stringBuilder.append(args[i]).append(" ");
                }
                MilkshakeManager manager = MilkshakeManager.getInstance();
                try {
                    String text = stringBuilder.toString().trim();
                    if (text.length() > 32) {
                        channel.sendMessage("**O limite de caracteres é 32**").queue();
                        return;
                    }
                    manager.addText(text);
                    channel.sendMessage("**O texto:** `" + text + "` ** foi adicionado às sources**").queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (content.toLowerCase().startsWith("m!source")) {
            String[] args = content.split(" ");
            if (args.length == 1) {
                channel.sendMessage("**Comando invalido:**\n`m!source [nome da source]`").queue();
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                stringBuilder.append(args[i]).append(" ");
            }
            String sourceName = stringBuilder.toString().trim();
            InputStream is = getMostRecentInputStream(event);
            if (is != null) {
                BufferedImage image = inputStreamToImage(is);
                if (image == null) {
                    channel.sendMessage("**Não foi possível encontrar uma imagem**").queue();
                } else {
                    File sourceFile = new File("sources",sourceName + ".sundae");
                    int idx = 0;
                    while (sourceFile.exists()) {
                        idx++;
                        sourceFile = new File("sources", sourceName + "-" + idx + ".sundae");
                    }
                    File file = new File("images", UUID.randomUUID().toString().replace("-","") + ".png");
                    try {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("name", sourceName);
                        jsonObject.addProperty("image-path", file.getPath());
                        Files.write(sourceFile.toPath(), jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                        ImageIO.write(image, "png", file);
                        channel.sendMessage("**A imagem mais recente foi adicionada como source!**").queue();
                        MilkshakeManager.getInstance().getSources().add(new Source(sourceName, file.getPath(), sourceFile.getPath(), ""));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (content.toLowerCase().startsWith("m!eliminar")) {
            String[] args = content.split(" ");
            if (!MilkshakeSimulator.REPUTABLE_PEOPLE.contains(event.getAuthor().getIdLong())) {
                channel.sendMessage("**Você não tem permissão de executar este comando**").queue();
                return;
            }
            if (args.length == 1) {
                channel.sendMessage("**Comando incorreto:**\n`m!eliminar (nome do template)`**").queue();
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                stringBuilder.append(args[i]).append(" ");
            }
            String templateName = stringBuilder.toString().trim();
            Template template = MilkshakeManager.getInstance().getTemplateByName(templateName);
            if (template == null) {
                channel.sendMessage("**Não existe este template**").queue();
                return;
            }
            MilkshakeManager.getInstance().getTemplates().remove(template);
            File originalFile = template.getOriginalFile();
            if (originalFile != null)
                originalFile.delete();
            template.getImageFile().delete();
            channel.sendMessage("**Template removido!**").queue();
        } else if (content.toLowerCase().startsWith("m!forcegenerate")) {
            String[] args = content.split(" ");
            if (!MilkshakeSimulator.REPUTABLE_PEOPLE.contains(event.getAuthor().getIdLong())) {
                channel.sendMessage("**Você não tem permissão de executar este comando**").queue();
                return;
            }
            if (args.length == 1) {
                channel.sendMessage("**Comando incorreto:**\n`m!forcegenerate (nome do template)`**").queue();
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                stringBuilder.append(args[i]).append(" ");
            }
            String templateName = stringBuilder.toString().trim();
            Template template = MilkshakeManager.getInstance().getTemplateByName(templateName);
            if (template == null) {
                channel.sendMessage("**Não existe este template**").queue();
                return;
            }
            List<SourceRegion> regions = template.getSourceRegions();

            channel.sendMessage("**Processando..**").queue((m) -> createMilkshake(m, regions, event, template, true));
        } else if (content.startsWith("m!renomear")) {
            String[] args = content.split("[|]");
            if (!MilkshakeSimulator.REPUTABLE_PEOPLE.contains(event.getAuthor().getIdLong())) {
                channel.sendMessage("**Você não tem permissão de executar este comando**").queue();
                return;
            }
            if (args.length < 2) {
                channel.sendMessage("**Comando incorreto:**\n`m!renomear (nome original do template) | (novo nome)`**").queue();
                return;
            }
            String originalName = args[0].replace("m!renomear", "").trim();
            String newName = args[1].trim();
            Source source = MilkshakeManager.getInstance().getSourceByName(originalName);
            if (source == null) {
                channel.sendMessage("**Essa source não existe**").queue();
                return;
            }
            try {
                source.rename(newName);
                channel.sendMessage("**Source renomeada para:**`"  + newName +"`").queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (content.startsWith("m!matar")) {
            if (!MilkshakeSimulator.REPUTABLE_PEOPLE.contains(event.getAuthor().getIdLong())) {
                channel.sendMessage("**Você não tem permissão de executar este comando**").queue();
                return;
            }
            event.getJDA().shutdown();
            System.exit(0);
        } else if (content.startsWith("m!reiniciar")) {
            if (!MilkshakeSimulator.REPUTABLE_PEOPLE.contains(event.getAuthor().getIdLong())) {
                channel.sendMessage("**Você não tem permissão de executar este comando**").queue();
                return;
            }
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar;
            try {
                currentJar = new File(MilkshakeSimulator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                if(!currentJar.getName().endsWith(".jar"))
                    return;

                final ArrayList<String> command = new ArrayList<>();
                command.add(javaBin);
                command.add("-jar");
                command.add(currentJar.getPath());

                final ProcessBuilder builder = new ProcessBuilder(command);
                builder.start();
                event.getJDA().shutdown();
                System.exit(0);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }

        }
    }

    private BufferedImage inputStreamToImage(InputStream inputStream) {
        try {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    private InputStream getMostRecentInputStream(GuildMessageReceivedEvent event) {
        InputStream inputStream = getInputStreamFromMessage(event.getMessage());
        if (inputStream != null) {
            return inputStream;
        }
        final InputStream[] stream = {null};
        event.getChannel().getHistory().retrievePast(50).queue(h -> {
            for (Message m : h) {
                InputStream is = getInputStreamFromMessage(m);
                if (is != null) {
                    stream[0] = is;
                    break;
                }
            }
        });
        return stream[0];
    }

    private InputStream getInputStreamFromMessage(Message message) {
        List<Message.Attachment> attachments = message.getAttachments();
        if (!attachments.isEmpty()) {
            try {
                return attachments.get(0).retrieveInputStream().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        String content = message.getContentRaw();
        Matcher matcher = URL_PATTERN.matcher(content);
        while (matcher.find()) {
            String url = matcher.group(1);
            try {
                return new URL(url).openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void createMilkshake(Message message, List<SourceRegion> regions, GuildMessageReceivedEvent event, Template template) {
        createMilkshake(message, regions, event, template,false);
    }

    private void createMilkshake(Message message, List<SourceRegion> regions, GuildMessageReceivedEvent event, Template template, boolean forceful) {
        MilkshakeManager manager = MilkshakeManager.getInstance();
        Map<Integer, Source> sources = new HashMap<>();
        for (SourceRegion sourceRegion : regions) {
            int sourceIdx = sourceRegion.getSourceName();
            if (sources.containsKey(sourceIdx)) {
                System.out.println("CONTAIN");
                continue;
            }
            Source source = manager.getSources().get(RANDOM.nextInt(manager.getSources().size()));
            if (!sourceRegion.isText()) {
                while (source.getImagePath().isEmpty()) {
                    source = manager.getSources().get(RANDOM.nextInt(manager.getSources().size()));
                }
            }
            sources.put(sourceIdx, source);
        }

        template.createMilkshake(sources, event, message, forceful);
    }
}
