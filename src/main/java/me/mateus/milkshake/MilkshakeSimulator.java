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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class MilkshakeSimulator {

    public static final Logger LOGGER = LoggerFactory.getLogger(MilkshakeSimulator.class);
    public static final Set<Long> VIPS = new HashSet<>();
    public static boolean running = true;

    public static void main(String[] args) throws IOException, LoginException, InterruptedException {
        File tokenFile = new File("token.txt");
        File vipsFile = new File("vips.txt");

        if (!tokenFile.exists()) {
            LOGGER.info("Não foi possível encontrar o arquivo de token, irei tentar criar ele...");
            if (!tokenFile.createNewFile()) {
                LOGGER.error("Não consegui criar o arquivo de token");
                return;
            }
            LOGGER.info("Arquivo de token criado com sucesso! Escreva o token dentro dele.");
            return;
        }
        if (vipsFile.exists()) {
            List<String> lines = Files.readAllLines(vipsFile.toPath());
            lines.forEach(l -> VIPS.add(Long.parseLong(l)));
        }

        byte[] bytes = Files.readAllBytes(tokenFile.toPath());
        String token = new String(bytes, StandardCharsets.UTF_8);
        if (token.isEmpty()) {
            LOGGER.info("O arquivo de token está vazio");
            return;
        }
        CommandManager manager = CommandManager.getInstance();
        manager.setupPrefix();
        manager.registerCommands(new SayCommand());
        MilkshakeManager.getInstance().setupMilkshakes();
        manager.registerCommands(new GenerateCommand());
        manager.registerCommands(new CreateCommands());
        manager.registerCommands(new AdminCommands());
        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new CommandListener())
                .build();
        jda.awaitReady();
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String next = scanner.next();
                if (next.equalsIgnoreCase("shutdown")) {
                    jda.shutdown();
                    running = false;
                    System.exit(0);
                }
            }
        });
        consoleThread.start();
    }
}
