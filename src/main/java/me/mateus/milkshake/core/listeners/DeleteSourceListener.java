package me.mateus.milkshake.core.listeners;

import me.mateus.milkshake.core.milkshake.MilkshakeManager;
import me.mateus.milkshake.core.milkshake.Source;
import me.mateus.milkshake.core.utils.StringComparator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class DeleteSourceListener extends ListenerAdapter {

    private final long userId;
    private final long channelId;
    private final List<Source> sources;

    public DeleteSourceListener(long userId, long channelId, List<Source> sources) {

        this.userId = userId;
        this.channelId = channelId;
        this.sources = sources;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild())
            return;
        if (event.getAuthor().getIdLong() != userId || event.getChannel().getIdLong() != channelId) {
            return;
        }
        String message = event.getMessage().getContentRaw();
        if (message.equals("&&cancel")) {
            event.getChannel().sendMessage("**Escolha cancelada**").queue();
            event.getJDA().removeEventListener(this);
            return;
        }
        if (!StringComparator.isInteger(message)) {
            String[] spliced = message.trim().split("\\s+");
            if (spliced.length != 2) {
                return;
            }
            String ver = spliced[0].trim();
            if (!ver.equalsIgnoreCase("&&ver")) {
                return;
            }
            if (!StringComparator.isInteger(spliced[1])) {
                return;
            }
            int idx = Integer.parseInt(spliced[1]);
            Source source = sources.get(idx - 1);
            try {
                BufferedImage image = ImageIO.read(new File(source.getImagePath()));
                event.getAuthor().openPrivateChannel().queue(c -> c.sendFiles(FileUpload.fromData(Objects.requireNonNull(bufferedImageToBytes(image)), "source.png")).queue(),
                        f -> event.getChannel().sendFiles(FileUpload.fromData(Objects.requireNonNull(bufferedImageToBytes(image)), "source.png")).queue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        int idx = Integer.parseInt(message);
        Source src = sources.get(idx - 1);
        if (src.errorWhileDeleting()) {
            event.getChannel().sendMessage("**Erro ao deletar a source**").queue();
            event.getJDA().removeEventListener(this);
            return;
        }
        MilkshakeManager.getInstance().getSources().remove(src);
        event.getChannel().sendMessage("**Source deletada!**").queue();
        event.getJDA().removeEventListener(this);
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
