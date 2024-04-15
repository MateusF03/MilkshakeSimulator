package me.mateus.milkshake.commands;

import com.google.gson.JsonObject;
import me.mateus.milkshake.core.command.ArgumentType;
import me.mateus.milkshake.core.command.CommandEnvironment;
import me.mateus.milkshake.core.command.CommandManager;
import me.mateus.milkshake.core.command.RegisteredCommand;
import me.mateus.milkshake.core.command.interfaces.Argument;
import me.mateus.milkshake.core.command.interfaces.Command;
import me.mateus.milkshake.core.command.translator.ArgumentTranslator;
import me.mateus.milkshake.core.listeners.DeleteSourceListener;
import me.mateus.milkshake.core.listeners.RenameSourceListener;
import me.mateus.milkshake.core.milkshake.MilkshakeManager;
import me.mateus.milkshake.core.milkshake.Source;
import me.mateus.milkshake.core.milkshake.SourceRegion;
import me.mateus.milkshake.core.milkshake.Template;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CreateCommands {

    @Command(name = "create", description = "Cria um template", receivesImage = true, args = {
            @Argument(name = "name", type = ArgumentType.STRING, obligatory = true),
            @Argument(name = "regions", type = ArgumentType.REGIONS, obligatory = true)
    })
    public void create(CommandEnvironment env, ArgumentTranslator argumentTranslator) {
        String name = argumentTranslator.getAsString("name");
        if (name.length() > 64) {
            env.reply("**O limite de caracteres no nome é 64**");
            return;
        }
        List<SourceRegion> regions = argumentTranslator.getAsRegions("regions");
        MilkshakeManager manager = MilkshakeManager.getInstance();
        Template t = manager.getTemplateByName(name);

        String[] installedFonts = getInstalledFonts();
        for (SourceRegion region : regions) {
            String regionFont = region.getFont();
            if (regionFont.equals(""))
                continue;
            boolean isAvaiable = false;
            for (String font : installedFonts) {
                if (font.equals(regionFont)) {
                    isAvaiable = true;
                    break;
                }
            }
            if (!isAvaiable) {
                env.reply("**Fonte **`" + regionFont + "`** não está disponível**");
                return;
            }
        }

        if (t != null) {
            env.reply("**Já existe um template com este nome**");
            return;
        }

        BufferedImage image = env.getAttatchedImage();
        if (image == null) {
            env.reply("**Não foi possível reconhecer a imagem**");
            return;
        }
        String fileName = UUID.randomUUID() + ".png";
        File file = new File("images", fileName.replace("-",""));
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            env.reply("**Erro ao salvar imagem**");
            e.printStackTrace();
            return;
        }
        Template template = new Template(name, regions, file.getPath());
        JsonObject jsonObject = template.toJsonObject(MilkshakeManager.GSON);

        File templateFile = new File("templates", name.replaceAll("[^a-zA-Z0-9.\\-]", "_") + ".milkshake");
        try {
            Files.writeString(templateFile.toPath(), MilkshakeManager.GSON.toJson(jsonObject));
            template.setOriginalFilePath(templateFile.getPath());
            template.setWidth(image.getWidth());
            template.setHeight(image.getHeight());
            env.reply("**Template criado!**");
            manager.addTemplate(template);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Command(name = "add", description = "Adiciona um texto", args = {
            @Argument(name = "text", type = ArgumentType.STRING, obligatory = true)
    })
    public void add(CommandEnvironment env, ArgumentTranslator translator) {

        MilkshakeManager manager = MilkshakeManager.getInstance();
        try {
            String text = translator.getAsString("text");
            if (text.length() > 64) {
                env.reply("**O limite de caracteres é 64**");
                return;
            }
            manager.addText(text);
            env.reply("**O texto:** `" + text + "` ** foi adicionado às sources**");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Command(name = "eliminar", description = "Deleta um template", vipOnly = true, args = {
            @Argument(name = "template", type = ArgumentType.STRING, obligatory = true)
    })
    public void delete(CommandEnvironment env, ArgumentTranslator translator) {
        String name = translator.getAsString("template");
        MilkshakeManager manager = MilkshakeManager.getInstance();
        Template t = manager.getTemplateByName(name);

        if (t == null) {
            env.reply("**Não existe este template**");
            return;
        }
        if (!t.delete()) {
            env.reply("**Erro ao deletar template!**");
        } else {
            manager.getTemplates().remove(t);
            env.reply("**Template deletado!**");
        }
    }

    @Command(name = "source", description = "Cria uma source", receivesImage = true, args = {
            @Argument(name = "name", type = ArgumentType.STRING, obligatory = true)
    })
    public void source(CommandEnvironment env, ArgumentTranslator translator) {
        String name = translator.getAsString("name");
        if (name.length() > 64) {
            env.reply("**O limite de caracteres no nome é 64**");
            return;
        }

        BufferedImage image = env.getAttatchedImage();
        if (image == null) {
            env.reply("**Não foi possível reconhecer a imagem**");
            return;
        }

        File file = new File("sources", name.replaceAll("[^a-zA-Z0-9.\\-]", "_") + ".sundae");
        File imageFile = new File("images", UUID.randomUUID().toString().replace("-","") + ".png");
        try {
            ImageIO.write(image, "png",imageFile);
            Source source = new Source(name, imageFile.getPath(), file.getPath(), "");
            String json = MilkshakeManager.GSON.toJson(source, Source.class);
            Files.writeString(file.toPath(), json);
            MilkshakeManager.getInstance().addSource(source);

            env.reply("**Source adicionada!**");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Command(name = "renomear", description = "Renomeia uma source", vipOnly = true,args = {
            @Argument(name = "oldName", type = ArgumentType.STRING, obligatory = true),
            @Argument(name = "newName", type = ArgumentType.STRING, obligatory = true)
    })
    public void rename(CommandEnvironment env, ArgumentTranslator translator) {
        String oldName = translator.getAsString("oldName");
        String newName = translator.getAsString("newName");
        List<Source> sources = MilkshakeManager.getInstance().getSourcesByName(oldName);

        if (sources.isEmpty()) {
            env.reply("**Não existe nenhuma source com este nome**");
        } else if (sources.size() == 1) {
            Source source = sources.get(0);
            try {
                source.rename(newName);
                env.reply("**Source renomeada para:** `" + newName + "`");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(0xb28dff);
            embedBuilder.setTitle("Qual source você deseja renomear?");
            StringBuilder description = new StringBuilder();
            description.append("`");
            int idx = 0;
            for (Source ignored : sources) {
                idx++;
                description.append("- Source Nº").append(idx).append("\n");
            }
            description.append("`\n**Digite:** `Nº da source | novo nome` **para trocar de nome e** `Nº da source` **apenas para ver a imagem da source**");
            embedBuilder.setDescription(description.toString());
            embedBuilder.setFooter("Digite &&cancel para cancelar");
            env.embed(embedBuilder.build());
            env.getJDAInstance().addEventListener(
                new RenameSourceListener(
                    env.getCommandCaller().getIdLong(),
                    env.getCallingChannel().getIdLong(),
                    sources
                )
            );
        }
    }

    @Command(name = "guide", description = "Envia um guia de como criar template")
    public void guide(CommandEnvironment env, ArgumentTranslator translator) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Guia de como criar templates:");
        embedBuilder.setColor(0xb28dff);
        String description = """
                **Parâmetros:**
                `-x/p1: A coordenada X da imagem ou o ponto 1 (x,y)
                -y/p2: A coordenada Y da imagem ou o ponto 2 (x,y)
                -width/p3: A largura da imagem ou o ponto 3 (x,y)
                -height/p4: A altura da imagem ou o ponto 4 (x,y)
                -nº da source: O número da source, todas as sources com o mesmo número são iguais
                -texto: true/false, se a região é um texto ou não
                -prioridade: A prioridade da imagem, prioridades menores de 0 serão desenhadas antes do template, ficando no background
                -cor: A cor do texto em hexadecimal (#rrggbb)
                -orientação: A orientação do texto
                -fonte: A fonte do texto
                -cor da borda: A cor da borda do texto
                -tamanho da borda: O tamanho da borda do texto
                `""";
        embedBuilder.setDescription(description);
        String links = """
                **Lista de cores:** <https://imagemagick.org/script/color.php#color_names>
                **Lista de orientações:** <https://www.imagemagick.org/script/command-line-options.php#gravity>
                **Lista de fontes:** use `magick identify -list font` para pegar todas fontes""";
        env.embed(embedBuilder.build());
        env.reply(links); // FIXME: this won't work on current implementation
    }

    @Command(name = "help", description = "O comando para listar todos os outros comandos do bot")
    public void help(CommandEnvironment env, ArgumentTranslator translator)  {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Comandos:");
        embedBuilder.setColor(0xb28dff);
        StringBuilder description = new StringBuilder();
        description.append("`");
        CommandManager manager = CommandManager.getInstance();
        for (RegisteredCommand command : manager.getCommands()) {
            description.append(manager.getPrefix()).append(command.getName()).append(": ").append(command.getDescription())
                    .append('\n');
        }
        description.append('`');
        embedBuilder.setDescription(description.toString());
        env.embed(embedBuilder.build());
    }

    @Command(name = "eliminarSource", description = "Elimina uma source",vipOnly = true, args = {
            @Argument(name = "sourceName", type = ArgumentType.STRING, obligatory = true)
    })
    public void removeSource(CommandEnvironment env, ArgumentTranslator translator) {
        String sourceName = translator.getAsString("sourceName");
        List<Source> sources = MilkshakeManager.getInstance().getSourcesByName(sourceName);

        if (sources.isEmpty()) {
            env.reply("**Não existe nenhuma source com este nome**");
        } else if (sources.size() == 1) {
            Source source = sources.get(0);
            if (source.errorWhileDeleting()) {
                env.reply("**Erro ao deletar source**");
                return;
            }
            MilkshakeManager.getInstance().getSources().remove(source);
            env.reply("**Source deletada!**");

        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(0xb28dff);
            embedBuilder.setTitle("Qual source você deseja deletar?");
            StringBuilder description = new StringBuilder();
            description.append("`");
            int idx = 0;
            for (Source ignored : sources) {
                idx++;
                description.append("- Source Nº").append(idx).append("\n");
            }
            description.append("`\n**Digite:** `Nº da source` **para deleter e** `&&ver Nº da source` **apenas para ver a imagem da source**");
            embedBuilder.setDescription(description.toString());
            embedBuilder.setFooter("Digite &&cancel para cancelar");
            env.embed(embedBuilder.build());
            env.getJDAInstance().addEventListener(
                new DeleteSourceListener(
                    env.getCommandCaller().getIdLong(),
                    env.getCallingChannel().getIdLong(),
                    sources
                )
            );
        }
    }

    @Command(name = "fontes", description = "Lista as fontes instaladas no servidor", args = {
        @Argument(name = "page", type = ArgumentType.INTEGER, obligatory = false)
    })
    public void listInstalledFonts(CommandEnvironment env, ArgumentTranslator translator) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Fontes disponíveis:");
        embedBuilder.setColor(0xb28dff);
        StringBuilder description = new StringBuilder();
        description.append("`");

        String[] installedFonts = getInstalledFonts();
        int page = Math.max(translator.getAsInteger("page"), 1);
        int lastPage = installedFonts.length / 20;
        page = Math.min(page, lastPage);
        int pageOverflow = (page == lastPage)? installedFonts.length % 20 : 0;
        for (int i = 20 * (page - 1); i < 20 * page + pageOverflow; i++)
            description.append(installedFonts[i]).append("\n");

        description.append("`");
        embedBuilder.setDescription(description.toString());
        embedBuilder.setFooter("Página " + page + " de " + lastPage);
        env.embed(embedBuilder.build());
    }

    private String[] getInstalledFonts() {
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", "magick -list font | grep Font:");
        try {
            Process p = processBuilder.start();
            BufferedReader reader = p.inputReader();
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            List<String> fontList = reader.lines().toList();
            reader.close();

            String[] result = new String[fontList.size()];
            for (int i = 0; i < result.length; i++)
                result[i] = fontList.get(i).substring(8).toLowerCase();
            
            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private BufferedImage inputStreamToImage(InputStream inputStream) {
        try {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    private InputStream getInputStreamFromMessage(Message message) {
        List<Message.Attachment> attachments = message.getAttachments();
        if (!attachments.isEmpty()) {
            try {
                return attachments.get(0).getProxy().download().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
