package gvlfm78.plugin.InactiveLockette;

import gvlfm78.plugin.InactiveLockette.listeners.ILListener;
import gvlfm78.plugin.InactiveLockette.listeners.LocketteListener;
import gvlfm78.plugin.InactiveLockette.listeners.LocketteProListener;
import gvlfm78.plugin.InactiveLockette.updates.ILUpdateListener;
import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class ILMain extends JavaPlugin {

    private static final String[] lockettePlugins = {"Lockette", "LockettePro"};
    private static final ILListener[] locketteListeners = {new LocketteListener(), new LocketteProListener()};

    private static PluginDescriptionFile description;
    public static Economy econ = null;

    @Override
    public void onEnable(){
        description = getDescription();
        Messenger.initialiseLogging(getLogger());
        ILConfigHandler.initialise(this);
        Messenger.initialiseMessaging();

        initialiseLocketteListener();

        getCommand("inactivelockette").setExecutor(new ILCommandHandler());

        setupEconomy();

        setupWorldGuard();

        Messenger.sendConsoleMessage(description.getName() + " v" + description.getVersion() + " has been enabled");

        //MCStats
        try{
            MCStats MCStats = new MCStats(this);
            MCStats.start();
        } catch(IOException ignored){}

        //bStats
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("locale_language", () -> ILConfigHandler.getLanguage().getHumanReadableName()));

        //Checking for updates
        if(getConfig().getBoolean("checkForUpdates", true)){
            getServer().getPluginManager().registerEvents((new ILUpdateListener(this, this.getFile())), this);
        }
    }


    @Override
    public void onDisable(){
        Messenger.sendConsoleMessage(description.getName() + " " + description.getVersion() + " has been disabled");
    }

    private boolean setupEconomy(){
        if(getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) return false;

        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupWorldGuard(){
        FileConfiguration config = getConfig();
        if( (config.getBoolean("breakLockIfRegionOwner") || config.getBoolean("breakLockIfCanBuild"))
                && getServer().getPluginManager().getPlugin("WorldGuard") == null){
            Messenger.sendConsoleErrorMessage("You have enabled WorldGuard features but WorldGuard was not detected!");
            return false;
        }
        return true;
    }

    private void initialiseLocketteListener(){
        PluginManager pm = Bukkit.getServer().getPluginManager();

        for(int i = 0; i < lockettePlugins.length; i++){
            String lockettePlugin = lockettePlugins[i];
            if(!pm.isPluginEnabled(lockettePlugin)) continue;
            Messenger.sendConsoleMessage(lockettePlugin + " detected, enabling " + lockettePlugin + " support");
            pm.registerEvents(locketteListeners[i], this);
        }
    }
    public static PluginDescriptionFile getDescriptionFile(){
        return description;
    }
}