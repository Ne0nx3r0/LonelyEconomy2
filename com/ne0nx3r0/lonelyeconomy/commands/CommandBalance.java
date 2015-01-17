package com.ne0nx3r0.lonelyeconomy.commands;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomyResponse;
import com.ne0nx3r0.lonelyeconomy.economy.PlayerAccount;
import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        LonelyEconomyResponse response = this.economy.getPlayerAccount(player.getUniqueId(),false);
        
        if(!response.wasSuccessful()) {
            this.send(cs,response.getMessage());
            
            return true;
        }
        
        PlayerAccount account = response.getAccount();

        this.send(cs,account.getUsername()+" has "+this.economy.format(account.getBalance()));
        
        return true;
    }
}
