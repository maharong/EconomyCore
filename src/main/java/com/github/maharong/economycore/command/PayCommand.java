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

public class PayCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        // 인자 개수 체크
        if (args.length < 2) {
            MessageUtil.send(sender, "money.usage.pay");
            return;
        }

        // 콘솔은 사용 불가능
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "money.console-only");
            return;
        }

        // 타겟이 실제로 접속한 적 없는 경우
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            MessageUtil.send(sender, "money.never-joined");
            return;
        }

        // 보낼 금액이 0보다 적은 경우
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            MessageUtil.send(sender, "money.invalid-amount");
            return;
        }

        // 송금 처리
        boolean result = EconomyManager.pay(player, target, amount);

        // 보낼 금액이 없는 경우
        if (!result) {
            MessageUtil.send(player, "money.insufficient");
            return;
        }
        // 보낸 사람에게 송금 완료 메세지
        MessageUtil.send(player, "money.sent", Map.of(
                "target", target.getName() != null ? target.getName() : "알 수 없음",
                "amount", FormatUtil.format(amount)
        ));

        // 받은 사람에게 송금 메세지 (getPlayer에서 null은 오프라인 상태)
        Player receiver = target.getPlayer();
        // 받은 사람이 온라인일 경우 메세지 전송
        if (receiver != null) {
            MessageUtil.send(receiver, "money.received", Map.of(
                    "sender", Objects.requireNonNull(player.getName()),
                    "amount", FormatUtil.format(amount)
            ));
        }
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
