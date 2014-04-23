package com.ne0nx3r0.lonelyeconomy;

import com.ne0nx3r0.lonelyeconomy.commands.LonelyCommandExecutor;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.migration.Migrator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public class LonelyEconomyPlugin extends JavaPlugin {
    private LonelyEconomy economy;
    
    @Override
    public void onEnable() {
        try {
            getDataFolder().mkdirs();

            File configFile = new File(getDataFolder(),"config.yml");

            if(!configFile.exists())
            {
                copy(getResource("config.yml"), configFile);
            }
        } 
        catch (IOException ex) {
            this.getLogger().log(Level.INFO, "Unable to load config!");
            
            return;
        }
        
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
        
        if(this.getConfig().getBoolean("transfer_balances")){
            this.getLogger().log(Level.INFO,"!!!!!!!!!!!!!!!!going to transfer balances!!!!!!!!!!!!!!!");
            
            final LonelyEconomyPlugin plugin = this;
            
            this.getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable(){

                @Override
                public void run() {
                    Migrator migrator = new Migrator(plugin);

                    migrator.migrate();
                }
            }, 120);
        }
    }
    
    @Override
    public void onDisable() {
        
    }
    
    public LonelyEconomy getEconomy() {
        return this.economy;
    }

    public void copy(InputStream in, File file) throws IOException
    {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while((len=in.read(buf))>0)
        {
            out.write(buf,0,len);
        }
        out.close();
        in.close();
    }
}
