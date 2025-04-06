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
        Bukkit.getScheduler().runTaskLater(this, () -> enablePlugin(), 20L);
    }
    
    private void enablePlugin() {
        try {
            // 保存默認配置
            saveDefaultConfig();
            
            getLogger().info("===== KoukeNeko Residence Extension 啟動中 =====");
            
            // 檢查領地插件
            
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
        try {
            // 直接添加一個測試 BossBar
            bossBarManager.updateBossBar(player, 10, 1000.0);
        } catch (Exception e) {
            getLogger().severe("測試 BossBar 時發生錯誤: " + e.getMessage());
            messageManager.sendMessage(player, "&c測試 BossBar 時發生錯誤，請查看控制台日誌");
            e.printStackTrace();
        }
    }
}
