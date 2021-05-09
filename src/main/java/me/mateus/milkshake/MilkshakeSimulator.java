package me.mateus.milkshake;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.mateus.milkshake.core.MilkshakeManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class MilkshakeSimulator {

    public static final Set<Long> REPUTABLE_PEOPLE = new HashSet<>();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean processing = false;

    public static void main(String[] args) {
        File tokenFile = new File("token.txt");
        File reputablePeople = new File("vips.txt");
        try {
            List<String> lines = Files.readAllLines(reputablePeople.toPath());
            for (String line : lines) {
                REPUTABLE_PEOPLE.add(Long.parseLong(line));
            }
            String token = new String(Files.readAllBytes(tokenFile.toPath()));
            MilkshakeManager.getInstance().setupMilkshakes();
            JDABuilder jdaBuilder = JDABuilder.createDefault(token);
            jdaBuilder.addEventListeners(new MilkshakeListener());
            JDA jda = jdaBuilder.build();
            jda.awaitReady();
            Thread consoleThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNext()) {
                    String next = scanner.nextLine();
                    if (next.equalsIgnoreCase("shutdown")) {
                        jda.shutdown();
                        System.exit(0);
                    }
                }
            });
            consoleThread.start();
        } catch (IOException | LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
