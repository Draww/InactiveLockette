package gvlfm78.plugin.InactiveLockette;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ILConfigHandler {

	private ILMain plugin;
	public static  FileConfiguration config;
	private static YamlConfiguration locale;

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
	public boolean useUUIDs(){
		File file = new File("plugins"+File.separator+"Lockette"+File.separator+"config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		return config.getBoolean("enable-uuid-support");
	}
	//Reload methods
	public void reloadConfigs(){
		setupConfigYML();
		plugin.reloadConfig();
		config = plugin.getConfig();
		setupLocale();
		locale = getLocale();
	}
}