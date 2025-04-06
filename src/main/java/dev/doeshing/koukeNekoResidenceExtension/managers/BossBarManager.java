package dev.doeshing.koukeNekoResidenceExtension.managers;

import dev.doeshing.koukeNekoResidenceExtension.KoukeNekoResidenceExtension;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理玩家領地選擇時的 BossBar 顯示
 */
public class BossBarManager {

    private final KoukeNekoResidenceExtension plugin;
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Integer> autoHideTasks = new ConcurrentHashMap<>();
    
    private BarColor barColor;
    private BarStyle barStyle;
    private String barTitle;
    private boolean autoHide;
    private int displaySeconds;

    public BossBarManager(KoukeNekoResidenceExtension plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * 從配置文件加載 BossBar 設置
     */
    public void loadConfig() {
        // 讀取 BossBar 顏色
        try {
            this.barColor = BarColor.valueOf(plugin.getConfig().getString("boss-bar.color", "YELLOW"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("無效的 BossBar 顏色設定，使用預設值 YELLOW");
            this.barColor = BarColor.YELLOW;
        }
        
        // 讀取 BossBar 樣式
        try {
            this.barStyle = BarStyle.valueOf(plugin.getConfig().getString("boss-bar.style", "SOLID"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("無效的 BossBar 樣式設定，使用預設值 SOLID");
            this.barStyle = BarStyle.SOLID;
        }
        
        // 讀取 BossBar 標題格式
        this.barTitle = plugin.getConfig().getString("boss-bar.title", "&e領地選擇: &b{size} &6區塊 &7- &a花費: &2${cost}");
        
        // 讀取自動隱藏設定
        this.autoHide = plugin.getConfig().getBoolean("boss-bar.auto-hide", true);
        this.displaySeconds = plugin.getConfig().getInt("boss-bar.display-seconds", 5);
        
        // 預防無效的設定
        if (this.displaySeconds <= 0) {
            this.displaySeconds = 5;
            plugin.getLogger().warning("BossBar 顯示秒數必須大於 0，已重設為 5 秒");
        }
    }
    
    /**
     * 更新玩家的 BossBar 顯示
     * 
     * @param player 玩家
     * @param size 選擇區域大小
     * @param cost 領地花費
     */
    public void updateBossBar(Player player, int size, double cost) {
        UUID playerId = player.getUniqueId();
        BossBar bossBar = playerBossBars.get(playerId);
        
        // 如果玩家沒有 BossBar，則創建一個
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(
                formatTitle(size, cost),
                barColor,
                barStyle
            );
            playerBossBars.put(playerId, bossBar);
            bossBar.addPlayer(player);
        } else {
            // 更新現有 BossBar
            bossBar.setTitle(formatTitle(size, cost));
        }
        
        // 設置進度為 1 (滿)
        bossBar.setProgress(1.0);
        
        // 確保 BossBar 是可見的
        bossBar.setVisible(true);
        
        // 處理自動隱藏
        if (autoHide) {
            // 先取消與玩家相關的任何已存在的定時任務
            Integer existingTaskId = autoHideTasks.remove(playerId);
            if (existingTaskId != null) {
                Bukkit.getScheduler().cancelTask(existingTaskId);
            }
            
            // 創建新的定時任務來隱藏 BossBar
            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                removeBossBar(player);
                autoHideTasks.remove(playerId);
            }, displaySeconds * 20L); // 轉換為 tick (20 ticks = 1 秒)
            
            autoHideTasks.put(playerId, taskId);
        }
    }
    
    /**
     * 移除玩家的 BossBar
     * 
     * @param player 玩家
     */
    public void removeBossBar(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 取消相關的定時任務
        Integer taskId = autoHideTasks.remove(playerId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        
        // 移除 BossBar
        BossBar bossBar = playerBossBars.remove(playerId);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
        }
    }
    
    /**
     * 格式化 BossBar 標題
     * 
     * @param size 選擇區域大小
     * @param cost 領地花費
     * @return 格式化後的標題
     */
    private String formatTitle(int size, double cost) {
        String title = barTitle
            .replace("{size}", String.valueOf(size))
            .replace("{cost}", String.format("%.2f", cost))
            .replace("&", "§"); // 轉換顏色代碼
        
        return title;
    }
    
    /**
     * 移除所有 BossBar
     * 用於插件關閉時清理
     */
    public void removeAllBossBars() {
        // 取消所有定時任務
        for (Integer taskId : autoHideTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        autoHideTasks.clear();
        
        // 移除所有 BossBar
        for (BossBar bossBar : playerBossBars.values()) {
            bossBar.setVisible(false);
            bossBar.removeAll();
        }
        playerBossBars.clear();
    }
}
