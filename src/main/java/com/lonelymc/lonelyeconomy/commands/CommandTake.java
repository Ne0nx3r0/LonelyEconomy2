package com.lonelymc.lonelyeconomy.commands;

import com.lonelymc.lonelyeconomy.LonelyEconomyPlugin;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomy;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomyResponse;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    @SuppressWarnings("deprecation")
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

        OfflinePlayer takeFromPlayer = Bukkit.getOfflinePlayer(pTakeFrom);

        if(takeFromPlayer == null){
            this.sendError(cs,"Unable to find player "+pTakeFrom);

            return true;
        }

        LonelyEconomyResponse ler = this.economy.takeMoneyFromPlayer(takeFromPlayer.getUniqueId(), amount);
        
        if(!ler.wasSuccessful())
        {
            this.sendError(cs,ler.getMessage());
            
            return true;
        }
        
        this.send(cs,pTakeFrom+" lost "+this.economy.format(amount)+" to the server account");
        
        Player pReceiver = Bukkit.getServer().getPlayer(pTakeFrom);
        
        if(pReceiver != null) {
            pReceiver.sendMessage("You had "+this.economy.format(amount)+" deducted.");
        }
        
        return true;
    }
}
