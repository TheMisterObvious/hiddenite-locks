package eu.hiddenite.locks;

import eu.hiddenite.locks.commands.LockCommand;
import eu.hiddenite.locks.commands.UnlockCommand;
import eu.hiddenite.locks.listeners.LocksListeners;
import eu.hiddenite.locks.utils.LocksStorage;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

// TODO bypass permission (open, unlock, break)
// TODO add and remove users that can open

public class LocksPlugin extends JavaPlugin {
    private final LocksStorage storage = new LocksStorage(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginCommand lockCommand = getCommand("lock");
        if (lockCommand != null) {
            lockCommand.setExecutor(new LockCommand(this));
        }

        PluginCommand unlockCommand = getCommand("unlock");
        if (unlockCommand != null) {
            unlockCommand.setExecutor(new UnlockCommand(this));
        }

        getServer().getPluginManager().registerEvents(new LocksListeners(this, storage), this);
    }

    public void sendMessage(Player player, String configPath, Object... parameters) {
        String message = getConfig().getString("messages." + configPath);
        if (message != null) {
            for (int i = 0; i < parameters.length - 1; i += 2) {
                message = message.replace(parameters[i].toString(), parameters[i + 1].toString());
            }
            player.sendMessage(TextComponent.fromLegacyText(message));
        }
    }

    public OfflinePlayer findExistingPlayer(String name) {
        OfflinePlayer[] allPlayers = Bukkit.getOfflinePlayers();
        for (OfflinePlayer player : allPlayers) {
            if (player.getName() != null && player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    public void lockChest(Player player, Block block) {
        boolean alreadyLocked = storage.isContainerLocked(block);
        UUID owner = storage.getContainerOwner(block);

        if (!player.getUniqueId().equals(owner)) {
            sendMessage(player, "error-not-owner");
            return;
        }
        if (alreadyLocked) {
            sendMessage(player, "error-already-locked");
            return;
        }

        storage.lockContainer(block);

        getLogger().info(String.format(
                "Player %s locked the chest %s:[%d, %d, %d].",
                player.getName(),
                player.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()));

        sendMessage(player, "lock-success");
    }

    public void unlockChest(Player player, Block block) {
        boolean alreadyLocked = storage.isContainerLocked(block);
        UUID owner = storage.getContainerOwner(block);

        if (!player.getUniqueId().equals(owner)) {
            sendMessage(player, "error-not-owner");
            return;
        }
        if (!alreadyLocked) {
            sendMessage(player, "error-not-locked");
            return;
        }

        storage.unlockContainer(block);

        getLogger().info(String.format(
                "Player %s unlocked the chest %s:[%d, %d, %d].",
                player.getName(),
                player.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()));

        sendMessage(player, "unlock-success");
    }

    public void addPlayerToLock(Player owner, OfflinePlayer target, Block block) {
        // TODO
    }

    public void removePlayerFromLock(Player owner, OfflinePlayer target, Block block) {
        // TODO
    }
}
