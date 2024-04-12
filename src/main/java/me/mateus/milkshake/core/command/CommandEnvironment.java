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

import me.mateus.milkshake.core.utils.Casing;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public final class CommandEnvironment {
    private final Consumer<Pair<Message, InteractionHook>> DO_NOTHING = p -> {};

    private Pair<MessageReceivedEvent, SlashCommandInteractionEvent> eventUnion;
    
    public Pair<MessageReceivedEvent, SlashCommandInteractionEvent> getEventUnion() {
        return eventUnion;
    }

    public CommandEnvironment(MessageReceivedEvent event) {
        this.eventUnion = Pair.of(event, null);
    }

    public CommandEnvironment(SlashCommandInteractionEvent event) {
        this.eventUnion = Pair.of(null, event);
    }

    public String getCommandName() {
        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            String contentRaw = event.getMessage().getContentRaw();
            String prefix = CommandManager.getInstance().getPrefix();
            if (!contentRaw.startsWith(prefix))
                return null;
            String contentWithoutPrefix = contentRaw.substring(prefix.length());
            String[] words = contentWithoutPrefix.split("\\s+");
            return words[0];
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            return event.getFullCommandName();
        }
    }

    public String getCommandBody() {
        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            String contentRaw = event.getMessage().getContentRaw();
            String[] words = contentRaw.split("\\s+");
            String body = contentRaw.substring(words[0].length());
            return body.trim();
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            String body = "";
            for (OptionMapping optionMapping : event.getOptions()) {
                if (optionMapping.getType() == OptionType.ATTACHMENT)
                    continue;
                String argumentName =
                    Casing.toCamelCase(Casing.fromSnakeCase(optionMapping.getName()));
                body += String.format("-%s %s ", argumentName, optionMapping.getAsString());
            }
            return body.trim();
        }
    }

    public User getCommandCaller() {
        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            return event.getAuthor();
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            return event.getUser();
        }
    }

    public MessageChannelUnion getCallingChannel() {
        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            return event.getChannel();
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            return event.getChannel();
        }
    }

    public JDA getJDAInstance() {
        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            return event.getJDA();
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            return event.getJDA();
        }
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
        reply(replyContent, isInfo, DO_NOTHING);
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

    public void embed(MessageEmbed embeddedContent, @Nullable Consumer<Pair<Message, InteractionHook>> success) {
        if (this.eventUnion.getRight() == null) {
            MessageReceivedEvent event = this.eventUnion.getLeft();
            event.getChannel().sendMessageEmbeds(embeddedContent)
                .queue(m -> success.accept(Pair.of(m, null)));
        } else {
            SlashCommandInteractionEvent event = this.eventUnion.getRight();
            event.replyEmbeds(embeddedContent)
                .queue(i -> success.accept(Pair.of(null, i)));
        }
    }

    public void embed(MessageEmbed embeddedContent) {
        embed(embeddedContent, DO_NOTHING);
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
