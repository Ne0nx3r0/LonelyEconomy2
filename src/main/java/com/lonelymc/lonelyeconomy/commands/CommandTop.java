package com.lonelymc.lonelyeconomy.commands;

import com.lonelymc.lonelyeconomy.LonelyEconomyPlugin;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandTop extends LonelyCommand {
    private final LonelyEconomy economy;
    
    public CommandTop(LonelyEconomyPlugin plugin) {
        super(
                "top", 
                "[amount]", 
                "see the top-ranked players",
                "lonelyeconomy.top"
        );
        
        this.economy = plugin.getEconomy();
    }
    
    @Override
    public boolean execute(CommandSender cs,String[] args) {        
        int iTopAmount = 10;
        
        if(args.length == 2)
        {
            try
            {
                iTopAmount = Integer.parseInt(args[1]);
            }
            catch(NumberFormatException e)
            {
                this.send(cs,"Top",ChatColor.RED+args[1]+" is not a valid amount!");
                
                return true;
            }
        }
        
        if(iTopAmount > 50)
        {
            iTopAmount = 50;
        }
        else if(iTopAmount < 1){
            iTopAmount = 1;
        }

        List<String> toSend = new ArrayList<>();

        toSend.add("Top "+iTopAmount+" players");
        
        int iRank = 0;
        
        LinkedHashMap<String, BigDecimal> topPlayers = this.economy.getTopPlayers(iTopAmount);
        Iterator<Entry<String, BigDecimal>> it = topPlayers.entrySet().iterator();
        
        while (it.hasNext()) {
            iRank++;
            
            Map.Entry<String, BigDecimal> pairs = (Map.Entry<String, BigDecimal>) it.next();

            toSend.add(ChatColor.GOLD+"#"+iRank+" "+ChatColor.WHITE+pairs.getKey()+ChatColor.GRAY+" ("+this.economy.format((BigDecimal) pairs.getValue())+ChatColor.GRAY+")");
        }
        
        this.send(cs,toSend.toArray(new String[toSend.size()]));
        
        return true;
    }
}
