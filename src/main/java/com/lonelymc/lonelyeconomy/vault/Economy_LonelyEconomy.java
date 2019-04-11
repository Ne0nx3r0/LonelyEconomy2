package com.lonelymc.lonelyeconomy.vault;

import com.lonelymc.lonelyeconomy.LonelyEconomyPlugin;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomy;
import com.lonelymc.lonelyeconomy.economy.LonelyEconomyResponse;
import java.util.List;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
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

    // Wrapper
    @Override
    public double getBalance(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null){
            return 0;
        }

        return this.getBalance(offlinePlayer);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        LonelyEconomyResponse response = this.economy.getPlayerAccount(offlinePlayer.getUniqueId(), false);

        if(!response.wasSuccessful()){
            return 0;
        }

        return response.getAccount().getBalance().doubleValue();
    }

    // Wrapper
    @Override
    public double getBalance(String playerName, String world) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null){
            return 0;
        }

        return this.getBalance(offlinePlayer);
    }

    // Wrapper
    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String world) {
        return this.getBalance(offlinePlayer);
    }

    // Wrapper
    @Override
    public boolean has(String playerName, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null){
            return false;
        }

        return has(offlinePlayer, amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        LonelyEconomyResponse response = this.economy.getPlayerAccount(offlinePlayer.getUniqueId(), false);

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
    public boolean has(OfflinePlayer offlinePlayer, String worldName, double amount) {
        return this.has(offlinePlayer,amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "User account not found!");
        }

        return this.withdrawPlayer(offlinePlayer,amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        LonelyEconomyResponse ler = this.economy.takeMoneyFromPlayer(offlinePlayer.getUniqueId(), this.economy.getBigDecimal(amount));

        if(ler.wasSuccessful()) {
            return new EconomyResponse(amount, this.economy.getDouble(ler.getAccount().getBalance()), EconomyResponse.ResponseType.SUCCESS, null);
        }

        return new EconomyResponse(0, this.economy.getDouble(ler.getAccount().getBalance()), EconomyResponse.ResponseType.FAILURE, ler.getMessage());
    }

    // Wrapper
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "User account not found!");
        }

        return this.withdrawPlayer(offlinePlayer,amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String worldName, double amount) {
        return this.withdrawPlayer(offlinePlayer,amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "User account not found!");
        }

        return this.depositPlayer(offlinePlayer, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }
        
        LonelyEconomyResponse ler = this.economy.giveMoneyToPlayer(offlinePlayer.getUniqueId(), this.economy.getBigDecimal(amount));
        
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
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double amount) {
        return this.depositPlayer(offlinePlayer, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer != null){
            return this.economy.getPlayerAccount(offlinePlayer.getUniqueId(), true).wasSuccessful();
        }
        
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return this.economy.getPlayerAccount(offlinePlayer.getUniqueId(), true).wasSuccessful();
    }

    // Wrapper
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null){
            return false;
        }

        return this.createPlayerAccount(offlinePlayer);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String worldName) {
        return this.createPlayerAccount(offlinePlayer);
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
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if(offlinePlayer == null) {
            return false;
        }

        return this.economy.getPlayerAccount(offlinePlayer.getUniqueId(), true).wasSuccessful();
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return this.economy.getPlayerAccount(offlinePlayer.getUniqueId(), true).wasSuccessful();
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return this.hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String worldName) {
        return this.hasAccount(offlinePlayer);
    }

    // Unsupported methods
    @Override
    public EconomyResponse createBank(String name, String player) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
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
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public List<String> getBanks() {
        throw new UnsupportedOperationException("Not supported."); 
    }

    // new methods
}
