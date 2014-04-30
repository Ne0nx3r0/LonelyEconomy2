package com.ne0nx3r0.lonelyeconomy.economy;

import java.sql.Connection;
import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.tasks.PeriodicHandoutTask;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class LonelyEconomy {
    private Connection con;
    private final Logger logger;
    
    private final String TBL_ACCOUNTS;
    private final String TBL_TRANSACTIONS;
    private final String TBL_SERVER_BALANCE;
    private boolean enabled = false;
    private BukkitTask runTaskTimer;

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

            String createAccountsQuery = "CREATE TABLE IF NOT EXISTS ###TABLE_ACCOUNTS### (  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  `username` VARCHAR(45) NOT NULL,  `uuid` VARCHAR(36) NOT NULL,  `balance` DECIMAL(13,2) UNSIGNED NOT NULL,  `last_seen` DATETIME NOT NULL,  PRIMARY KEY (`id`),  UNIQUE INDEX `le2_username_UNIQUE` (`username` ASC),  UNIQUE INDEX `le2_uuid_UNIQUE` (`uuid` ASC))ENGINE = InnoDB";
            createAccountsQuery = createAccountsQuery.replace("###TABLE_ACCOUNTS###",this.TBL_ACCOUNTS);
            PreparedStatement createAccountsTable = this.con.prepareStatement(createAccountsQuery);
            
            String createTransactionsQuery = "CREATE TABLE IF NOT EXISTS ###TBL_TRANSACTIONS### (  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,  `account_id` INT UNSIGNED NOT NULL,  `amount` DECIMAL(13,2) NOT NULL,  `timestamp` TIMESTAMP NOT NULL,  PRIMARY KEY (`id`),  INDEX `id_idx` (`account_id` ASC),  CONSTRAINT `account_id`    FOREIGN KEY (`account_id`)    REFERENCES ###TABLE_ACCOUNTS### (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB";
            createTransactionsQuery = createTransactionsQuery.replace("###TBL_TRANSACTIONS###",this.TBL_TRANSACTIONS);
            createTransactionsQuery = createTransactionsQuery.replace("###TABLE_ACCOUNTS###",this.TBL_ACCOUNTS);
            PreparedStatement createTransactionsTable = this.con.prepareStatement(createTransactionsQuery);
            
            String createServerBalanceQuery = "CREATE TABLE IF NOT EXISTS ###TABLE_SERVER_ACCOUNT### (  `balance` DECIMAL(13,2) NOT NULL,  PRIMARY KEY (`balance`))ENGINE = InnoDB";
            createServerBalanceQuery = createServerBalanceQuery.replace("###TABLE_SERVER_ACCOUNT###",this.TBL_SERVER_BALANCE);
            
            PreparedStatement createServerBalanceTable = this.con.prepareStatement(createServerBalanceQuery);
            
            
            PreparedStatement insertServerBalance = this.con.prepareStatement("INSERT INTO "+this.TBL_SERVER_BALANCE+"(balance) VALUES(?)");
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
                
                this.con.setAutoCommit(true);
            }
        }
        
        tableExistsResultSet.close();
        
        this.resetTask(plugin);
        
        this.enabled = true;
    }
    
    public boolean isEnabled(){
        System.out.println("############################# Enabled: "+this.enabled);
        return this.enabled;
    }
    
    public LonelyEconomyResponse getPlayerAccount(String playerName,boolean createIfNotExists) {
        playerName = playerName.toLowerCase();
        
        try(PreparedStatement getPlayerAccount = this.con.prepareStatement("SELECT id,username,uuid,balance,last_seen FROM "+this.TBL_ACCOUNTS+" WHERE username=?")){
            
            getPlayerAccount.setString(1, playerName);
            
            try(ResultSet result = getPlayerAccount.executeQuery()){
                if(result.next()) {
                    String playerUsername = result.getString("username");
                    int playerDBID = result.getInt("id");
                    
                    return new LonelyEconomyResponse(
                        LonelyEconomyResponseType.SUCCESS,
                        new PlayerAccount(
                            playerDBID,
                            playerUsername,
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
    
    private synchronized boolean updatePlayerAccountUsername(int dbID, String username) {
        username = username.toLowerCase();
        
        try {
            PreparedStatement updateUsername = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET username=? WHERE id=? LIMIT 1;");
            updateUsername.setString(1, username);
            updateUsername.setInt(2, dbID);
            
            updateUsername.executeUpdate();
            
            this.logger.log(Level.INFO, "updated username for {0} dbid:{1}", new Object[]{username, dbID});
        } 
        catch (SQLException ex) {
            this.logger.log(Level.SEVERE, null, ex);
            
            return false;
        }
        return true;
    }
    
    public LonelyEconomyResponse getPlayerAccount(UUID playerUUID,boolean createIfNotExists) {
        try (PreparedStatement getPlayerAccount = this.con.prepareStatement("SELECT id,username,uuid,balance,last_seen FROM "+this.TBL_ACCOUNTS+" WHERE uuid=?")){
            
            getPlayerAccount.setString(1, playerUUID.toString());
            
            try(ResultSet result = getPlayerAccount.executeQuery()){
                
                if(result.next()) {
                    String playerUsername = result.getString("username");
                    
                    return new LonelyEconomyResponse(
                        LonelyEconomyResponseType.SUCCESS,
                        new PlayerAccount(
                            result.getInt("id"),
                            playerUsername,
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
        playerName = playerName.toLowerCase();
        
        try (PreparedStatement createPlayerAccount = this.con.prepareStatement("INSERT INTO "+this.TBL_ACCOUNTS+"(username,uuid,balance,last_seen) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            createPlayerAccount.setString(1, playerName);
            createPlayerAccount.setString(2, playerUUID.toString());
            createPlayerAccount.setInt(3, 0);
            createPlayerAccount.setTimestamp(4, getCurrentTimeStamp());

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
        try(PreparedStatement getPlayerData = this.con.prepareStatement("SELECT balance FROM "+this.TBL_SERVER_BALANCE)){
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
                
                try(PreparedStatement giveMoneyToPlayer = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET balance = balance + ? WHERE id = ? LIMIT 1")){
                    giveMoneyToPlayer.setBigDecimal(1, amountToGivePlayer);
                    giveMoneyToPlayer.setInt(2, account.getDatabaseId());

                    int giveMoneyRows = giveMoneyToPlayer.executeUpdate();
                    
                    if(giveMoneyRows > 0){
                        account.setBalance(account.getBalance().add(amountToGivePlayer));
                    
                        return new LonelyEconomyResponse(LonelyEconomyResponseType.SUCCESS,account);
                    }
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
        
        return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"Unable to give "+amountToGivePlayer+" to "+giveToPlayerName);
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

        try(PreparedStatement takeMoneyFromPlayer = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET balance = balance - ? WHERE id = ? LIMIT 1")){
            takeMoneyFromPlayer.setBigDecimal(1, amountToTakeFromPlayer);
            takeMoneyFromPlayer.setInt(2, playerAccount.getDatabaseId());
            
            int takeResult = takeMoneyFromPlayer.executeUpdate();
            if(takeResult > 0){                
                try(PreparedStatement giveMoneyToPlayer = this.con.prepareStatement("UPDATE "+this.TBL_SERVER_BALANCE+" SET balance = balance + ?")){
                    giveMoneyToPlayer.setBigDecimal(1, amountToTakeFromPlayer);

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
    
    public LonelyEconomyResponse payPlayer(String payFromPlayerName, String payToPlayerName, BigDecimal amountToPay) {
        LonelyEconomyResponse payFromResponse = this.getPlayerAccount(payFromPlayerName, false);
                
        if(!payFromResponse.wasSuccessful()) {
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_NO_ACCOUNT_EXISTS,"No account exists for "+payFromPlayerName);
        }
        
        PlayerAccount payFrom = payFromResponse.getAccount();
        
        // first value was greater = 1
        // values were equal = 0
        // second value was greater = -1
        if(payFrom.getBalance().compareTo(amountToPay) == -1){
            return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_INSUFFICIENT_FUNDS,payFromPlayerName+" does not have "+amountToPay+"!");
        }
                
        LonelyEconomyResponse payToResponse = this.getPlayerAccount(payToPlayerName, true);
        
        if(!payToResponse.wasSuccessful()){
            return payToResponse;
        }
        
        PlayerAccount payTo = payToResponse.getAccount();
        
        try(PreparedStatement takeMoneyFrom = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET balance = balance - ? WHERE id = ? LIMIT 1")){
            takeMoneyFrom.setBigDecimal(1, amountToPay);
            takeMoneyFrom.setInt(2, payFrom.getDatabaseId());
            
            int takeFromRows = takeMoneyFrom.executeUpdate();
            
            if(takeFromRows > 0){
                try(PreparedStatement giveMoneyTo = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET balance = balance + ? WHERE id = ? LIMIT 1")){
                    giveMoneyTo.setBigDecimal(1, amountToPay);
                    giveMoneyTo.setInt(2, payTo.getDatabaseId());

                    int giveMoneyRows = giveMoneyTo.executeUpdate();
                    
                    if(giveMoneyRows > 0){
                        return new LonelyEconomyResponse(LonelyEconomyResponseType.SUCCESS,payTo);
                    }
                } 
            }
            else {
                return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE,"Unable to pay "+payTo.getUsername());
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new LonelyEconomyResponse(LonelyEconomyResponseType.FAILURE_DATABASE,"A database error occurred!");
    }

    public LinkedHashMap<String, BigDecimal> getTopPlayers(int iTopAmount) {
        try(PreparedStatement statement = this.con.prepareStatement("SELECT username,balance FROM "+this.TBL_ACCOUNTS+" ORDER BY balance DESC LIMIT ?"))
        {
            statement.setInt(1, iTopAmount);
            
            try(ResultSet result = statement.executeQuery()){
                LinkedHashMap <String,BigDecimal> topPlayers = new LinkedHashMap <>();

                while(result.next())
                {
                    topPlayers.put(result.getString("username"),result.getBigDecimal("balance"));
                }

                return topPlayers;
            }
        }
        catch (Exception ex)
        {
            this.logger.log(Level.SEVERE, null, ex);
        }
        
        return null;
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

    public void updateLastSeen(Player player) {
        LonelyEconomyResponse response = this.getPlayerAccount(player.getUniqueId(), false);
        
        // else player doesn't have an account
        if(response.wasSuccessful()){
            PlayerAccount account = response.getAccount();
            
            if(!player.getName().toLowerCase().equals(account.getUsername())){
                if(!this.updatePlayerAccountUsername(account.getDatabaseId(),player.getName())){
                    player.sendMessage(ChatColor.RED+"LonelyEconomy was unable to update your username in the database. This may cause issues in delivering money to you.");
                }
            }

            try(PreparedStatement updateLastSeen = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET last_seen = now() WHERE id = ? AND last_seen < now() LIMIT 1")){
                updateLastSeen.setInt(1, account.getDatabaseId());

                updateLastSeen.executeUpdate();
            } 
            catch (SQLException ex) {
                Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
                 player.sendMessage(ChatColor.RED+"LonelyEconomy was unable to update your last seen time in the database.");
            }
        }
    }

    // primarily used for hourly wages
    public void giveMoneyToPlayers(List<UUID> playersToPay, BigDecimal amount) {
        String playersWhere = "";
        
        for(UUID playerUUID : playersToPay){
            playersWhere += "OR uuid = '"+playerUUID.toString()+"'";
        }
        
        playersWhere = playersWhere.substring(3);
        
        try(PreparedStatement giveMoneyToPlayers = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET balance = balance + ? WHERE "+playersWhere+" LIMIT ?")){
            giveMoneyToPlayers.setBigDecimal(1, amount);
            giveMoneyToPlayers.setInt(2, playersToPay.size());
            
            giveMoneyToPlayers.executeUpdate();
        } 
        catch (SQLException ex) {
            Logger.getLogger(LonelyEconomy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetTask(LonelyEconomyPlugin plugin) {
        if(this.runTaskTimer != null) {
            this.runTaskTimer.cancel();
        }
        
        long handoutTicks = plugin.getConfig().getLong("handout_timer",20*60*60);// once/hour

        this.runTaskTimer = plugin.getServer().getScheduler().runTaskTimer(plugin, new PeriodicHandoutTask(plugin,this), handoutTicks, handoutTicks);
    }
}
