package com.github.maharong.economycore.listener;

import com.github.maharong.economycore.economy.EconomyManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final FileConfiguration config;

    public JoinListener(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 기본 돈 지급
        if (!player.hasPlayedBefore()) {
            double startAmount = config.getDouble("starting-money", 1000.0);
            EconomyManager.rawDeposit(player, startAmount);
        }
    }
}
