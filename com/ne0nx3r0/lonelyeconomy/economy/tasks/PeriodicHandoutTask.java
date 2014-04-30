package com.ne0nx3r0.lonelyeconomy.economy.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomyResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PeriodicHandoutTask implements Runnable {
    private final LonelyEconomyPlugin plugin;
    private final LonelyEconomy economy;
    private final Map<String,BigDecimal> wageAmounts;

    public PeriodicHandoutTask(LonelyEconomyPlugin plugin, LonelyEconomy economy) {
        this.plugin = plugin;
        this.economy = economy;
        
        wageAmounts = new HashMap<>();
        
        ConfigurationSection wagesSection = plugin.getConfig().getConfigurationSection("wages");
        
        for(String key : wagesSection.getKeys(false)){
            wageAmounts.put("lonelyeconomy.wages."+key,economy.getBigDecimal(wagesSection.getString(key)));
        }
    }

    @Override
    public void run() {
        Essentials essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        
        Bukkit.getServer().broadcastMessage(ChatColor.GOLD+"[LonelyEconomy] "+ChatColor.WHITE+"Paying player wages");
            
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if(essentials != null){
                User user = essentials.getUser(player);

                if(user.isAfk()){
                    continue;
                }
            }
            
            BigDecimal amountToPayPlayer = BigDecimal.ZERO;
            
            // find the largest wage group they have the perm to
            for (Entry<String,BigDecimal> wageGroup : this.wageAmounts.entrySet()) {
                if(player.hasPermission(wageGroup.getKey())){
                    // getValue > amountToPayPlayer
                    if(wageGroup.getValue().compareTo(amountToPayPlayer) == 1){
                        amountToPayPlayer = wageGroup.getValue();
                    }
                }
            }
            
            // amountToPayPlayer > 0
            if(amountToPayPlayer.compareTo(BigDecimal.ZERO) == 1){
                LonelyEconomyResponse response = economy.giveMoneyToPlayer(player.getName(), amountToPayPlayer);

                if(response.wasSuccessful()){
                    player.sendMessage("You earned "+economy.format(amountToPayPlayer)+" in hourly wages!");
                }
                else {
                    plugin.getLogger().log(Level.INFO, 
                        "Unable to pay {0} ({1}) wage of {2} because: {3}",new Object[]{
                            player.getName(), 
                            player.getUniqueId(), 
                            amountToPayPlayer, 
                            response.getMessage()
                        }
                    );
                    
                    player.sendMessage(ChatColor.RED+"Error: "+ response.getMessage());
                    player.sendMessage(ChatColor.RED+"Unable to pay your hourly wage!");
                }                    
            }
        }
    }
    
}
