package gvlfm78.plugin.InactiveLockette;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class InactiveLockette extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft"); //Creating a logger
    public static Economy econ = null; //Creating economy variable
    InactiveLocketteConfigHandler ilch = InactiveLocketteConfigHandler.getInstance(); //Getting the instance of the Config Handler

    @Override
    public void onEnable(){

        if (!new File(getDataFolder(), "config.yml").exists()) { //Checking if config file exists
            ilch.setupConfig(this);//Creates config file
            ilch.setupLanguageEnglish(this);//Adds language strings
        }
        getServer().getPluginManager().registerEvents(new InactiveLocketteListener(this), this);//Firing event listener
        getCommand("inactivelockette").setExecutor(new InactiveLocketteCommandHandler(this));//Firing commands listener
        setupEconomy();//Setting up the economy
        if (!setupEconomy() && getConfig().getBoolean("useEconomy")) {//If economy is turned on
            //But no vault is found it will warn the user
            log.severe(String.format("[%s] - No Vault dependency found!", getDescription().getName()));
            return;
        }

        if(getConfig().getBoolean("settings.forceLanguageOverwriteOnNextStartup")){
            String language = getConfig().getString("settings.language");
            if(language.equals("en"))
                ilch.setLanguageEnglish(this);
            else if(language.equals("it"))
                ilch.setLanguageItalian(this);
            else{
                ilch.setLanguageEnglish(this);
            }
            getConfig().set("settings.forceLanguageOverwriteOnNextStartup", Boolean.valueOf(false));
            saveConfig();
            reloadConfig();
        }

        log.info(getDescription().getName() + " " + getDescription().getVersion() + " has been enabled");//Logging to console the enabling of IL

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
    }


    @Override
    public void onDisable(){

        reloadConfig();
        saveConfig();

        PluginDescriptionFile pdfFile = this.getDescription();//Logging to console the disabling of IL
        log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been disabled");
    }



    private boolean setupEconomy() {//Setting up the economy
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}