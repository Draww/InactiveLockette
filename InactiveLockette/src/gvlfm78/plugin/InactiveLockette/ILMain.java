package gvlfm78.plugin.InactiveLockette;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class ILMain extends JavaPlugin {

	private final Logger log = Logger.getLogger("Minecraft"); //Logger
	public static Economy econ = null; //Creating economy variable
	private ILConfigHandler conf = new ILConfigHandler(this); //Getting the instance of the Config Handler

	protected ILUpdateChecker updateChecker;

	@Override
	public void onEnable(){

		conf.setupConfigYML();//Creates config file if not existant
		conf.upgradeConfig();
		conf.setupLocale();//Creates locale file if not existant

		PluginManager pm = Bukkit.getServer().getPluginManager();

		if(pm.isPluginEnabled("Lockette")){
			pm.registerEvents(new ILListener(this), this);
			log.info("Lockette detected, enabling Lockette support");
		}
		else if(pm.isPluginEnabled("LockettePro")){
			pm.registerEvents(new ILPListener(this), this);
			log.info("LockettePro detected, enabling LockettePro support");
		}

		getCommand("inactivelockette").setExecutor(new ILCommandHandler(this));//Firing commands listener

		setupEconomy();//Setting up the economy
		if (!setupEconomy() && getConfig().getBoolean("useEconomy")) {//If economy is turned on
			//But no vault is found it will warn the user
			log.severe(String.format("[%s] - No Vault dependency found!", getDescription().getName()));
			return;
		}

		log.info(getDescription().getName() + " v" + getDescription().getVersion() + " has been enabled");//Logging to console the enabling of IL

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			//Failed to submit the stats
		}
		
		//Update Checking
		if(getConfig().getBoolean("checkForUpdates")){
			pm.registerEvents(new ILJoinListener(this), this);
			final ILMain plugin = this;
			Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable () {
				public void run() {

					updateChecker = new ILUpdateChecker(plugin);

					if(updateChecker.updateNeeded()){
						log.info(ChatColor.stripColor(ILConfigHandler.mes("onPluginLoad.updateAvailable")+" "+updateChecker.getVersion()));
						log.info(ChatColor.stripColor(ILConfigHandler.mes("onPluginLoad.updateAvailableLink")+" "+updateChecker.getLink()));
					}
				}
			},20L);
		}
	}


	@Override
	public void onDisable(){
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