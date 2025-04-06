package dev.doeshing.koukeNekoResidenceExtension;

import dev.doeshing.koukeNekoResidenceExtension.core.CommandSystem;
import dev.doeshing.koukeNekoResidenceExtension.core.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KoukeNekoResidenceExtension extends JavaPlugin {

    private MessageManager messageManager;
    private CommandSystem commandSystem;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.messageManager = new MessageManager(this);
        this.commandSystem = new CommandSystem(this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
