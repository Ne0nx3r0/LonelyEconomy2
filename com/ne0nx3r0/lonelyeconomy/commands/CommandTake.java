package com.ne0nx3r0.lonelyeconomy.commands;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomyResponse;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTake extends LonelyCommand {
    private final LonelyEconomy economy;
    
    public CommandTake(LonelyEconomyPlugin plugin) {
        super(
                "take", 
                "<username> <amount>", 
                "take money from a user and give to the server",
                "lonelyeconomy.take"
        );
        
        this.economy = plugin.getEconomy();
    }
    
    @Override
    public boolean execute(CommandSender cs,String[] args) {        
        if(args.length < 3)
        {
            this.send(cs,this.getUsage());

            return true;
        }
        
        String pTakeFrom = args[1];
        
        BigDecimal amount;
        
        try {
            amount = this.economy.getBigDecimal(args[2]);
        }
        catch(NumberFormatException e) {
            this.sendError(cs,"Invalid amount!");
            
            return true;
        }
  
        LonelyEconomyResponse ler = this.economy.takeMoneyFromPlayer(pTakeFrom, amount);
        
        if(!ler.wasSuccessful())
        {
            this.sendError(cs,ler.getMessage());
            
            return true;
        }
        
        this.send(cs,pTakeFrom+" lost "+this.economy.format(amount)+" to the server account!");
        
        Player pReceiver = Bukkit.getServer().getPlayer(pTakeFrom);
        
        if(pReceiver != null) {
            this.send(cs,"you lost "+this.economy.format(amount)+"!");
        }
        
        return true;
    }
}
