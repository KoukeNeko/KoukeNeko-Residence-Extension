package dev.doeshing.koukeNekoResidenceExtension.commands;

import dev.doeshing.koukeNekoResidenceExtension.KoukeNekoResidenceExtension;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final KoukeNekoResidenceExtension plugin;

    public ReloadCommand(KoukeNekoResidenceExtension plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // 檢查權限
        if (!sender.hasPermission("koukeneko.admin")) {
            plugin.getMessageManager().sendMessage(sender, "&c你沒有權限使用此命令!");
            return true;
        }

        // 處理命令
        if (args[0].equalsIgnoreCase("reload")) {
            // 重新載入配置文件
            plugin.reloadConfig();

            // 重新載入訊息前綴
            plugin.getMessageManager().loadPrefix();

            // 發送重載完成訊息
            plugin.getMessageManager().sendMessage(sender, "&a插件設定成功重新載入!");
        }

        return true;
    }


}
