package com.ne0nx3r0.lonelyeconomy.listeners;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LonelyEconomyPlayerListener implements Listener {
    private final LonelyEconomy economy;

    public LonelyEconomyPlayerListener(LonelyEconomyPlugin plugin) {
        this.economy = plugin.getEconomy();
    }

    @EventHandler
    public void onPlayerInteract(PlayerJoinEvent e) {
        this.economy.updateLastSeen(e.getPlayer());
    }
}
