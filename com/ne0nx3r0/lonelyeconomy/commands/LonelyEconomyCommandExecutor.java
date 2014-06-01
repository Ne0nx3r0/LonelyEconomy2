package com.ne0nx3r0.lonelyeconomy.commands;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomyResponse;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LonelyEconomyCommandExecutor implements CommandExecutor {
    private final Map<String,LonelyCommand> subCommands;
    private final LonelyEconomy economy;

    public LonelyEconomyCommandExecutor(LonelyEconomyPlugin plugin) {
        this.economy = plugin.getEconomy();
        this.subCommands = new HashMap<>();
        
        this.registerSubcommand(new CommandBalance(plugin));
        this.registerSubcommand(new CommandGive(plugin));
        this.registerSubcommand(new CommandTake(plugin));
        this.registerSubcommand(new CommandPay(plugin));
        this.registerSubcommand(new CommandTop(plugin));
        this.registerSubcommand(new CommandRank(plugin));
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        if(args.length == 0 || args[0].equals("?")) {
            this.sendUsage(cs);
            
            return true;
        }

        LonelyCommand lonelyCommand = this.subCommands.get(args[0]);
        
        if(lonelyCommand == null){
            lonelyCommand = this.subCommands.get("balance");
            args = new String[]{"",args[0]};
        }
        
        if(lonelyCommand != null) {
            if(cs.hasPermission(lonelyCommand.getPermissionNode()) || !(cs instanceof Player)) {
                return lonelyCommand.execute(cs,args);
            }
            else {
                lonelyCommand.send(cs, 
                    ChatColor.RED+"You do not have permission to "+lonelyCommand.getAction(),
                    ChatColor.RED+"Required node: "+ChatColor.WHITE+lonelyCommand.getPermissionNode()
                );
            }
        }
        
        return false;
    }

    private void sendUsage(CommandSender cs) {
        cs.sendMessage(ChatColor.GRAY+"---"+ChatColor.GREEN+" LonelyEconomy "+ChatColor.GRAY+"---");
        cs.sendMessage("Here are the commands you have access to:");
        
        for(LonelyCommand lc : this.subCommands.values()) {
            if(cs.hasPermission(lc.getPermissionNode())) {
                cs.sendMessage(lc.getUsage());
            }
        }
        
        cs.sendMessage("Server reserve is "+economy.format(economy.getServerBalance()));
        
        if(cs instanceof Player){
            Player player = (Player) cs;
            
            LonelyEconomyResponse response = this.economy.getPlayerAccount(player.getUniqueId(), false);

            if(response.wasSuccessful()){
                cs.sendMessage("You have "+economy.format(response.getAccount().getBalance()));
            }
            else {
                cs.sendMessage("You have "+economy.format(this.economy.getBigDecimal(0)));
            }
        }
    }
    
    public final void registerSubcommand(LonelyCommand lc) {
        this.subCommands.put(lc.getName(), lc);
    }
}
