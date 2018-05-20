package gvlfm78.plugin.InactiveLockette;

import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class ILMain extends JavaPlugin {

    private static final String[] lockettePlugins = {"Lockette", "LockettePro"};

    private static PluginDescriptionFile description;
    public static Economy econ = null;

    @Override
    public void onEnable(){
        description = getDescription();
        Messenger.initialise(this);
        ILConfigHandler.initialise(this);

        initialiseLocketteListener();

        getCommand("inactivelockette").setExecutor(new ILCommandHandler());

        setupEconomy();

        Messenger.sendConsoleMessage(description.getName() + " v" + description.getVersion() + " has been enabled");

        try{
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch(IOException ignored){}

        //todo bstats

        //todo spigot & bukkit update checking
        //Bukkit.getServer().getPluginManager().registerEvents(new ILJoinListener(getFile()), this);

        //Update Checking
        /*if(getConfig().getBoolean("checkForUpdates")){
            pm.registerEvents(new ILJoinListener(this, this.getFile()), this);
            final ILMain plugin = this;
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                public void run(){

                    Updater updater = new Updater(plugin, 52457, plugin.getFile(), Updater.UpdateType.DEFAULT, false);

                    if(updater.getResult().equals(UpdateResult.UPDATE_AVAILABLE)){
                        Messenger.sendConsoleMessage(ILConfigHandler.mes("onPluginLoad.updateAvailable") + " " + updater.getLatestName().replaceAll("[A-Za-z\\s]", ""));
                        Messenger.sendConsoleMessage(ILConfigHandler.mes("onPluginLoad.updateAvailableLink") + " " + updater.getLatestFileLink());
                    }
                }
            }, 20L);
        }*/
    }


    @Override
    public void onDisable(){
        Messenger.sendConsoleMessage(description.getName() + " " + description.getVersion() + " has been disabled");
    }

    private boolean setupEconomy(){//Setting up the economy
        if(getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) return false;

        econ = rsp.getProvider();
        return econ != null;
    }

    private void initialiseLocketteListener(){
        PluginManager pm = Bukkit.getServer().getPluginManager();

        for(String lockettePlugin : lockettePlugins){
            if(!pm.isPluginEnabled(lockettePlugin)) continue;

            try{
                Class<?> clazz = Class.forName(lockettePlugin + "Listener");
                Constructor<?> constructor = clazz.getConstructor();
                Listener listener = (Listener) constructor.newInstance();

                pm.registerEvents(listener, this);
            } catch (Exception e){
                Messenger.sendConsoleErrorMessage("Could not load " + lockettePlugin + " listener!");
            }

            Messenger.sendConsoleMessage(lockettePlugin + " detected, enabling " + lockettePlugin + " support");
        }
    }
    public static PluginDescriptionFile getDescriptionFile(){
        return description;
    }
}