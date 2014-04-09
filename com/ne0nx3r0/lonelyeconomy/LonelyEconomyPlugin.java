package com.ne0nx3r0.lonelyeconomy;

import com.ne0nx3r0.lonelyeconomy.commands.LonelyCommandExecutor;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public class LonelyEconomyPlugin extends JavaPlugin {
    private LonelyEconomy economy;
    
    @Override
    public void onEnable() {
        try {
            this.economy = new LonelyEconomy(this);
        } 
        catch (SQLException ex) {
            this.getLogger().log(Level.SEVERE, null, ex);
            
            this.getLogger().log(Level.INFO, "A database connection error occured, this plugin will not function.");
            
            this.economy = null;
            
            return;
            
            // TODO: attempt reconnect every x seconds either here or in the economy class
        }
        
        this.getCommand("money").setExecutor(new LonelyCommandExecutor(this));
    }
    
    @Override
    public void onDisable() {
        
    }
    
    public LonelyEconomy getEconomy() {
        return this.economy;
    }
}
