package com.github.maharong.economycore.command;

import com.github.maharong.economycore.EconomyCore;
import com.github.maharong.economycore.economy.EconomyManager;
import com.github.maharong.economycore.util.FormatUtil;
import com.github.maharong.economycore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MoneyCommand implements CommandExecutor, TabCompleter {

    // label("돈", "money") → (subCommand("지급", "give") → 구현체)
    private final Map<String, Map<String, SubCommand>> subCommands = new HashMap<>();

    public MoneyCommand(EconomyCore plugin) {
        register("돈", "지급", new GiveCommand());
        register("돈", "차감", new TakeCommand());
        register("돈", "송금", new PayCommand());
        register("돈", "설정", new SetCommand());
        register("돈", "순위", new RankCommand());
        register("돈", "초기화", new ResetCommand());
        register("money", "give", new GiveCommand());
        register("money", "take", new TakeCommand());
        register("money", "pay", new PayCommand());
        register("money", "set", new SetCommand());
        register("money", "rank", new RankCommand());
        register("money", "reset", new ResetCommand());
    }

    private void register(String label, String sub, SubCommand command) {
        subCommands.computeIfAbsent(label, k -> new HashMap<>()).put(sub, command);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        String main = label.toLowerCase();
        Map<String, SubCommand> labelMap = subCommands.get(main);

        // /돈 or /money - 자신의 돈 확인
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                MessageUtil.send(sender, "money.usage"); // 콘솔에서는 사용 불가
                return true;
            }

            double balance = EconomyManager.getBalance(player);
            MessageUtil.send(player, "money.self", Map.of(
                    "amount", FormatUtil.format(balance)
            ));
            return true;
        }

        // 첫 번째 인자가 서브커맨드인지 확인
        String sub = args[0].toLowerCase();
        if (labelMap != null && labelMap.containsKey(sub)) {
            SubCommand subCommand = labelMap.get(sub);
            if (subCommand != null) {
                subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
        }

        // /돈 [닉네임] or /money [nickname]
        if (args.length == 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            // 닉네임이 실제로 접속한 적 없는 경우
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                MessageUtil.send(sender, "money.never-joined");
                return true;
            }

            double balance = EconomyManager.getBalance(target);
            MessageUtil.send(sender, "money.other", Map.of(
                    "player", Objects.requireNonNull(target.getName()),
                    "amount", FormatUtil.format(balance)
            ));
            return true;
        }

        // 서브커맨드 실패 메시지
        MessageUtil.send(sender, "money.unknown-subcommand", Map.of("sub", sub));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        String labelKey = label.toLowerCase();
        Map<String, SubCommand> labelMap = subCommands.get(labelKey);

        // 첫번째 인수 탭 완성 (서브커맨드 목록)
        if (args.length == 1 && labelMap != null) {
            return labelMap.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .toList();
        }

        // 이후 인수 탭 완성은 해당 서브커맨드에게 위임
        if (args.length > 1 && labelMap != null) {
            SubCommand subCommand = labelMap.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return List.of();
    }
}
