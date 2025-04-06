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

/**
 * 監聽玩家選擇領地的事件
 */
public class SelectionListener implements Listener {

    private final KoukeNekoResidenceExtension plugin;
    private final BossBarManager bossBarManager;
    private final SelectionManager selectionManager;

    public SelectionListener(KoukeNekoResidenceExtension plugin) {
        this.plugin = plugin;
        this.bossBarManager = plugin.getBossBarManager();
        
        // 增加更多的日誌信息來偵錯 Residence 插件的加載問題
        plugin.getLogger().info("[SelectionListener] 嘗試獲取 Residence 實例...");
        try {
            Object residenceInstance = Residence.getInstance();
            if (residenceInstance == null) {
                plugin.getLogger().severe("[SelectionListener] Residence.getInstance() 返回 null！");
                throw new RuntimeException("Residence實例為空");
            }
            plugin.getLogger().info("[SelectionListener] 成功獲取 Residence 實例: " + residenceInstance);
            
            // 嘗試獲取選擇管理器
            SelectionManager selManager = Residence.getInstance().getSelectionManager();
            if (selManager == null) {
                plugin.getLogger().severe("[SelectionListener] 無法獲取 SelectionManager！");
                throw new RuntimeException("SelectionManager為空");
            }
            this.selectionManager = selManager;
            plugin.getLogger().info("[SelectionListener] 成功獲取 SelectionManager: " + selectionManager);
        } catch (Exception e) {
            plugin.getLogger().severe("[SelectionListener] 初始化時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("無法初始化 SelectionListener: " + e.getMessage(), e);
        }
        
        // 啟動定時更新任務，更新所有正在選擇的玩家的 BossBar
        int updateInterval = plugin.getConfig().getInt("boss-bar.update-interval", 5);
        plugin.getLogger().info("[SelectionListener] Starting update task with interval: " + updateInterval + " ticks");
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllBossBars, updateInterval, updateInterval);
    }

    /**
     * 檢測玩家使用選擇工具的事件
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 確保只處理主手的事件，避免雙次處理
        if (event.getHand() != EquipmentSlot.HAND) {
            plugin.getLogger().info("[SelectionListener] Ignoring off-hand interaction for player: " + event.getPlayer().getName());
            return;
        }
        
        Player player = event.getPlayer();
        plugin.getLogger().info("[SelectionListener] Player " + player.getName() + " interacted with " + event.getAction());
        player.sendMessage("§7[Debug] Processing your interaction...");
        
        // 檢查玩家是否在使用領地選擇工具
        if (isSelectionTool(player, event.getAction(), event.getItem())) {
            plugin.getLogger().info("[SelectionListener] Player " + player.getName() + " used selection tool");
            player.sendMessage("§7[Debug] Selection tool used - updating BossBar in next tick");
            // 在下一個 tick 更新，確保領地插件已處理選擇事件
            Bukkit.getScheduler().runTask(plugin, () -> updateBossBar(player));
        } else {
            plugin.getLogger().info("[SelectionListener] Not a selection tool interaction for player: " + player.getName());
            player.sendMessage("§7[Debug] Not using selection tool");
        }
    }
    
    /**
     * 當玩家離線時移除他們的 BossBar
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info("[SelectionListener] Player quit: " + player.getName() + " - removing BossBar");
        bossBarManager.removeBossBar(player);
    }
    
    /**
     * 檢查玩家是否使用了領地選擇工具
     */
    private <CMIMaterial> boolean isSelectionTool(Player player, Action action, ItemStack item) {
        // 確保是左右鍵點擊方塊的事件
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            plugin.getLogger().info("[SelectionListener] Not a block click action for player: " + player.getName());
            return false;
        }
        
        // 檢查物品是否為領地選擇工具
        if (item == null) {
            plugin.getLogger().info("[SelectionListener] No item in hand for player: " + player.getName());
            return false;
        }
        
        // 獲取 Residence 配置中設定的選擇工具
        net.Zrips.CMILib.Items.CMIMaterial selectionTool = Residence.getInstance().getConfigManager().getSelectionTool();
        plugin.getLogger().info("[SelectionListener] Checking tool - Player: " + player.getName() + 
                              ", Tool in hand: " + item.getType() + 
                              ", Expected tool: " + selectionTool);
        
        // 檢查玩家手中的物品是否匹配選擇工具
        boolean isMatch = selectionTool != null && selectionTool.equals(item.getType());
        plugin.getLogger().info("[SelectionListener] Tool match result: " + isMatch);
        return isMatch;
    }
    
    /**
     * 更新玩家的選擇信息 BossBar
     */
    private void updateBossBar(Player player) {
        plugin.getLogger().info("[SelectionListener] Updating BossBar for player: " + player.getName());
        player.sendMessage("§7[Debug] Updating selection information...");
        
        // 檢查是否啟用了 BossBar 功能
        if (!plugin.getConfig().getBoolean("boss-bar.enabled", true)) {
            plugin.getLogger().info("[SelectionListener] BossBar feature is disabled in config");
            player.sendMessage("§7[Debug] BossBar feature is disabled in config");
            return;
        }
        
        // 檢查是否已經放置了兩個點
        if (selectionManager.hasPlacedBoth(player)) {
            plugin.getLogger().info("[SelectionListener] Player " + player.getName() + " has placed both points");
            player.sendMessage("§7[Debug] Both selection points are placed");
            
            // 獲取選擇區域
            CuboidArea selection = selectionManager.getSelectionCuboid(player);
            if (selection != null) {
                // 計算區域大小
                int size = (int) selection.getSize();
                
                // 計算創建領地的花費
                ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
                PermissionGroup group = rPlayer.getGroup();
                double cost = selection.getCost(group);
                
                plugin.getLogger().info("[SelectionListener] Selection info - Player: " + player.getName() + 
                                      ", Size: " + size + ", Cost: " + cost + 
                                      ", Group: " + group.getGroupName());
                player.sendMessage("§7[Debug] Selection size: " + size + ", cost: " + cost);
                
                // 更新或顯示 BossBar
                bossBarManager.updateBossBar(player, size, cost);
                
                // 如果配置了，也可以發送消息
                if (plugin.getConfig().getBoolean("messages.show-selection-updated", true)) {
                    String message = plugin.getConfig().getString("messages.selection-updated", "&e已更新選擇範圍，大小: &b{size} &e區塊，花費: &a${cost}")
                        .replace("{size}", String.valueOf(size))
                        .replace("{cost}", String.format("%.2f", cost));
                    plugin.getMessageManager().sendMessage(player, message);
                    plugin.getLogger().info("[SelectionListener] Sent selection update message to player: " + player.getName());
                }
            } else {
                plugin.getLogger().warning("[SelectionListener] Failed to get selection cuboid for player: " + player.getName());
                player.sendMessage("§7[Debug] Failed to get selection area");
            }
        } else {
            plugin.getLogger().info("[SelectionListener] Player " + player.getName() + " hasn't placed both points yet");
            player.sendMessage("§7[Debug] Both points not placed yet - removing BossBar");
            // 如果玩家尚未選擇完成兩個點，則隱藏 BossBar
            bossBarManager.removeBossBar(player);
        }
    }
    
    /**
     * 更新所有正在選擇的玩家的 BossBar
     */
    private void updateAllBossBars() {
        plugin.getLogger().info("[SelectionListener] Running periodic update for all players");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (selectionManager.hasPlacedBoth(player)) {
                plugin.getLogger().info("[SelectionListener] Updating BossBar for player with active selection: " + player.getName());
                updateBossBar(player);
            }
        }
    }
}
