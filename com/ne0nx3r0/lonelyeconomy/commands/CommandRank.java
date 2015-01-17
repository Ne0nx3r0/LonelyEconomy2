package com.ne0nx3r0.lonelyeconomy.commands;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandRank extends LonelyCommand {
    private final LonelyEconomy economy;
    
    public CommandRank(LonelyEconomyPlugin plugin) {
        super(
            "rank", 
            "[username]", 
            "see a person's rank",
            "lonelyeconomy.rank"
        );
        
        this.economy = plugin.getEconomy();
    }
    
    @Override
    public boolean execute(CommandSender cs,String[] args) {
        if(args.length == 1) {
            if(!(cs instanceof Player)){
                this.sendError(cs,"You must specify a player name!");

                return true;
            }

            UUID uuid = ((Player) cs).getUniqueId();

            this.send(cs,"You are ranked "+ChatColor.GOLD+"#"+this.economy.getRank(uuid));
            
            return true;
        }
        
        String playerName = args[1];

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        int rank = this.economy.getRank(player.getUniqueId());
        
        if(rank != -1) {
            this.send(cs,playerName+ " is ranked "+ChatColor.GOLD+"#"+this.economy.getRank(player.getUniqueId()));
        }
        else {
            this.sendError(cs,playerName+" does not have an account");
        }
        
        return true;
    }
}
