package me.mateus.milkshake.core.command;

import me.mateus.milkshake.MilkshakeSimulator;
import me.mateus.milkshake.core.command.interfaces.Argument;
import me.mateus.milkshake.core.command.interfaces.Command;
import me.mateus.milkshake.core.command.translator.ArgumentTranslator;
import me.mateus.milkshake.core.milkshake.SourceRegion;
import me.mateus.milkshake.core.utils.StringComparator;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
public class CommandManager {

    private CommandManager() {}

    private String prefix = "m!";

    private static CommandManager instance;

    public static CommandManager getInstance() {
        if (instance == null) {
            synchronized (CommandManager.class) {
                if (instance == null) {
                    instance = new CommandManager();
                }
            }
        }
        return instance;
    }

    private final List<RegisteredCommand> commands = new ArrayList<>();

    public void registerCommands(Object object) {
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            Parameter[] params = method.getParameters();
            if (!paramsAreValid(params))
                continue;
            Command ann = method.getAnnotation(Command.class);
            if (ann == null)
                continue;
            commands.add(new RegisteredCommand(ann, method, object));
        }
    }

    public void setPrefix(String prefix) throws IOException {
        this.prefix= prefix;
        File prefixFile = new File("prefix.txt");
        if (!prefixFile.exists()) {
            if (!prefixFile.createNewFile()) {
                System.err.println("ERRO AO CRIAR ARQUIVO DE PREFIX");
                return;
            }
        }
        Files.write(prefixFile.toPath(), prefix.getBytes(StandardCharsets.UTF_8));
    }

    public List<RegisteredCommand> getCommands() {
        return commands;
    }

    public void setupPrefix() throws IOException {
        File prefixFile = new File("prefix.txt");
        if (!prefixFile.exists()) {
            if (!prefixFile.createNewFile()) {
                System.err.println("ERRO AO CRIAR ARQUIVO DE PREFIX");
                return;
            }
            Files.write(prefixFile.toPath(), "m!".getBytes(StandardCharsets.UTF_8));
        } else {
            String content = new String(Files.readAllBytes(prefixFile.toPath()), StandardCharsets.UTF_8);
            if (!content.isEmpty()) {
                this.prefix = content;
            }
        }
    }


    public RegisteredCommand getCommandByName(String name) {
        return commands.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public String getPrefix() {
        return prefix;
    }

    public void runCommand(GuildMessageReceivedEvent event) {
        String messageRaw = event.getMessage().getContentRaw();
        if (!messageRaw.startsWith(prefix))
            return;
        messageRaw = messageRaw.substring(prefix.length());
        String[] values = messageRaw.split("\\s+");
        String commandName = values[0];

        RegisteredCommand command = getCommandByName(commandName);
        if (command == null)
            return;
        User author = event.getAuthor();
        TextChannel channel = event.getChannel();
        if (command.isVipOnly() && !MilkshakeSimulator.VIPS.contains(author.getIdLong())) {
            channel.sendMessage("**Voc?? n??o tem permiss??o de executar este comando**").queue();
            return;
        }
        messageRaw = messageRaw.substring(commandName.length());
        ArgumentTranslator argTranslator = new ArgumentTranslator(messageRaw);

        StringBuilder invalidParameters = new StringBuilder();
        for (Argument argument : command.getArguments()) {
            if (argTranslator.hasNoArgument(argument.name()) && argument.obligatory()) {
                channel.sendMessage("**Comando invalido:**\n`" + command.getCorrectCommand() + "`").queue();
                return;
            }
            if (argTranslator.hasNoArgument(argument.name()))
                continue;
            boolean invalid = isInvalid(argTranslator.getAsString(argument.name()), argument.type(), argTranslator);
            if (!invalid)
                continue;
            invalidParameters.append(argument.name()).append(": ").append(argument.type().getUsage()).append("\n");
        }
        String s = invalidParameters.toString();
        if (!s.isEmpty()) {
            channel.sendMessage("**Os argumentos a seguir est??o errados:**\n`" + s+"`").queue();
            return;
        }
        command.execute(event, argTranslator);
    }

    private boolean paramsAreValid(Parameter[] params) {
        if (params.length != 2)
            return false;
        Parameter param1 = params[0];
        Parameter param2 = params[1];
        if (!GuildMessageReceivedEvent.class.isAssignableFrom(param1.getType())) {
            return false;
        }
        return ArgumentTranslator.class.isAssignableFrom(param2.getType());
    }

    private boolean isInvalid(String argValue, ArgumentType argumentType, ArgumentTranslator argumentTranslator) {
        boolean invalid = false;
        switch (argumentType) {

            case INTEGER:
                if (!StringComparator.isInteger(argValue)) {
                    invalid = true;
                }
                break;
            case POINT:
                if (!StringComparator.isPoint(argValue)) {
                    invalid = true;
                }
                break;
            case BOOLEAN:
                if (argValue.equalsIgnoreCase("true") || argValue.equalsIgnoreCase("false")) {
                    return false;
                }
            case REGIONS:
                List<SourceRegion> regions = argumentTranslator.toRegions(argValue);
                if (regions.isEmpty())
                    return true;
        }
        return invalid;
    }
}
