package com.ne0nx3r0.lonelyeconomy.economy;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.PatPeter.SQLibrary.MySQL;
import net.minecraft.util.org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class LonelyEconomy {
    private final MySQL connection;
    private final Logger logger;
    
    public LonelyEconomy(LonelyEconomyPlugin plugin) throws SQLException {  
        this.logger = plugin.getLogger();
        
        ConfigurationSection dbConfig = plugin.getConfig().getConfigurationSection("database");
        
        String prefix = dbConfig.getString("prefix");
        String hostname = dbConfig.getString("hostname");
        String port = dbConfig.getString("port");
        String database = dbConfig.getString("database");
        String username = dbConfig.getString("username");
        String password = dbConfig.getString("password");
                
        this.connection = new MySQL(plugin.getLogger(),prefix,hostname,port,database,username,password);
        
        this.connection.open();

        if(!this.createTablesIfNeeded()) {
            // exception message should be right above this
            this.logger.log(Level.WARNING, "Unable to see if the database tables need to be created and create them if needed!");
        }
    }
    
    private boolean createTablesIfNeeded(){
        InputStream inputStream = getClass().getResourceAsStream("/createTables.sql");
        
        StringWriter writer = new StringWriter();
        
        try {
            IOUtils.copy(inputStream, writer);
        } 
        catch (IOException ex) {
            this.logger.log(Level.SEVERE, null, ex);
            
            return false;
        }
        
        String queries = writer.toString();
        
        PreparedStatement statement;
        
        try {
            statement = this.connection.prepare(queries);
            statement.execute();
        } 
        catch (SQLException ex) {
            this.logger.log(Level.SEVERE, null, ex);
            
            return false;
        }
        
        return true;
    }
    
    public boolean hasAccount(String playerName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public BigDecimal getBalance(String playerName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public BigDecimal getServerBalance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public LonelyEconomyResponse giveMoneyToPlayer(String giveToPlayerName, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LonelyEconomyResponse takeMoneyFromPlayer(String payFromPlayerName, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LonelyEconomyResponse payPlayer(String payFromPlayerName, String payToPlayerName, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LinkedHashMap<String, BigDecimal> getTopPlayers(int iTopAmount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getRank(String playerName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public BigDecimal getBigDecimal(double amount) {
        BigDecimal bd = new BigDecimal(Math.abs(amount));
        
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    public BigDecimal getBigDecimal(String amount) throws NumberFormatException {
        BigDecimal bd;

        bd = new BigDecimal(amount);

        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).abs();
    }
    
    public double getDouble(BigDecimal bd) {
        return bd.doubleValue();
    }
    
    public String getCurrencyName(boolean plural) {
        return "ß";
    }
    
    public String format(BigDecimal amount) {
        return ChatColor.WHITE+amount.toPlainString()+ChatColor.GOLD+this.getCurrencyName(true)+ChatColor.RESET;
    }
}
