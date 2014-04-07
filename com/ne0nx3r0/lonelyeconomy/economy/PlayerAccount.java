package com.ne0nx3r0.lonelyeconomy.economy;

import java.math.BigDecimal;
import java.util.UUID;

public class PlayerAccount {
    private final String username;
    private final UUID uuid;
    private BigDecimal balance;
    
    public PlayerAccount(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
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
}
