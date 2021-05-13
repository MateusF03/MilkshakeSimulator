package me.mateus.milkshake.core.command.interfaces;

import me.mateus.milkshake.core.command.ArgumentType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {
    String name();
    ArgumentType type();
    boolean obligatory();
}
