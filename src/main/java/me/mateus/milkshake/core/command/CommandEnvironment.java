package me.mateus.milkshake.core.command;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import kotlin.NotImplementedError;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
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
        InputStream stream = null;

        if (this.event instanceof MessageReceivedEvent) {
            MessageReceivedEvent realEvent =
                (MessageReceivedEvent) this.event;
            Message message = realEvent.getMessage();
            stream = getInputStreamFromMessage(message);
        } else if (this.event instanceof SlashCommandInteractionEvent) {
            SlashCommandInteractionEvent realEvent =
                (SlashCommandInteractionEvent) this.event;
            Attachment attachment = realEvent.getOption("image").getAsAttachment();
            stream = getInputStreamFromAttatchment(attachment);
        } else {
            throw new NotImplementedError(this.event + " is not being handled by `CommandEnvironment`");
        }

        return inputStreamToImage(stream);
    }

    private BufferedImage inputStreamToImage(InputStream inputStream) {
        try {
            return ImageIO.read(inputStream);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private InputStream getInputStreamFromAttatchment(Attachment attachment) {
        if (!attachment.isImage())
            return null;

        try {
            return attachment.getProxy().download().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private InputStream getInputStreamFromMessage(Message message) {
        List<Message.Attachment> attachments = message.getAttachments();
        
        if (!attachments.isEmpty())
            return getInputStreamFromAttatchment(attachments.get(0));
        
        return null;
    }
}
