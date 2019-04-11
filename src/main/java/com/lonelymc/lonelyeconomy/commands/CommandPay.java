package com.lonelymc.lonelyeconomy.commands;

import com.lonelymc.lonelyeconomy.LonelyEconomyPlugin;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomy;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomyResponse;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPay extends LonelyCommand {
    private final LonelyEconomy economy;
    
    public CommandPay(LonelyEconomyPlugin plugin) {
        super(
                "pay", 
                "<username> <amount>", 
                "pay another user",
                "lonelyeconomy.pay"
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
        
        if(!(cs instanceof Player)) {
            this.sendError(cs,"This command cannot be used from the console.");
            
            return true;
        }
        
        String sPayTo = args[1];
        
        BigDecimal amount;
        
        try {
            amount = this.economy.getBigDecimal(args[2]);
        }
        catch(NumberFormatException e) {
            this.sendError(cs,"Invalid amount!");
            
            return true;
        }

        // 1 - first one is bigger
        // 0 - the same
        // -1 - the second one is bigger
        if(amount.compareTo(BigDecimal.ZERO) != 1) {
            this.sendError(cs,"You cannot pay someone less than "+this.economy.format(this.economy.getBigDecimal("0.01")));
            
            return true;
        }

        Player senderPlayer = (Player) cs;

        OfflinePlayer payTo = Bukkit.getOfflinePlayer(sPayTo);

        if(payTo == null){
            this.sendError(cs,"Unable to find player "+sPayTo);

            return true;
        }

        LonelyEconomyResponse ler = this.economy.payPlayer(senderPlayer.getUniqueId(), payTo.getUniqueId(), amount);
        
        if(!ler.wasSuccessful())
        {
            this.sendError(cs,ler.getMessage());
            
            return true;
        }
        
        this.send(cs,"You paid "+this.economy.format(amount)+" to "+sPayTo+"!");
        
        Player pReceiver = Bukkit.getServer().getPlayer(sPayTo);
        
        if(pReceiver != null) {
            this.send(pReceiver,cs.getName()+" paid you  "+this.economy.format(amount)+"!");
        }
        
        return true;
    }
}
