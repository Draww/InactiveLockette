package gvlfm78.plugin.InactiveLockette;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.Charset;

public class ILConfigHandler {

	private ILMain plugin;
	public static  FileConfiguration config;
	private static YamlConfiguration locale;
	private final int  CONFIG_VERSION = 1;

	public ILConfigHandler(ILMain plugin){
		this.plugin = plugin;
		locale = getLocale();
		config = plugin.getConfig();
	}

	public static String mes(String path){
		String mes =  locale.getString(path);
		if(mes!=null&&!mes.isEmpty())
			return (locale.getString("settingsChat.prefix") +" "+ mes).replaceAll("(?i)&([a-fk-r0-9])", "\u00A7$1");
		else
			return ChatColor.DARK_RED+"[InactiveLockette] Message String "+path+" is null!";
	}
	public static String mesnopre(String path){
		String mes =  locale.getString(path);
		if(mes!=null&&!mes.isEmpty())
			return (mes).replaceAll("(?i)&([a-fk-r0-9])", "\u00A7$1");
		else
			return ChatColor.DARK_RED+"[InactiveLockette] Message String "+path+" is null!";
	}

	public void saveConfiguration(File file, YamlConfiguration config){

		try{
			Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
			fileWriter.write(config.saveToString());
			fileWriter.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public YamlConfiguration getYML(File file){
		YamlConfiguration config = new YamlConfiguration();
		FileInputStream fileinputstream;

		try {
			fileinputstream = new FileInputStream(file);
			config.load(new InputStreamReader(fileinputstream, Charset.forName("UTF-8")));
		} catch (FileNotFoundException e){
			System.out.print("");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		return config;
	}
	public void saveLanguageFile(String code){
		plugin.saveResource("locale-"+code+".yml", false);
		File badLoc = getFile("locale-"+code);
		badLoc.renameTo(getLocaleFile());
		plugin.getLogger().info(code+" Language strings generated");
	}

	//Setting up methods
	public void setupConfigYML(){
		if(!getConfigYMLFile().exists())
			plugin.saveResource("config.yml", false);
	}
	public void upgradeConfig(){
		if(getYML(getConfigYMLFile()).getInt("config-version")!=CONFIG_VERSION){
			plugin.getLogger().info("Config versions do not match, backing up config and saving a new copy...");
			File bakFile = getFile("config_bak");
			bakFile.delete();
			getConfigYMLFile().renameTo(bakFile);
			setupConfigYML();
		}
	}
	public String getLanguage(){
		return config.getString("language");
	}
	public void setupLocale(){
		if(!getLocaleFile().exists()){
			String lang = getLanguage();
			switch(lang){
			case "enGB": case "itIT": case "frFR":
				saveLanguageFile(lang);
				break;
			default: plugin.getLogger().info("Invalid language code in config.yml! Loading up english locale");
			saveLanguageFile("enGB");
			}
		}
	}

	//Getters
	public YamlConfiguration getLocale(){
		return getYML(getLocaleFile());
	}
	public File getFile(String name){
		return new File(plugin.getDataFolder()+File.separator+name+".yml");
	}
	public YamlConfiguration getYML(String name){
		return getYML(getFile(name));
	}
	public File getLocaleFile(){
		return getFile("locale");
	}
	public File getConfigYMLFile(){
		return getFile("config");
	}
	//Reload methods
	public void reloadConfigs(){
		setupConfigYML();
		upgradeConfig();
		plugin.reloadConfig();
		config = plugin.getConfig();
		setupLocale();
		locale = getLocale();
	}
}