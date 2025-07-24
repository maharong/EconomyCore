package com.github.maharong.economycore.log;

import com.github.maharong.economycore.EconomyCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class EconomyLogger {
    public static void log(String type, OfflinePlayer player, double amount, double balanceBefore, double balanceAfter) {
        if (!EconomyCore.getInstance().getConfig().getBoolean("log")) return;

        String log = String.format("[%s] %s → %,+.2f (%.2f → %.2f)",
                type,
                player.getName(),
                amount,
                balanceBefore,
                balanceAfter
        );
        Bukkit.getLogger().info(log);
    }

    // 송금용 로그
    public static void log(
            String type, OfflinePlayer from, OfflinePlayer to, double amount,
            double fromBefore, double fromAfter, double toBefore, double toAfter) {
        if (!EconomyCore.getInstance().getConfig().getBoolean("log")) return;

        String log = String.format("[%s] %s → %s | %, .2f원 | 보낸이: %.2f → %.2f / 받은이: %.2f → %.2f",
                type,
                from.getName(),
                to.getName(),
                amount,
                fromBefore,
                fromAfter,
                toBefore,
                toAfter
        );
        Bukkit.getLogger().info(log);
    }
}