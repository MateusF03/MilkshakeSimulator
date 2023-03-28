package me.mateus.milkshake.commands;

import me.mateus.milkshake.MilkshakeSimulator;
import me.mateus.milkshake.core.command.ArgumentType;
import me.mateus.milkshake.core.command.CommandManager;
import me.mateus.milkshake.core.command.interfaces.Argument;
import me.mateus.milkshake.core.command.interfaces.Command;
import me.mateus.milkshake.core.command.translator.ArgumentTranslator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class AdminCommands {
    @Command(name = "matar", description = "Desliga o bot", vipOnly = true)
    public void kill(MessageReceivedEvent event, ArgumentTranslator translator) {
        MilkshakeSimulator.running = false;
        event.getJDA().shutdown();
        System.exit(0);
    }


    @Command(name = "reiniciar", description = "Reinicia o bot", vipOnly = true)
    public void restart(MessageReceivedEvent event, ArgumentTranslator translator) {
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
            MilkshakeSimulator.running = false;
            event.getJDA().shutdown();
            System.exit(-1);
        } catch (URISyntaxException | IOException  e) {
            e.printStackTrace();
        }
    }

    @Command(name = "setPrefix", description = "Muda o prefixo do bot", vipOnly = true, args = {
            @Argument(name = "newPrefix", type = ArgumentType.STRING, obligatory = true)
    })
    public void setPrefix(MessageReceivedEvent event, ArgumentTranslator translator) {
        String newPrefix = translator.getAsString("newPrefix");
        try {
            CommandManager.getInstance().setPrefix(newPrefix);
            event.getChannel().sendMessage("**Prefixo trocado!**").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
