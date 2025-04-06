package dev.doeshing.koukeNekoResidenceExtension.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import dev.doeshing.koukeNekoResidenceExtension.KoukeNekoResidenceExtension;
import dev.doeshing.koukeNekoResidenceExtension.managers.BossBarManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 監聽玩家選擇領地的事件
 */
public class SelectionListener implements Listener {

    private final KoukeNekoResidenceExtension plugin;
    private final BossBarManager bossBarManager;
    private final SelectionManager selectionManager;
    
    // 用於防抖功能的玩家上次更新資訊
    private final Map<UUID, Long> lastUpdateTime = new HashMap<>();
    private final Map<UUID, Integer> selectionSizes = new HashMap<>();
    private final Map<UUID, Double> selectionCosts = new HashMap<>();
    
    // 防抖間隔，預設為 500ms
    private static final long DEBOUNCE_INTERVAL = 500L;

    public SelectionListener(KoukeNekoResidenceExtension plugin) {
        this.plugin = plugin;
        this.bossBarManager = plugin.getBossBarManager();
        
        try {
            // 獲取 Residence 實例
            Object residenceInstance = Residence.getInstance();
            if (residenceInstance == null) {
                throw new RuntimeException("Residence實例為空");
            }
            
            // 獲取選擇管理器
            SelectionManager selManager = Residence.getInstance().getSelectionManager();
            if (selManager == null) {
                throw new RuntimeException("SelectionManager為空");
            }
            this.selectionManager = selManager;
        } catch (Exception e) {
            plugin.getLogger().severe("無法獲取 Residence 插件功能: " + e.getMessage());
            throw new RuntimeException("無法初始化 SelectionListener: " + e.getMessage(), e);
        }

        // 註冊自定義事件以監聽 Residence 選擇
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 檢測玩家使用選擇工具的事件
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 確保只處理主手的事件，避免雙次處理
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // 檢查玩家是否在使用領地選擇工具
        if (isSelectionTool(player, event.getAction(), event.getItem())) {
            // 在下一個 tick 更新，確保領地插件已處理選擇事件
            Bukkit.getScheduler().runTask(plugin, () -> updateBossBar(player));
        }
    }
    
    /**
     * 當玩家離線時移除他們的 BossBar
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        bossBarManager.removeBossBar(player);
    }
    
    /**
     * 檢查玩家是否使用了領地選擇工具
     */
    private <CMIMaterial> boolean isSelectionTool(Player player, Action action, ItemStack item) {
        // 確保是左右鍵點擊方塊的事件
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }
        
        // 檢查物品是否為領地選擇工具
        if (item == null) {
            return false;
        }
        
        // 獲取 Residence 配置中設定的選擇工具
        net.Zrips.CMILib.Items.CMIMaterial selectionTool = Residence.getInstance().getConfigManager().getSelectionTool();
        
        // 檢查玩家手中的物品是否匹配選擇工具
        return selectionTool != null && selectionTool.equals(item.getType());
    }
    
    /**
     * 更新玩家的選擇信息 BossBar
     */
    private void updateBossBar(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // 檢查是否啟用了 BossBar 功能
        if (!plugin.getConfig().getBoolean("boss-bar.enabled", true)) {
            return;
        }
        
        // 檢查是否已經放置了兩個點
        if (selectionManager.hasPlacedBoth(player)) {
            // 獲取選擇區域
            CuboidArea selection = selectionManager.getSelectionCuboid(player);
            if (selection != null) {
                // 計算區域大小
                int size = (int) selection.getSize();
                
                // 計算創建領地的花費
                ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
                PermissionGroup group = rPlayer.getGroup();
                double cost = selection.getCost(group);
                
                // 防抖檢查: 檢查是否與上次更新相同
                Integer lastSize = selectionSizes.get(playerId);
                Double lastCost = selectionCosts.get(playerId);
                Long lastTime = lastUpdateTime.get(playerId);
                
                boolean hasSameValues = (lastSize != null && lastSize == size) && 
                                      (lastCost != null && Math.abs(lastCost - cost) < 0.01);
                boolean hasRecentUpdate = lastTime != null && (currentTime - lastTime) < DEBOUNCE_INTERVAL;
                
                if (hasSameValues && hasRecentUpdate) {
                    // 數值沒有變化且在防抖間隔內，不執行更新
                    return;
                }
                
                // 更新紀錄
                selectionSizes.put(playerId, size);
                selectionCosts.put(playerId, cost);
                lastUpdateTime.put(playerId, currentTime);
                
                // 更新或顯示 BossBar
                bossBarManager.updateBossBar(player, size, cost);
                
                // 如果配置了，也可以發送消息
                if (plugin.getConfig().getBoolean("messages.show-selection-updated", true)) {
                    // 只有資訊確實變化時才發送訊息
                    if (!hasSameValues) {
                        String message = plugin.getConfig().getString("messages.selection-updated", "&e已更新選擇範圍，大小: &b{size} &e區塊，花費: &a${cost}")
                            .replace("{size}", String.valueOf(size))
                            .replace("{cost}", String.format("%.2f", cost));
                        plugin.getMessageManager().sendMessage(player, message);
                    }
                }
            }
        } else {
            // 如果玩家尚未選擇完成兩個點，則隱藏 BossBar
            bossBarManager.removeBossBar(player);
            
            // 清除紀錄
            selectionSizes.remove(playerId);
            selectionCosts.remove(playerId);
            lastUpdateTime.remove(playerId);
        }
    }
    

}
