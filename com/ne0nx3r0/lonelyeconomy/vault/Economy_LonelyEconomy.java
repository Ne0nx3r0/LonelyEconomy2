package com.ne0nx3r0.lonelyeconomy.vault;

import com.ne0nx3r0.lonelyeconomy.LonelyEconomyPlugin;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomy;
import com.ne0nx3r0.lonelyeconomy.economy.LonelyEconomyResponse;
import java.util.List;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_LonelyEconomy implements Economy {
    private static final Logger log = Logger.getLogger("Minecraft");
    private final Plugin plugin;
    private LonelyEconomy economy;
    public final String name = "Lonely Economy";
    
    public Economy_LonelyEconomy(Plugin plugin) {
        this.plugin = plugin;
        
        Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);

        if (economy == null) {
            LonelyEconomyPlugin lonelyEconomyPlugin = (LonelyEconomyPlugin) plugin.getServer().getPluginManager().getPlugin(name);

            if (lonelyEconomyPlugin != null && lonelyEconomyPlugin.isEnabled()) {
                economy = lonelyEconomyPlugin.getEconomy();

                log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), name));
            }
        }
    }
 
    public class EconomyServerListener implements Listener {
        Economy_LonelyEconomy economy = null;

        public EconomyServerListener(Economy_LonelyEconomy economy) {
            this.economy = economy;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            if (economy.economy == null) {
                Plugin eco = event.getPlugin();

                if (eco.getDescription().getName().equals("LonelyEconomy")) {
                    economy.economy = ((LonelyEconomyPlugin) eco).getEconomy();
                    log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), economy.name));
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginDisable(PluginDisableEvent event) {
            if (economy.economy != null) {
                if (event.getPlugin().getDescription().getName().equals("LonelyEconomy")) {
                    economy.economy = null;
                    log.info(String.format("[%s][Economy] %s unhooked.", plugin.getDescription().getName(), economy.name));
                }
            }
        }
    }  
    
    @Override
    public double getBalance(String playerName) {
        LonelyEconomyResponse response = this.economy.getPlayerAccount(playerName, false);
        
        if(!response.wasSuccessful()){
            return 0;
        }
        
        return response.getAccount().getBalance().doubleValue();
    }
    
    // Wrapper
    @Override
    public double getBalance(String playerName, String world) {
        return this.getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, double amount) {
        LonelyEconomyResponse response = this.economy.getPlayerAccount(playerName, false);
        
        if(!response.wasSuccessful()){
            return false;
        }
        
        return response.getAccount().getBalance().compareTo(this.economy.getBigDecimal(amount)) != -1;
    }
    
    // Wrapper
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return this.has(playerName,amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }
        
        LonelyEconomyResponse ler = this.economy.takeMoneyFromPlayer(playerName, this.economy.getBigDecimal(amount));
        
        if(ler.wasSuccessful()) {
            return new EconomyResponse(amount, this.economy.getDouble(ler.getAccount().getBalance()), EconomyResponse.ResponseType.SUCCESS, null);
        }

        return new EconomyResponse(0, this.economy.getDouble(ler.getAccount().getBalance()), EconomyResponse.ResponseType.FAILURE, ler.getMessage());
    }
    
    // Wrapper
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return this.withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }
        
        LonelyEconomyResponse ler = this.economy.giveMoneyToPlayer(playerName, this.economy.getBigDecimal(amount));
        
        if(ler.wasSuccessful()) {
            return new EconomyResponse(amount, this.economy.getDouble(ler.getAccount().getBalance()), EconomyResponse.ResponseType.SUCCESS, null);
        }

        return new EconomyResponse(0, this.economy.getDouble(ler.getAccount().getBalance()), EconomyResponse.ResponseType.FAILURE, ler.getMessage());
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return this.depositPlayer(playerName, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        LonelyEconomyResponse ler = this.economy.getPlayerAccount(playerName, true);
        
        return ler.wasSuccessful();
    }
    
    // Wrapper
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return this.createPlayerAccount(playerName);
    }

    @Override
    public boolean isEnabled() {
        if (economy == null) {
            return false;
        } else {
            return economy.isEnabled();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String format(double amount) {
        return this.economy.format(this.economy.getBigDecimal(amount));
    }

    @Override
    public String currencyNamePlural() {
        return this.economy.getCurrencyName(true);
    }

    @Override
    public String currencyNameSingular() {
        return this.economy.getCurrencyName(false);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return this.economy.getPlayerAccount(playerName, true).wasSuccessful();
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return this.hasAccount(playerName);
    }

    // Unsupported methods
    @Override
    public EconomyResponse createBank(String name, String player) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public List<String> getBanks() {
        throw new UnsupportedOperationException("Not supported."); 
    }
}
