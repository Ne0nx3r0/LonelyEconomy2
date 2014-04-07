package com.ne0nx3r0.lonelyeconomy.economy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import org.bukkit.ChatColor;

public class LonelyEconomy {

    public boolean hasAccount(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public BigDecimal getBalance(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public String getCurrencyName(boolean plural) {
        return "ß";
    }
    
    public String format(BigDecimal amount) {
        return ChatColor.WHITE+amount.toPlainString()+ChatColor.GOLD+this.getCurrencyName(true)+ChatColor.RESET;
    }

    public BigDecimal getBigDecimal(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LonelyEconomyResponse giveMoneyToPlayer(String receiverName, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public BigDecimal getServerBalance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LonelyEconomyResponse takeMoneyFromPlayer(String pTakeFrom, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LonelyEconomyResponse payPlayer(String name, String sPayTo, BigDecimal amount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public LinkedHashMap<String, BigDecimal> getTopPlayers(int iTopAmount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getRank(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
