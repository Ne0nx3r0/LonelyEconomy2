package com.ne0nx3r0.lonelyeconomy.commands;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import java.math.BigDecimal;
import org.bukkit.command.CommandSender;

public class CommandBalance extends LonelyCommand {
    private final LonelyEconomy economy;
    
    public CommandBalance(LonelyEconomyPlugin plugin) {
        super(
                "balance", 
                "<username>", 
                "get a player's balance",
                "lonelyeconomy.balance"
        );
        
        this.economy = plugin.getEconomy();
    }
    
    @Override
    public boolean execute(CommandSender cs,String[] args) {        
        if(args.length < 2)
        {
            this.send(cs,this.getUsage());

            return true;
        }
        
        String playerName = args[1];
        
        if(this.economy.hasAccount(playerName)) {
            BigDecimal playerBalance = this.economy.getBalance(playerName);
            
            this.send(cs,playerName+" has "+this.economy.format(playerBalance));
        }
        else {
            this.sendError(cs,playerName+" does not have an account!");
        }
        
        return true;
    }
}
