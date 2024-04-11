package me.mateus.milkshake.core.command;

import java.awt.image.BufferedImage;

import kotlin.NotImplementedError;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public final class CommandEnvironment {
    private Event event;
    
    public CommandEnvironment(MessageReceivedEvent event) {
        this.event = event;
    }

    public void reply(String replyContent) {
        if (this.event instanceof MessageReceivedEvent) {
            MessageReceivedEvent realEvent =
                (MessageReceivedEvent) this.event;
            realEvent.getChannel().sendMessage(replyContent)
                .queue();
        } else if (this.event instanceof SlashCommandInteractionEvent) {
            SlashCommandInteractionEvent realEvent =
                (SlashCommandInteractionEvent) this.event;
            realEvent.reply(replyContent)
                .setEphemeral(true) // TODO: make this be parametric
                .queue(); 
        } else {
            throw new NotImplementedError(this.event + " is not being handled by `CommandEnvironment`");
        }
    }

    public BufferedImage getAttatchedImage() {
        return null;
    }
}
