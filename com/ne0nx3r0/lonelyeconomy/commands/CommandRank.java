package com.ne0nx3r0.lonelyeconomy.commands;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
        if(args.length == 1)
        {
            this.send(cs,"You are ranked "+ChatColor.GOLD+"#"+this.economy.getRank(cs.getName()));
            
            return true;
        }
        
        String playerName = args[1];
        int rank = this.economy.getRank(playerName);
        
        if(rank != -1) {
            this.send(cs,playerName+ " is ranked "+ChatColor.GOLD+"#"+this.economy.getRank(playerName));
        }
        else {
            this.sendError(cs,playerName+" does not have an account");
        }
        
        return true;
    }
}
