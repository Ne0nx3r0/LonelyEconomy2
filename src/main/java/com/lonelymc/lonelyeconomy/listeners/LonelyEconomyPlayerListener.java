package com.lonelymc.lonelyeconomy.listeners;

import com.lonelymc.lonelyeconomy.LonelyEconomyPlugin;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LonelyEconomyPlayerListener implements Listener {
    private final LonelyEconomy economy;

    public LonelyEconomyPlayerListener(LonelyEconomyPlugin plugin) {
        this.economy = plugin.getEconomy();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.economy.updateLastSeen(e.getPlayer());
    }
}
