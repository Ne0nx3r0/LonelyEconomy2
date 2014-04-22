package com.ne0nx3r0.lonelyeconomy.economy;

import java.sql.Connection;
import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

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
            int serverStartingBalance = plugin.getConfig().getInt("server_starting_balance",0);
            
            this.con.setAutoCommit(false);
            
            PreparedStatement createAccountsTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS `"+this.TBL_ACCOUNTS+"` (  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  `username` VARCHAR(45) NOT NULL,  `uuid` VARCHAR(36) NOT NULL,  `balance` DECIMAL(13,2) UNSIGNED NOT NULL,  `last_seen` DATETIME NOT NULL,  PRIMARY KEY (`id`),  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC))ENGINE = InnoDB;");
            PreparedStatement createTransactionsTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS `"+this.TBL_TRANSACTIONS+"` (  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,  `account_id` INT UNSIGNED NOT NULL,  `amount` DECIMAL(13,2) NOT NULL,  `timestamp` TIMESTAMP NOT NULL,  PRIMARY KEY (`id`),  INDEX `id_idx` (`account_id` ASC),  CONSTRAINT `account_id`    FOREIGN KEY (`account_id`)    REFERENCES "+this.TBL_ACCOUNTS+" (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;");
            PreparedStatement createServerBalanceTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS `"+this.TBL_SERVER_BALANCE+"` (  `balance` DECIMAL(13,2) NOT NULL,  PRIMARY KEY (`balance`))ENGINE = InnoDB;");
            PreparedStatement insertServerBalance = this.con.prepareStatement("INSERT INTO "+this.TBL_SERVER_BALANCE+"(balance) VALUES(?);");
            insertServerBalance.setInt(1,serverStartingBalance);
            
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
            finally{
                createAccountsTable.close();
                createTransactionsTable.close();
                createServerBalanceTable.close();
                insertServerBalance.close();
            }
            
            //this.SERVER_BALANCE = this.getBigDecimal(serverStartingBalance);
        }
        else {
            //this.SERVER_BALANCE = this.getServerBalance();
        }
    }
    
    public LonelyEconomyResponse getPlayerAccount(String playerName,boolean createIfNotExists) {
        playerName = playerName.toLowerCase();
        
        try(PreparedStatement getPlayerAccount = this.con.prepareStatement("SELECT id,username,uuid,balance,last_seen FROM "+this.TBL_ACCOUNTS+" WHERE username=?")){
            
            getPlayerAccount.setString(1, playerName);
            
            try(ResultSet result = getPlayerAccount.executeQuery()){
                if(result.next()) {
                    return new LonelyEconomyResponse(
                        LonelyEconomyResponseType.SUCCESS,
                        new PlayerAccount(
                            result.getInt("id"),
                            result.getString("username"),
                            UUID.fromString(result.getString("uuid")),
                            result.getBigDecimal("balance")
                        )
                    );
                }
                else if(createIfNotExists){
                    // if the player is online create an account for them
                    Player player = Bukkit.getPlayer(playerName);

                    if(player != null){
                        return this.createPlayerAccount(player.getUniqueId(),player.getName().toLowerCase());
                    }
                }
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
            
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"A database error occured while finding an account for "+playerName);
        }  
            
        return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"No account exists for "+playerName);
    }
    
    public LonelyEconomyResponse getPlayerAccount(UUID playerUUID,boolean createIfNotExists) {
        try (PreparedStatement getPlayerAccount = this.con.prepareStatement("SELECT id,username,uuid,balance,last_seen FROM "+this.TBL_ACCOUNTS+" WHERE uuid=?")){
            
            getPlayerAccount.setString(1, playerUUID.toString());
            
            try(ResultSet result = getPlayerAccount.executeQuery()){
                
                if(result.next()) {
                    return new LonelyEconomyResponse(
                        LonelyEconomyResponseType.SUCCESS,
                        new PlayerAccount(
                            result.getInt("id"),
                            result.getString("username"),
                            UUID.fromString(result.getString("uuid")),
                            result.getBigDecimal("balance")
                        )
                    );
                }
                else if(createIfNotExists){
                    // if the player is online create an account for them
                    Player player = Bukkit.getPlayer(playerUUID);

                    if(player != null){
                        return this.createPlayerAccount(player.getUniqueId(),player.getName().toLowerCase());
                    }
                }
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
            
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"A database error occured while finding an account for "+playerUUID);
        }    
                 
        return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"No account exists for "+playerUUID);
    }
    
    public LonelyEconomyResponse createPlayerAccount(UUID playerUUID, String playerName) {
        try (PreparedStatement createPlayerAccount = this.con.prepareStatement("INSERT INTO "+this.TBL_ACCOUNTS+"(username,uuid,balance,last_seen) VALUES(?,?,?,?);",Statement.RETURN_GENERATED_KEYS)){
            createPlayerAccount.setString(1, playerName);
            createPlayerAccount.setString(2, playerUUID.toString());
            createPlayerAccount.setInt(3, 0);
            createPlayerAccount.setTimestamp(4, getCurrentTimeStamp());

            createPlayerAccount.executeUpdate();

            int affectedRows = createPlayerAccount.executeUpdate();
            
            if (affectedRows == 0) {
                 return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_DATABASE,"A database error occured while creating an account for "+playerName+"("+playerUUID+")");
            }
            
            try(ResultSet keys = createPlayerAccount.getGeneratedKeys()){
                if (keys.next()) {
                     return new LonelyEconomyResponse(
                        LonelyEconomyResponseType.SUCCESS,
                        new PlayerAccount(
                            keys.getInt(1),
                            playerName,
                            playerUUID,
                            this.getBigDecimal(0)
                        )
                     );
                }
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
            
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_DATABASE,"A database error occurred!");
        }      
        
        return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"Unable to create an account for "+playerName+"("+playerUUID+")");
    }

    public BigDecimal getServerBalance() {
        try(PreparedStatement getPlayerData = this.con.prepareStatement("SELECT balance FROM "+this.TBL_SERVER_BALANCE+";")){
            ResultSet result = getPlayerData.executeQuery();
            
            if(result.next()) {
                return this.getBigDecimal(result.getString("balance"));
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        return this.getBigDecimal(0);
    }
    
    public LonelyEconomyResponse giveMoneyToPlayer(String giveToPlayerName, BigDecimal amountToGivePlayer) {
        BigDecimal serverBalance = this.getServerBalance();
        
        // values were equal = 0
        // first value was greater = 1
        // second value was greater = -1
        if(amountToGivePlayer.compareTo(serverBalance) == 1){
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_INSUFFICIENT_FUNDS,"The server does not have "+amountToGivePlayer+" to spend!");
        }
        
        LonelyEconomyResponse playerAccountResponse = this.getPlayerAccount(giveToPlayerName, true);
        
        // verifies the player has a valid account to give the money to
        // before taking it from the server
        if(!playerAccountResponse.wasSuccessful()){
            return playerAccountResponse;
        }
        
        try(PreparedStatement takeMoneyFromServer = this.con.prepareStatement("UPDATE "+this.TBL_SERVER_BALANCE+" SET balance = balance - ?")){
            takeMoneyFromServer.setBigDecimal(1, amountToGivePlayer);
            
            int takeResult = takeMoneyFromServer.executeUpdate();
            if(takeResult > 0){
                PlayerAccount account = playerAccountResponse.getAccount();
                
                try(PreparedStatement giveMoneyToPlayer = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET balance = balance + ? WHERE uuid = ? LIMIT 1;")){
                    giveMoneyToPlayer.setBigDecimal(1, amountToGivePlayer);
                    giveMoneyToPlayer.setString(2, account.getUUID().toString());

                    giveMoneyToPlayer.executeUpdate();
                    
                    account.setBalance(account.getBalance().add(amountToGivePlayer));
                    
                    return new LonelyEconomyResponse(LonelyEconomyResponseType.SUCCESS,account);
                }
            }
            else{
                return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"Unable to retrieve funds from the server!");
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
        
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_DATABASE,"A database error occurred!");
        }
    }

    public LonelyEconomyResponse takeMoneyFromPlayer(String takeFromPlayerName, BigDecimal amountToTakeFromPlayer) {
        LonelyEconomyResponse takeFromResponse = this.getPlayerAccount(takeFromPlayerName, false);
        
        if(!takeFromResponse.wasSuccessful()) {
            return takeFromResponse;
        }
        
        PlayerAccount playerAccount = takeFromResponse.getAccount();
        
        // values were equal = 0
        // first value was greater = 1
        // second value was greater = -1
        if(playerAccount.getBalance().compareTo(amountToTakeFromPlayer) == -1){
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_INSUFFICIENT_FUNDS,playerAccount.getUsername()+" does not have "+amountToTakeFromPlayer+"!");
        }

        try(PreparedStatement takeMoneyFromPlayer = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET balance = balance - ? WHERE uuid = ? LIMIT 1;")){
            takeMoneyFromPlayer.setBigDecimal(1, amountToTakeFromPlayer);
            
            int takeResult = takeMoneyFromPlayer.executeUpdate();
            if(takeResult > 0){                
                try(PreparedStatement giveMoneyToPlayer = this.con.prepareStatement("UPDATE "+this.TBL_SERVER_BALANCE+" SET balance = balance + ?")){
                    giveMoneyToPlayer.setBigDecimal(1, amountToTakeFromPlayer);
                    giveMoneyToPlayer.setString(2, playerAccount.getUUID().toString());

                    giveMoneyToPlayer.executeUpdate();
                    
                    playerAccount.setBalance(playerAccount.getBalance().subtract(amountToTakeFromPlayer));
                    
                    return new LonelyEconomyResponse(LonelyEconomyResponseType.SUCCESS,playerAccount);
                }
            }
            else{
                return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"Unable to retrieve funds from the server!");
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
        
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_DATABASE,"A database error occurred!");
        }
    }
    
    public LonelyEconomyResponse payPlayer(String payFromPlayerName, String payToPlayerName, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LinkedHashMap<String, BigDecimal> getTopPlayers(int iTopAmount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // returns the player's rank or -1 for no account or error
    public int getRank(String playerName) {
        LonelyEconomyResponse response = this.getPlayerAccount(playerName, false);
        
        if(!response.wasSuccessful()) {
            return -1;
        }

        try(PreparedStatement statement = this.con.prepareStatement("SELECT COUNT(*) as rank FROM "+this.TBL_ACCOUNTS+" WHERE balance > ?"))
        {
            statement.setBigDecimal(1, response.getAccount().getBalance());
            
            try(ResultSet result = statement.executeQuery())
            {
                if(result.next())
                {
                    int iRank = result.getInt("rank")+1;

                    return iRank;
                }
            }
        }
        catch (SQLException ex)
        {
            this.logger.log(Level.SEVERE, null, ex);
        }
        
        return -1;
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
    
    private static java.sql.Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }
}
