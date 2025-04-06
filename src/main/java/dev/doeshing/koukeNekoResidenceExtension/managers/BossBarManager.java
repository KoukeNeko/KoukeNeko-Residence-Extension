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

/**
 * 管理玩家領地選擇時的 BossBar 顯示
 */
public class BossBarManager {

    private final KoukeNekoResidenceExtension plugin;
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    
    private BarColor barColor;
    private BarStyle barStyle;
    private String barTitle;

    public BossBarManager(KoukeNekoResidenceExtension plugin) {
        this.plugin = plugin;
        loadConfig();
        plugin.getLogger().info("[BossBarManager] Initialized with color: " + barColor + ", style: " + barStyle);
    }
    
    /**
     * 從配置文件加載 BossBar 設置
     */
    public void loadConfig() {
        // 讀取 BossBar 顏色
        try {
            this.barColor = BarColor.valueOf(plugin.getConfig().getString("boss-bar.color", "YELLOW"));
            plugin.getLogger().info("[BossBarManager] Loaded bar color: " + barColor);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("無效的 BossBar 顏色設定，使用預設值 YELLOW");
            this.barColor = BarColor.YELLOW;
        }
        
        // 讀取 BossBar 樣式
        try {
            this.barStyle = BarStyle.valueOf(plugin.getConfig().getString("boss-bar.style", "SOLID"));
            plugin.getLogger().info("[BossBarManager] Loaded bar style: " + barStyle);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("無效的 BossBar 樣式設定，使用預設值 SOLID");
            this.barStyle = BarStyle.SOLID;
        }
        
        // 讀取 BossBar 標題格式
        this.barTitle = plugin.getConfig().getString("boss-bar.title", "&e領地選擇: &b{size} &6區塊 &7- &a花費: &2${cost}");
        plugin.getLogger().info("[BossBarManager] Loaded bar title format: " + barTitle);
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
        
        plugin.getLogger().info("[BossBarManager] Updating BossBar for player: " + player.getName() + 
                              " (size: " + size + ", cost: " + cost + ")");
        player.sendMessage("§7[Debug] Updating your BossBar... (size: " + size + ", cost: " + cost + ")");
        
        // 如果玩家沒有 BossBar，則創建一個
        if (bossBar == null) {
            plugin.getLogger().info("[BossBarManager] Creating new BossBar for player: " + player.getName());
            player.sendMessage("§7[Debug] Creating new BossBar...");
            
            bossBar = Bukkit.createBossBar(
                formatTitle(size, cost),
                barColor,
                barStyle
            );
            playerBossBars.put(playerId, bossBar);
            bossBar.addPlayer(player);
            
            plugin.getLogger().info("[BossBarManager] New BossBar created and added to player");
            player.sendMessage("§7[Debug] BossBar created and added");
        } else {
            // 更新現有 BossBar
            plugin.getLogger().info("[BossBarManager] Updating existing BossBar title for player: " + player.getName());
            player.sendMessage("§7[Debug] Updating existing BossBar...");
            bossBar.setTitle(formatTitle(size, cost));
        }
        
        // 設置進度為 1 (滿)
        bossBar.setProgress(1.0);
        
        // 確保 BossBar 是可見的
        bossBar.setVisible(true);
        
        plugin.getLogger().info("[BossBarManager] BossBar update completed for player: " + player.getName());
        player.sendMessage("§7[Debug] BossBar update completed");
    }
    
    /**
     * 移除玩家的 BossBar
     * 
     * @param player 玩家
     */
    public void removeBossBar(Player player) {
        plugin.getLogger().info("[BossBarManager] Attempting to remove BossBar for player: " + player.getName());
        player.sendMessage("§7[Debug] Removing your BossBar...");
        
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
            plugin.getLogger().info("[BossBarManager] BossBar successfully removed for player: " + player.getName());
            player.sendMessage("§7[Debug] BossBar removed successfully");
        } else {
            plugin.getLogger().info("[BossBarManager] No BossBar found to remove for player: " + player.getName());
            player.sendMessage("§7[Debug] No BossBar found to remove");
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
        
        plugin.getLogger().info("[BossBarManager] Formatted title: " + title);
        return title;
    }
    
    /**
     * 移除所有 BossBar
     * 用於插件關閉時清理
     */
    public void removeAllBossBars() {
        plugin.getLogger().info("[BossBarManager] Removing all BossBars...");
        
        for (BossBar bossBar : playerBossBars.values()) {
            bossBar.setVisible(false);
            bossBar.removeAll();
        }
        playerBossBars.clear();
        
        plugin.getLogger().info("[BossBarManager] All BossBars have been removed");
    }
}
