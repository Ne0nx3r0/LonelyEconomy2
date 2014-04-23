package com.ne0nx3r0.lonelyeconomy.economy.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

class PeriodicHandoutTask implements Runnable {
    private final LonelyEconomy economy;
    private final BigDecimal amount;

    public PeriodicHandoutTask(LonelyEconomy economy, BigDecimal amount) {
        this.economy = economy;
        this.amount = amount;
    }

    @Override
    public void run() {
        Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        Bukkit.getServer().broadcastMessage(ChatColor.GOLD+"[LonelyEconomy] "+ChatColor.WHITE+"Paying player wages...");

        List<UUID> playersToPay = new ArrayList<>();
        
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if( player.hasPermission("lonelyeconomy.wages")) {

                if(essentials != null){
                    User user = essentials.getUser(player);
                    
                    if(user.isAfk()){
                        continue;
                    }
                }
                playersToPay.add(player.getUniqueId());

                player.sendMessage("You earned "+economy.format(amount)+" in hourly wages!");
            }
        }
        
        this.economy.giveMoneyToPlayers(playersToPay, amount);
    }
    
}
