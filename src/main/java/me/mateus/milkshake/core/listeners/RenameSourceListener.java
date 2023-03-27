package me.mateus.milkshake.core.listeners;

import me.mateus.milkshake.core.milkshake.Source;
import me.mateus.milkshake.core.utils.StringComparator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RenameSourceListener extends ListenerAdapter {

    private final long userId;
    private final long channelId;
    private final List<Source> sources;

    public RenameSourceListener(long userId, long channelId, List<Source> sources) {

        this.userId = userId;
        this.channelId = channelId;
        this.sources = sources;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
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
            String[] spliced = message.trim().split("[|]");
            if (spliced.length != 2) {
                return;
            }
            String number = spliced[0].trim();
            if (!StringComparator.isInteger(number)) {
                return;
            }
            int num = Integer.parseInt(number);
            String newName = spliced[1].trim();
            try {
                sources.get(num -1).rename(newName);
                event.getChannel().sendMessage("**Source renomeada para:** `" + newName + "`").queue();
                event.getJDA().removeEventListener(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        int idx = Integer.parseInt(message);
        Source source = sources.get(idx - 1);
        try {
            BufferedImage image = ImageIO.read(new File(source.getImagePath()));
            event.getAuthor().openPrivateChannel().queue(c -> c.sendFile(Objects.requireNonNull(bufferedImageToBytes(image)), "source.png").queue(),
                    f -> event.getChannel().sendFile(Objects.requireNonNull(bufferedImageToBytes(image)), "source.png").queue());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
