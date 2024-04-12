package me.mateus.milkshake.core.command;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.management.InvalidAttributeValueException;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public final class CommandEnvironment {
    private Pair<MessageReceivedEvent, SlashCommandInteractionEvent> eventUnion;
    
    public CommandEnvironment(MessageReceivedEvent event) {
        this.eventUnion = Pair.of(event, null);
    }

    public CommandEnvironment(SlashCommandInteractionEvent event) {
        this.eventUnion = Pair.of(null, event);
    }

    public void reply(String replyContent, boolean isInfo, @Nullable Consumer<Pair<Message, InteractionHook>> success) {
        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            event.getChannel().sendMessage(replyContent)
                .queue(m -> success.accept(Pair.of(m, null)));
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            event.reply(replyContent)
                .setEphemeral(isInfo)
                .queue(i -> success.accept(Pair.of(null, i))); 
        }
    }

    public void reply(String replyContent, boolean isInfo) {
        reply(replyContent, isInfo, null);
    }

    public void reply(String replyContent) {
        reply(replyContent, true);
    }

    public BufferedImage getAttatchedImage() {
        InputStream stream = null;

        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            Message message = event.getMessage();
            stream = getInputStreamFromMessage(message);
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            Attachment attachment = event.getOption("image").getAsAttachment();
            stream = getInputStreamFromAttatchment(attachment);
        }

        return inputStreamToImage(stream);
    }

    public static void edit(Pair<Message, InteractionHook> reply, String newReplyContent) throws InvalidAttributeValueException {
        if (reply.getRight() == null) { // MessageReceivedEvent
            Message m = reply.getLeft();
            m.editMessage(newReplyContent).queue();
        } else { // SlashCommandInteractionEvent
            InteractionHook i = reply.getRight();
            i.editOriginal(newReplyContent).queue();
        }
    }

    private static BufferedImage inputStreamToImage(InputStream inputStream) {
        try {
            return ImageIO.read(inputStream);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static InputStream getInputStreamFromAttatchment(Attachment attachment) {
        if (!attachment.isImage())
            return null;

        try {
            return attachment.getProxy().download().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static InputStream getInputStreamFromMessage(Message message) {
        List<Message.Attachment> attachments = message.getAttachments();
        
        if (!attachments.isEmpty())
            return getInputStreamFromAttatchment(attachments.get(0));
        
        return null;
    }
}
