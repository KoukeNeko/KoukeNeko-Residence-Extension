package dev.doeshing.koukeNekoResidenceExtension.commands;

import dev.doeshing.koukeNekoResidenceExtension.KoukeNekoResidenceExtension;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TestCommand implements CommandExecutor, TabCompleter {

    private final KoukeNekoResidenceExtension plugin;

    public TestCommand(KoukeNekoResidenceExtension plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 檢查是否為玩家
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "&c此命令只能由玩家執行！");
            return true;
        }

        Player player = (Player) sender;
        
        // 檢查權限
        if (!player.hasPermission("koukeneko.admin")) {
            plugin.getMessageManager().sendMessage(player, "&c你沒有權限使用此命令!");
            return true;
        }

        // 處理命令
        if (args.length > 0 && args[0].equalsIgnoreCase("bossbar")) {
            // 測試 BossBar
            // 呼叫主類中的測試方法
            plugin.testBossBar(player);
            
            // 發送測試完成訊息
            plugin.getMessageManager().sendMessage(player, "&a測試 BossBar 已顯示！");
        } else {
            // 顯示用法
            plugin.getMessageManager().sendMessage(player, "&c用法: /" + label + " bossbar - 測試顯示 BossBar");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 只有一個子命令: bossbar
            if ("bossbar".startsWith(args[0].toLowerCase())) {
                completions.add("bossbar");
            }
        }
        
        return completions;
    }
}