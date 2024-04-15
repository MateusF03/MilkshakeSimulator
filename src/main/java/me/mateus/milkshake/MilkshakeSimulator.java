package me.mateus.milkshake;

import me.mateus.milkshake.commands.AdminCommands;
import me.mateus.milkshake.commands.CreateCommands;
import me.mateus.milkshake.commands.GenerateCommand;
import me.mateus.milkshake.commands.SayCommand;
import me.mateus.milkshake.core.command.CommandListener;
import me.mateus.milkshake.core.command.CommandManager;
import me.mateus.milkshake.core.command.RegisteredCommand;
import me.mateus.milkshake.core.milkshake.MilkshakeManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

public class MilkshakeSimulator {

    public static final Logger LOGGER = LoggerFactory.getLogger(MilkshakeSimulator.class);
    public static final Set<Long> VIPS = new HashSet<>();
    public static boolean running = true;

    public static void main(String[] args) throws IOException, LoginException {
        Path of = Path.of(".env");
        if (!Files.exists(of))
            Files.createFile(of);

        Dotenv dotenv = Dotenv.load();

        File vipsFile = new File("vips.txt");

        String token = dotenv.get("MILKSHAKE_TOKEN", "");
        if (token.isEmpty() || token.equals("<token>")) {
            LOGGER.error("Não foi possível ler a variável `MILKSHAKE_TOKEN`, especifique-a na execução ou no arquivo `.env`");
            String[] lines = {"MILKSHAKE_TOKEN=<token>", "MILKSHAKE_PREFIX=<prefix>"};
            Files.write(of, Arrays.asList(lines), StandardOpenOption.TRUNCATE_EXISTING);
            return;
        }
        if (vipsFile.exists()) {
            List<String> lines = Files.readAllLines(vipsFile.toPath());
            lines.forEach(l -> VIPS.add(Long.parseLong(l)));
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new CommandListener())
                .build();

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CommandListUpdateAction slashCommands;
        if (args.contains("guild-slash-commands"))
            slashCommands = jda.getGuildById(858724493966573589L).updateCommands();
        else
            slashCommands = jda.updateCommands();

        CommandManager manager = CommandManager.getInstance();
        manager.setupPrefix(dotenv);
        manager.registerCommands(slashCommands, new SayCommand());
        MilkshakeManager.getInstance().setupMilkshakes();
        manager.registerCommands(slashCommands, new GenerateCommand());
        manager.registerCommands(slashCommands, new CreateCommands());
        manager.registerCommands(slashCommands, new AdminCommands());

        slashCommands.queue();
    }
}
