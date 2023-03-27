package me.mateus.milkshake.commands;

import me.mateus.milkshake.core.command.ArgumentType;
import me.mateus.milkshake.core.command.interfaces.Argument;
import me.mateus.milkshake.core.command.interfaces.Command;
import me.mateus.milkshake.core.command.translator.ArgumentTranslator;
import me.mateus.milkshake.core.milkshake.GeneratorThread;
import me.mateus.milkshake.core.milkshake.Milkshake;
import me.mateus.milkshake.core.milkshake.MilkshakeManager;
import me.mateus.milkshake.core.milkshake.Template;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class GenerateCommand {

    private final GeneratorThread thread = new GeneratorThread();

    public GenerateCommand() {
        thread.start();
    }

    @Command(name = "generate", description = "Gera um milkshake")
    public void generate(MessageReceivedEvent event, ArgumentTranslator argumentTranslator) {
        Milkshake milkshake = thread.getMilkshake();
        event.getChannel().sendMessage("**Processando...**").queue(m -> {
            try {
                BufferedImage image = ImageIO.read(Paths.get(milkshake.getImagePath()).toFile());
                byte[] array = bufferedImageToBytes(image);
                if (array == null || array.length >= 8388603) {
                    m.editMessage("**Não foi possível enviar o milkshake**").queue();
                    return;
                }
                m.delete().queue();
                event.getChannel().sendFiles(FileUpload.fromData(array, "generated.png")).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Command(name = "forceGenerate", description = "Gera um milkshake forçadamente", vipOnly = true, args = {
            @Argument(name = "templateName", type = ArgumentType.STRING, obligatory = true)
    })
    public void forceGenerate(MessageReceivedEvent event, ArgumentTranslator argumentTranslator) {
        String templateName = argumentTranslator.getAsString("templateName");
        MilkshakeManager manager = MilkshakeManager.getInstance();
        Template template = manager.getTemplateByName(templateName);
        if (template == null) {
            event.getChannel().sendMessage("**Este template não existe**").queue();
            return;
        }
        Milkshake milkshake = thread.generateForceful(template);
        event.getChannel().sendMessage("**Processando...**").queue(m -> {
            try {
                BufferedImage image = ImageIO.read(Paths.get(milkshake.getImagePath()).toFile());
                byte[] array = bufferedImageToBytes(image);
                if (array == null || array.length >= 8388603) {
                    m.editMessage("**Não foi possível enviar o milkshake**").queue();
                    return;
                }
                m.delete().queue();
                event.getChannel().sendFiles(FileUpload.fromData(array, "generated.png")).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private byte[] bufferedImageToBytes(BufferedImage image) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            byte[] array = os.toByteArray();
            os.close();
            return array;
        } catch (IOException e) {
            return null;
        }
    }
}
