package com.ne0nx3r0.lonelyeconomy.migration;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomyResponse;
import com.ne0nx3r0.lonelyeconomy.economy.PlayerAccount;
import com.ne0nx3r0.lonelyeconomy.migration.com.evilmidget38.UUIDFetcher;
import com.ne0nx3r0.lonelyeconomy.migration.lib.PatPeter.SQLibrary.SQLite;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Migrator {
    private final LonelyEconomyPlugin plugin;
    private SQLite sqlite;
    
    public Migrator(LonelyEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void migrate() {
        System.out.println("Migrating balances from LonelyEconomy v1 database");

        this.loadDB();

        Map<String, BigDecimal> userBalances = this.getAllUserBalances();

        System.out.println("Found "+userBalances.size()+" accounts to migrate");

        LonelyEconomy economy = plugin.getEconomy();
        
        ArrayList<String> list = new ArrayList<>();
        
        list.addAll(userBalances.keySet());
        
        UUIDFetcher fetcher = new UUIDFetcher(list);

        Map<String, UUID> playerUUIDs = null;
        
        try {
            playerUUIDs = fetcher.call();
        } 
        catch (Exception e) {
            plugin.getLogger().warning("Exception while running UUIDFetcher");
            
            e.printStackTrace();
            
            return;
        }
        
        Map<String, UUID> playerUUIDsLower = new HashMap<>();
        
        // convert names to lower case...
        for(String username : playerUUIDs.keySet()){
            playerUUIDsLower.put(username.toLowerCase(),playerUUIDs.get(username));
        }
        
        for(String username : userBalances.keySet()){
            UUID uniqueId = playerUUIDsLower.get(username);

            if(uniqueId != null){
                LonelyEconomyResponse response = economy.createPlayerAccount(uniqueId, username);

                if(response.wasSuccessful()){
                    PlayerAccount account = response.getAccount();

                    economy.giveMoneyToPlayer(account.getUsername(), userBalances.get(username));

                    plugin.getLogger().log(Level.WARNING, "CA {0} with {2}", 
                            new Object[]{username, uniqueId, userBalances.get(username)});
                }
                else{
                    plugin.getLogger().log(Level.WARNING, "UCA {0} with {2}! "+response.getMessage(), 
                            new Object[]{username, uniqueId, userBalances.get(username)});
                }
            }
            else{
                plugin.getLogger().log(Level.WARNING, "UCAN for {0}({1}) with {2}! - null UUID!", 
                        new Object[]{username, uniqueId, userBalances.get(username)});
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Migrator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadDB() {
        this.sqlite = new SQLite(
            plugin.getLogger(),
            "LonelyEconomy",
            "lonelyeconomy",
            plugin.getDataFolder().getAbsolutePath()
        );

        try {
            sqlite.open();
        } 
        catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);

            return;
        }
    }

    public Map<String,BigDecimal> getAllUserBalances(){
        Map<String,BigDecimal> userBalances = new HashMap<>();

        try (PreparedStatement statement = sqlite.prepare("SELECT username,balance FROM accounts WHERE sorting_balance > 0 LIMIT 100000;")){
            try(ResultSet result = statement.executeQuery()){
                while(result.next())
                {
                    userBalances.put(result.getString("username").toLowerCase(),new BigDecimal(result.getString("balance")));
                }
            }
        } 
        catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }

        return userBalances;
    }
}
