package com.github.maharong.economycore;

import com.github.maharong.economycore.command.EcoreCommand;
import com.github.maharong.economycore.command.MoneyCommand;
import com.github.maharong.economycore.economy.EconomyManager;
import com.github.maharong.economycore.listener.JoinListener;
import com.github.maharong.economycore.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class EconomyCore extends JavaPlugin {
    private static EconomyCore instance;

    public static EconomyCore getInstance() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        // Vault 연동 시도
        if (!EconomyManager.setup()) {
            getLogger().severe("Vault 연동 실패! 플러그인을 비활성화합니다.");
            getServer().getPluginManager().disablePlugin(this);
        }
        saveDefaultConfig();
        saveResource("message.yml", false);

        // 누락된 키 검사 및 복구
        checkAndRestoreAllKeys("config.yml");
        checkAndRestoreAllKeys("message.yml");

        // 메시지 설정 로딩
        File messageFile = new File(getDataFolder(), "message.yml");
        YamlConfiguration messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        MessageUtil.load(messageConfig);

        // 커맨드 등록
        Objects.requireNonNull(getCommand("돈")).setExecutor(new MoneyCommand(this));
        Objects.requireNonNull(getCommand("ecore")).setExecutor(new EcoreCommand(this));

        // 리스너 등록
        getServer().getPluginManager().registerEvents(new JoinListener(getConfig()), this);

        getLogger().info("플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        getLogger().info("플러그인이 비활성화되었습니다.");
    }

    public void reloadMessages() {
        checkAndRestoreAllKeys("message.yml");
        FileConfiguration messageConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "message.yml"));
        MessageUtil.load(messageConfig);
    }

    private FileConfiguration loadDefaultResource(String resourceName) {
        try (InputStream stream = getResource(resourceName)) {
            if (stream == null) return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            getLogger().severe(resourceName + " 기본 리소스를 불러오는 중 오류 발생: " + e.getMessage());
            return null;
        }
    }

    private void checkAndRestoreAllKeys(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) return;

        FileConfiguration current = YamlConfiguration.loadConfiguration(file);
        FileConfiguration def = loadDefaultResource(fileName);
        if (def == null) return;

        Set<String> currentKeys = current.getKeys(true);
        Set<String> defaultKeys = def.getKeys(true);

        if (!currentKeys.containsAll(defaultKeys)) {
            Set<String> missing = new HashSet<>(defaultKeys);
            missing.removeAll(currentKeys);

            getLogger().warning("[" + fileName + "] 누락된 키: " + missing);
            getLogger().warning(fileName + "을 기본 파일로 덮어씁니다.");

            // 기존 파일 백업
            File original = new File(getDataFolder(), fileName);
            File backup = new File(getDataFolder(), fileName + ".bak");

            try {
                if (!backup.exists()) {
                    Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                getLogger().warning("백업 중 오류 발생: " + e.getMessage());
            }

            saveResource(fileName, true); // 덮어쓰기
        }
    }
}
