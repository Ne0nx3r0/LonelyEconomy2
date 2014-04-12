package com.ne0nx3r0.lonelyeconomy.economy;

import java.sql.Connection;
import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class LonelyEconomy {
    private Connection con;
    private final Logger logger;
    
    private final String TBL_ACCOUNTS;
    private final String TBL_TRANSACTIONS;
    private final String TBL_SERVER_BALANCE;

    public LonelyEconomy(LonelyEconomyPlugin plugin) throws SQLException {  
        this.logger = plugin.getLogger();
        
        ConfigurationSection dbConfig = plugin.getConfig().getConfigurationSection("database");
        
        String prefix = dbConfig.getString("prefix","");
        String hostname = dbConfig.getString("hostname","localhost");
        String port = dbConfig.getString("port","3306");
        String database = dbConfig.getString("database");
        String username = dbConfig.getString("username");
        String password = dbConfig.getString("password");
        
        this.TBL_ACCOUNTS = prefix+"accounts";
        this.TBL_TRANSACTIONS = prefix+"transactions";
        this.TBL_SERVER_BALANCE = prefix+"server_balance";
                
	try {
            Class.forName("com.mysql.jdbc.Driver");
	} 
        catch (ClassNotFoundException ex) {
            this.logger.log(Level.SEVERE, null, ex);
            
            this.logger.log(Level.SEVERE,"No MySQL JDBC driver found (that's bad)");
            
            return;
	}
        
	try {
		this.con = DriverManager.getConnection("jdbc:mysql://"+hostname+":"+port+"/"+database,username,password);
	} 
        catch (SQLException ex) {
            this.logger.log(Level.SEVERE, null, ex);

            System.out.println("Database connection failed!");

            return;
	}
 
	if (this.con == null) {
            this.logger.log(Level.SEVERE,"Unable to connect to the database");
            
            return;
	}

        ResultSet tableExistsResultSet = this.con.getMetaData().getTables(null, null, this.TBL_SERVER_BALANCE, null);
        
        if(!tableExistsResultSet.next()) {
            this.con.setAutoCommit(false);
            
            PreparedStatement createAccountsTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS `"+this.TBL_ACCOUNTS+"` (  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  `username` VARCHAR(45) NOT NULL,  `uuid` VARCHAR(36) NOT NULL,  `balance` DECIMAL(13,2) UNSIGNED NOT NULL,  `last_seen` DATETIME NOT NULL,  PRIMARY KEY (`id`),  UNIQUE INDEX `username_UNIQUE` (`username` ASC),  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC))ENGINE = InnoDB;");
            PreparedStatement createTransactionsTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS `"+this.TBL_TRANSACTIONS+"` (  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,  `account_id` INT UNSIGNED NOT NULL,  `amount` DECIMAL(13,2) NOT NULL,  `timestamp` TIMESTAMP NOT NULL,  PRIMARY KEY (`id`),  INDEX `id_idx` (`account_id` ASC),  CONSTRAINT `account_id`    FOREIGN KEY (`account_id`)    REFERENCES "+this.TBL_ACCOUNTS+" (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;");
            PreparedStatement createServerBalanceTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS `"+this.TBL_SERVER_BALANCE+"` (  `balance` DECIMAL(13,2) NOT NULL,  PRIMARY KEY (`balance`))ENGINE = InnoDB;");
            PreparedStatement insertServerBalance = this.con.prepareStatement("INSERT INTO "+this.TBL_SERVER_BALANCE+"(balance) VALUES(?);");
            insertServerBalance.setInt(1,plugin.getConfig().getInt("server_starting_balance",0));
            
            try {
                createAccountsTable.execute();
                createTransactionsTable.execute();
                createServerBalanceTable.execute();
                insertServerBalance.executeUpdate();
                
                this.con.commit();
            } 
            catch (SQLException ex) {
                this.con.rollback();

                this.logger.log(Level.SEVERE, null, ex);

                this.con = null;

                return;
            }
        }
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
