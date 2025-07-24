package com.github.maharong.economycore.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    // 명령어 실행
    void execute(CommandSender sender, String[] args);
    // 탭 완성 지원
    default List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
