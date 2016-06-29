package gvlfm78.plugin.InactiveLockette;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
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
		conf.setupLocale();//Creates locale file if not existant

		getServer().getPluginManager().registerEvents(new ILListener(this), this);//Firing event listener
		getCommand("inactivelockette").setExecutor(new ILCommandHandler(this));//Firing commands listener
		setupEconomy();//Setting up the economy
		if (!setupEconomy() && getConfig().getBoolean("useEconomy")) {//If economy is turned on
			//But no vault is found it will warn the user
			log.severe(String.format("[%s] - No Vault dependency found!", getDescription().getName()));
			return;
		}

		log.info(getDescription().getName() + " v" + getDescription().getVersion() + " has been enabled");//Logging to console the enabling of IL

		//Update Checking
		if(getConfig().getBoolean("checkForUpdates")){
			updateChecker = new ILUpdateChecker("http://dev.bukkit.org/bukkit-plugins/inactive-lockette/files.rss",this);

			if(updateChecker.updateNeeded()){
				log.info(getConfig().getString("onPluginLoad.updateAvailable")+" "+this.updateChecker.getVersion());
				log.info(getConfig().getString("onPluginLoad.updateAvailableLink")+" "+this.updateChecker.getLink());
			}
		}

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			//Failed to submit the stats
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