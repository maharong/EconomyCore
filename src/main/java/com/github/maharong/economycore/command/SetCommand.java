package com.github.maharong.economycore.command;

import com.github.maharong.economycore.economy.EconomyManager;
import com.github.maharong.economycore.util.FormatUtil;
import com.github.maharong.economycore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SetCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        // 인자 개수 체크
        if (args.length < 2) {
            MessageUtil.send(sender, "money.usage.set");
            return;
        }

        // 퍼미션 체크
        if (!sender.hasPermission("economycore.admin")) {
            MessageUtil.send(sender, "money.no-permission");
            return;
        }

        // 닉네임이 실제로 접속한 적 없는 경우
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            MessageUtil.send(sender, "money.never-joined");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "money.invalid-amount");
            return;
        }

        EconomyManager.set(target, amount);
        MessageUtil.send(sender,"money.set", Map.of(
                "player", Objects.requireNonNull(target.getName()),
                "amount", FormatUtil.format(amount)
        ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
