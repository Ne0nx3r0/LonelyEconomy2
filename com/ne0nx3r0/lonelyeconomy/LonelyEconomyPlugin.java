package com.ne0nx3r0.lonelyeconomy;

import com.ne0nx3r0.lonelyeconomy.commands.LonelyCommandExecutor;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import org.bukkit.plugin.java.JavaPlugin;

public class LonelyEconomyPlugin extends JavaPlugin {
    private LonelyEconomy economy;
    
    @Override
    public void onEnable() {
        this.economy = new LonelyEconomy();
        
        this.getCommand("money").setExecutor(new LonelyCommandExecutor(this));
    }
    
    @Override
    public void onDisable() {
        
    }
    
    public LonelyEconomy getEconomy() {
        return this.economy;
    }
}
