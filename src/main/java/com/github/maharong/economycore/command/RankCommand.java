package com.github.maharong.economycore.command;

import com.github.maharong.economycore.economy.EconomyManager;
import com.github.maharong.economycore.util.FormatUtil;
import com.github.maharong.economycore.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RankCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].matches("\\d+"))) {
            int page = (args.length == 0) ? 1 : Integer.parseInt(args[0]);
            showRankPage(sender, page);
        } else if (args.length == 1) {
            showPersonalRank(sender, args[0]);
        }
    }

    private void showRankPage(CommandSender sender, int page) {
        int entriesPerPage = 5;

        Map<OfflinePlayer, Double> balanceMap = new HashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            balanceMap.put(p, EconomyManager.getBalance(p));
        }

        List<Map.Entry<OfflinePlayer, Double>> sorted = balanceMap.entrySet().stream()
                .sorted(Map.Entry.<OfflinePlayer, Double>comparingByValue().reversed())
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil(sorted.size() / (double) entriesPerPage));
        page = Math.max(1, Math.min(page, totalPages));

        MessageUtil.send(sender, "money.rank.header", Map.of(
                "page", String.valueOf(page),
                "total_pages", String.valueOf(totalPages)
        ));

        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, sorted.size());

        for (int i = start; i < end; i++) {
            OfflinePlayer entryPlayer = sorted.get(i).getKey();
            double money = sorted.get(i).getValue();
            MessageUtil.send(sender, "money.rank.entry", Map.of(
                    "rank", String.valueOf(i + 1),
                    "name", Objects.requireNonNullElse(entryPlayer.getName(), "알 수 없음"),
                    "amount", FormatUtil.format(money)
            ));
        }

        Component prev = (page > 1)
                ? MessageUtil.get("money.rank.prev", Map.of("prev_page", String.valueOf(page - 1)))
                : MiniMessage.miniMessage().deserialize("<gray>이전</gray>");

        Component next = (page < totalPages)
                ? MessageUtil.get("money.rank.next", Map.of("next_page", String.valueOf(page + 1)))
                : MiniMessage.miniMessage().deserialize("<gray>다음</gray>");

        Component footer = Component.text("[ ")
                .append(prev)
                .append(Component.text(" | "))
                .append(next)
                .append(Component.text(" ]"));

        sender.sendMessage(footer);
    }

    private void showPersonalRank(CommandSender sender, String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            MessageUtil.send(sender, "money.rank.not-found");
            return;
        }

        Map<OfflinePlayer, Double> balanceMap = new HashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            balanceMap.put(p, EconomyManager.getBalance(p));
        }

        List<Map.Entry<OfflinePlayer, Double>> sorted = balanceMap.entrySet().stream()
                .sorted(Map.Entry.<OfflinePlayer, Double>comparingByValue().reversed())
                .toList();

        int rank = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().getUniqueId().equals(target.getUniqueId())) {
                rank = i + 1;
                break;
            }
        }

        MessageUtil.send(sender, "money.rank.personal", Map.of(
                "name", Objects.requireNonNullElse(target.getName(), "알 수 없음"),
                "rank", String.valueOf(rank),
                "amount", FormatUtil.format(EconomyManager.getBalance(target))
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
