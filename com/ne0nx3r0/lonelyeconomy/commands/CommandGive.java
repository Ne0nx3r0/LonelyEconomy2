package com.ne0nx3r0.lonelyeconomy.commands;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomyResponse;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGive extends LonelyCommand {
    private final LonelyEconomy economy;
    
    public CommandGive(LonelyEconomyPlugin plugin) {
        super(
                "give", 
                "<username> <amount>", 
                "give a user money from the server",
                "lonelyeconomy.give"
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
        
        String receiverName = args[1];
        
        BigDecimal amount;
        
        try {
            amount = this.economy.getBigDecimal(args[2]);
        }
        catch(NumberFormatException e) {
            this.sendError(cs,"Invalid amount!");
            
            return true;
        }
  
        LonelyEconomyResponse ler = this.economy.giveMoneyToPlayer(receiverName, amount);
        
        if(!ler.wasSuccessful())
        {
            this.sendError(cs,ler.getMessage());
            
            return true;
        }
        
        this.send(cs,receiverName+" was paid "+this.economy.format(amount)+" from the server account.");
        
        Player pReceiver = Bukkit.getServer().getPlayer(receiverName);
        
        if(pReceiver != null) {
            pReceiver.sendMessage("You were paid "+this.economy.format(amount)+" from the server.");
        }
        
        return true;
    }
}
