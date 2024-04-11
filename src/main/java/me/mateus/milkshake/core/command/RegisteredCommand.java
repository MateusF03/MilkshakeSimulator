package me.mateus.milkshake.core.command;

import me.mateus.milkshake.core.command.interfaces.Argument;
import me.mateus.milkshake.core.command.interfaces.Command;
import me.mateus.milkshake.core.command.translator.ArgumentTranslator;
import me.mateus.milkshake.core.utils.Casing;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RegisteredCommand {

    private final String name;
    private final String description;
    private final boolean vipOnly;
    private final boolean receivesImage;
    private final Argument[] arguments;
    private final Method method;
    private final Object object;

    public RegisteredCommand(Command annotation, Method method, Object object) {
        this.name = annotation.name();
        this.description = annotation.description();
        this.vipOnly = annotation.vipOnly();
        this.receivesImage = annotation.receivesImage();
        this.arguments = annotation.args();
        this.method = method;
        this.object = object;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVipOnly() {
        return vipOnly;
    }

    public Argument[] getArguments() {
        return arguments;
    }

    public void execute(MessageReceivedEvent event, ArgumentTranslator argumentTranslator) {
        try {
            method.invoke(object, event, argumentTranslator);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public String getCorrectCommand() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CommandManager.getInstance().getPrefix()).append(name).append(" ");
        for (Argument argument : arguments) {
            if (argument.obligatory()) {
                stringBuilder.append("-").append(argument.name()).append(" (").append(argument.type().getUsage()).append(") ");
            } else {
                stringBuilder.append("-").append(argument.name()).append("[").append(argument.type().getUsage()).append("] ");
            }
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public SlashCommandData toSlashCommand() {
        String validCommandName =
            Casing.squash(Casing.fromCamelCase(this.name));
        SlashCommandData result = Commands.slash(validCommandName, this.description).setGuildOnly(true);
        for (Argument argument : arguments) {
            OptionType type = OptionType.STRING;
            switch (argument.type()) {
                case INTEGER:
                    type = OptionType.INTEGER;
                case BOOLEAN:
                    type = OptionType.BOOLEAN;
                default:
                    break;
            }
            String validArgumentName =
                Casing.toSnakeCase(Casing.fromCamelCase(argument.name()));
            String validDescription = argument.type().getUsage();
            if (validDescription.length() > 100)
                validDescription = validDescription.substring(0, 99) + "…";
            result = result.addOptions(
                new OptionData(type, validArgumentName, validDescription)
                    .setRequired(argument.obligatory())
            );
        }
        if (this.receivesImage)
            result = result.addOptions(
                new OptionData(OptionType.ATTACHMENT, "image", "imagem")
            );
        return result;
    }
}
