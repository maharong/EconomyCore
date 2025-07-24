package com.github.maharong.economycore.economy;

import com.github.maharong.economycore.log.EconomyLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private static Economy economy;

    public static boolean setup() {
        // Vault 플러그인이 설치되어 있는지 확인
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;

        // Economy 서비스 제공자를 등록된 것 중에서 가져옴
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        // economy 인스턴스 저장
        economy = rsp.getProvider();
        return true;
    }

    // 플레이어의 잔액을 반환
    public static double getBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    // 돈을 입금 (플레이어에게 돈을 지급)
    public static void deposit(OfflinePlayer player, double amount) {
        double before = getBalance(player);
        economy.depositPlayer(player, amount);
        double after = getBalance(player);
        EconomyLogger.log("지급", player, amount, before, after);
    }

    // 돈을 출금 (플레이어의 돈을 차감)
    public static void withdraw(OfflinePlayer player, double amount) {
        double before = getBalance(player);
        economy.withdrawPlayer(player, amount);
        double after = getBalance(player);
        EconomyLogger.log("차감", player, amount, before, after);
    }

    // 돈을 입금 (플레이어에게 돈을 지급), 로그 X
    public static void rawDeposit(OfflinePlayer player, double amount) {
        economy.depositPlayer(player, amount);
    }

    // 돈을 출금 (플레이어에게 돈을 차감), 로그 X
    public static void rawWithdraw(OfflinePlayer player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    // 돈을 설정
    public static void set(OfflinePlayer player, double amount) {
        double before = getBalance(player);
        if (amount > before) {
            rawDeposit(player, amount - before);
        } else if (amount < before) {
            rawWithdraw(player, before - amount);
        }
        double after = getBalance(player);
        EconomyLogger.log("설정", player, amount - before, before, after);
    }

    // 돈 송금
    public static boolean pay(OfflinePlayer player, OfflinePlayer target, double amount) {
        // 입력 금액이 0 미만의 경우 취소
        if (amount <= 0) return false;

        // 보내는 사람의 돈이 보낼 금액보다 작은 경우 취소
        double playerBefore = getBalance(player);
        if (playerBefore < amount) return false;

        double targetBefore = getBalance(target);

        // 잔액 조정
        rawWithdraw(player, amount);
        rawDeposit(target, amount);

        double playerAfter = getBalance(player);
        double targetAfter = getBalance(target);

        // 로그 기록
        EconomyLogger.log(
                "송금", player, target, amount,
                playerBefore, playerAfter, targetBefore, targetAfter);
        return true;
    }
}
