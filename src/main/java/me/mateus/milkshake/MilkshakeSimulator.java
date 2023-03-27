package me.mateus.milkshake;

import me.mateus.milkshake.commands.AdminCommands;
import me.mateus.milkshake.commands.CreateCommands;
import me.mateus.milkshake.commands.GenerateCommand;
import me.mateus.milkshake.commands.SayCommand;
import me.mateus.milkshake.core.command.CommandListener;
import me.mateus.milkshake.core.command.CommandManager;
import me.mateus.milkshake.core.milkshake.MilkshakeManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MilkshakeSimulator {

    public static final Logger LOGGER = LoggerFactory.getLogger(MilkshakeSimulator.class);
    public static final Set<Long> VIPS = new HashSet<>();
    public static boolean running = true;

    public static void main(String[] args) throws IOException, LoginException {
        if (!Files.exists(Path.of(".env")))
            Files.createFile(Path.of(".env"));

        Dotenv dotenv = Dotenv.load();

        File vipsFile = new File("vips.txt");

        String token = dotenv.get("MILKSHAKE_TOKEN");
        if (token.isEmpty() || token.equals("<token>")) {
            LOGGER.error("Não foi possível ler a variável `MILKSHAKE_TOKEN`, especifique-a na execução ou no arquivo `.env`");
            return;
        }
        if (vipsFile.exists()) {
            List<String> lines = Files.readAllLines(vipsFile.toPath());
            lines.forEach(l -> VIPS.add(Long.parseLong(l)));
        }

        CommandManager manager = CommandManager.getInstance();
        manager.setupPrefix(dotenv);
        manager.registerCommands(new SayCommand());
        MilkshakeManager.getInstance().setupMilkshakes();
        manager.registerCommands(new GenerateCommand());
        manager.registerCommands(new CreateCommands());
        manager.registerCommands(new AdminCommands());
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new CommandListener())
                .build();
    }
}
