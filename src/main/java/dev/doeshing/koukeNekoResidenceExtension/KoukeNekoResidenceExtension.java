package dev.doeshing.koukeNekoResidenceExtension;

import dev.doeshing.koukeNekoResidenceExtension.commands.ReloadCommand;
import dev.doeshing.koukeNekoResidenceExtension.commands.TestCommand;
import dev.doeshing.koukeNekoResidenceExtension.core.CommandSystem;
import dev.doeshing.koukeNekoResidenceExtension.core.MessageManager;
import dev.doeshing.koukeNekoResidenceExtension.listeners.SelectionListener;
import dev.doeshing.koukeNekoResidenceExtension.managers.BossBarManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class KoukeNekoResidenceExtension extends JavaPlugin {

    private MessageManager messageManager;
    private BossBarManager bossBarManager;
    private SelectionListener selectionListener;

    @Override
    public void onEnable() {
        // 延遲加載，確保 Residence 先完全加載
        getLogger().info("延遲 20 ticks 後啟動插件，確保其他插件已完全加載...");
        Bukkit.getScheduler().runTaskLater(this, () -> enablePlugin(), 20L);
    }
    
    private void enablePlugin() {
        try {
            // 保存默認配置
            saveDefaultConfig();
            
            getLogger().info("===== KoukeNeko Residence Extension 啟動中 =====");
            
            // 檢查領地插件
            getLogger().info("正在檢查 Residence 插件...");
            
            // 列出所有已載入的插件，用於偵錯
            getLogger().info("服務器上已安裝的插件：");
            for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
                getLogger().info(" - " + plugin.getName() + " (" + plugin.getDescription().getVersion() + ")");
            }
            
            Plugin resPlug = getServer().getPluginManager().getPlugin("Residence");
            if (resPlug == null) {
                getLogger().severe("未找到 Residence 插件！本插件將被禁用。");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            if (!resPlug.isEnabled()) {
                getLogger().severe("Residence 插件已安裝但未啟用！本插件將被禁用。");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            getLogger().info("Residence 插件已找到: v" + resPlug.getDescription().getVersion());
            
            // 初始化管理器
            this.messageManager = new MessageManager(this);
            getLogger().info("訊息管理器已初始化");
            
            this.bossBarManager = new BossBarManager(this);
            getLogger().info("BossBar 管理器已初始化");
            
            // 註冊事件監聽器
            this.selectionListener = new SelectionListener(this);
            getServer().getPluginManager().registerEvents(this.selectionListener, this);
            getLogger().info("選擇監聽器已註冊");
            
            // 初始化命令系統
            CommandSystem commandSystem = new CommandSystem(this);
            getLogger().info("命令系統已初始化");
            
            // 註冊指令
            commandSystem.registerCommand(
                "knres", 
                new ReloadCommand(this),
                "koukeneko.admin", 
                "KoukeNeko-Residence-Extension 插件指令",
                "/knres reload - 重新載入插件設定",
                "koukenekorex", "krex");
            getLogger().info("重載命令已註冊");
            
            // 註冊測試指令
            commandSystem.registerCommand(
                "kntest", 
                new TestCommand(this),
                "koukeneko.admin", 
                "KoukeNeko-Residence-Extension 測試指令",
                "/kntest bossbar - 測試 BossBar 顯示",
                "knrtest");
            getLogger().info("測試命令已註冊");
            
            // 在下一個 Tick 發送測試消息以確保所有其他插件已加載完成
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage("§7[Debug] KoukeNeko Residence Extension 已載入，等待您使用選擇工具...");
                    player.sendMessage("§7[Debug] 您也可以使用 /kntest bossbar 命令來測試 BossBar 是否正常工作");
                    getLogger().info("發送測試消息給玩家: " + player.getName());
                }
                
                // 檢查並記錄目前已註冊的事件
                getLogger().info("SelectionListener 是否已註冊: " + (HandlerList.getRegisteredListeners(this).stream()
                    .anyMatch(handler -> handler.getListener() instanceof SelectionListener)));
            }, 20L);
            
            getLogger().info("KoukeNeko-Residence-Extension 插件已成功啟用！");
            getLogger().info("==============================================");
        } catch (Exception e) {
            getLogger().severe("插件啟動時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // 清理 BossBar
            if (bossBarManager != null) {
                bossBarManager.removeAllBossBars();
                getLogger().info("所有 BossBar 已清理");
            }
            
            // 取消註冊事件監聽器
            HandlerList.unregisterAll(this);
            getLogger().info("所有事件監聽器已取消註冊");
            
            getLogger().info("KoukeNeko-Residence-Extension 插件已關閉！");
        } catch (Exception e) {
            getLogger().severe("插件關閉時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 重新載入插件配置
     */
    @Override
    public void reloadConfig() {
        try {
            super.reloadConfig();
            
            // 重新載入相關配置
            if (messageManager != null) {
                messageManager.loadPrefix();
                getLogger().info("訊息前綴已重新載入");
            }
            
            if (bossBarManager != null) {
                bossBarManager.loadConfig();
                getLogger().info("BossBar 設定已重新載入");
            }
        } catch (Exception e) {
            getLogger().severe("重新載入配置時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
    
    /**
     * 添加一個測試方法，用於檢查和添加 BossBar
     */
    public void testBossBar(Player player) {
        getLogger().info("執行 BossBar 測試，玩家: " + player.getName());
        player.sendMessage("§7[Debug] 正在測試 BossBar...");
        
        try {
            // 直接嘗試添加一個測試 BossBar
            bossBarManager.updateBossBar(player, 10, 1000.0);
            player.sendMessage("§7[Debug] 測試 BossBar 已添加，顯示大小為 10，費用為 1000");
        } catch (Exception e) {
            getLogger().severe("測試 BossBar 時發生錯誤: " + e.getMessage());
            player.sendMessage("§c[錯誤] 測試 BossBar 時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
