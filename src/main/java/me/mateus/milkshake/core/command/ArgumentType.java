package me.mateus.milkshake.core.command;

public enum ArgumentType {
    STRING("texto"),
    INTEGER("número"),
    POINT("x,y"),
    BOOLEAN("true/false"),
    REGIONS("(x) (y) (largura) (altura) (nº da source) [texto] [prioridade] [cor] [orientação] [fonte] [cor da borda] [tamanho da borda] | [...]");

    private final String usage;

    ArgumentType(String usage) {
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }
}
