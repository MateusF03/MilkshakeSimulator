package me.mateus.milkshake.core.command;

import me.mateus.milkshake.core.command.interfaces.Argument;
import me.mateus.milkshake.core.command.interfaces.Command;
import me.mateus.milkshake.core.command.translator.ArgumentTranslator;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RegisteredCommand {

    private final String name;
    private final String description;
    private final boolean vipOnly;
    private final Argument[] arguments;
    private final Method method;
    private final Object object;

    public RegisteredCommand(Command annotation, Method method, Object object) {
        this.name = annotation.name();
        this.description = annotation.description();
        this.vipOnly = annotation.vipOnly();
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

    public void execute(GuildMessageReceivedEvent event, ArgumentTranslator argumentTranslator) {
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
}
