package com.github.maharong.economycore.command;

import com.github.maharong.economycore.EconomyCore;
import com.github.maharong.economycore.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EcoreCommand implements CommandExecutor, TabCompleter {
    private final EconomyCore plugin;

    public EcoreCommand(EconomyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("economycore.admin")) {
            MessageUtil.send(sender, "money.no-permission");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.send(sender, "plugin.usage");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "version" -> {
                String version = plugin.getDescription().getVersion();
                MessageUtil.send(sender, "plugin.version", Map.of("version", version));
            }
            case "reload" -> {
                MessageUtil.send(sender, "plugin.reloading");
                plugin.reloadConfig();
                plugin.reloadMessages();
                MessageUtil.send(sender, "plugin.reloaded");
            }
            default -> MessageUtil.send(sender, "plugin.usage");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("economycore.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return Stream.of("version", "reload")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}
