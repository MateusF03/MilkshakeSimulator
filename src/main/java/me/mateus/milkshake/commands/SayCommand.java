package me.mateus.milkshake.commands;

import me.mateus.milkshake.core.command.ArgumentType;
import me.mateus.milkshake.core.command.interfaces.Argument;
import me.mateus.milkshake.core.command.interfaces.Command;
import me.mateus.milkshake.core.command.translator.ArgumentTranslator;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SayCommand {

    @Command(name = "say", description = "Diz algo em um canal", vipOnly = true, args =
            {@Argument(name = "channel", type = ArgumentType.STRING, obligatory = true),
            @Argument(name = "text", type = ArgumentType.STRING, obligatory = true)})
    public void say(GuildMessageReceivedEvent event, ArgumentTranslator argumentTranslator) {
        String channel = argumentTranslator.getAsString("channel");
        String text = argumentTranslator.getAsString("text");

        TextChannel target = event.getJDA().getTextChannelById(channel);
        if (target == null) {
            event.getChannel().sendMessage("**Este canal não existe**").queue();
            return;
        }
        target.sendMessage(text).queue();
    }
}
