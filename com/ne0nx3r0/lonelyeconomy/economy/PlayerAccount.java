package com.ne0nx3r0.lonelyeconomy.economy;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerAccount {
    private final String username;
    private final UUID uuid;
    private final int dbId;
    private BigDecimal balance;
    
    public PlayerAccount(int id,String username, UUID uuid) {
        this.dbId = id;
        this.username = username;
        this.uuid = uuid;
    }
    
    public PlayerAccount(int dbId, String username, UUID uuid, BigDecimal balance) {
        this.dbId = dbId;
        this.username = username;
        this.uuid = uuid;
        this.balance = balance;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    public void setBalance(BigDecimal amount) {
        this.balance = amount;
    }
    
    public BigDecimal getBalance() {
        return this.balance;
    }
    
    public int getDatabaseId() {
        return this.dbId;
    }
}
