package gvlfm78.plugin.InactiveLockette.utils;

import gvlfm78.plugin.InactiveLockette.ILMain;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.Charset;

public class ILConfigHandler {

    private static final String[] LANGUAGES = {"enGB", "itIT", "frFR"};

    private static ILMain plugin;
    public static FileConfiguration config;
    private static YamlConfiguration locale;

    public static void initialise(ILMain plugin){
        ILConfigHandler.plugin = plugin;
        reloadConfigs();
    }

    public static void reloadConfigs(){
        //config.yml
        setupConfigYML();
        plugin.reloadConfig();
        config = plugin.getConfig();
        upgradeConfig();
        //Locale
        setupLocale();
    }

    private static YamlConfiguration getYML(File file){
        YamlConfiguration config = new YamlConfiguration();
        FileInputStream fileinputstream;

        try{
            fileinputstream = new FileInputStream(file);
            config.load(new InputStreamReader(fileinputstream, Charset.forName("UTF-8")));
        } catch(FileNotFoundException ignored){

        } catch(IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
        return config;
    }

    private static void saveLanguageFile(String code){
        plugin.saveResource("locale-" + code + ".yml", false);
        File badLoc = getFile("locale-" + code);
        badLoc.renameTo(getLocaleFile());
        plugin.getLogger().info(code + " Language strings generated");
    }

    private static void setupConfigYML(){
        if(!getConfigYMLFile().exists())
            plugin.saveResource("config.yml", false);
    }

    private static void upgradeConfig(){
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml")));

        if(config.getInt("config-version") == defaultConfig.getInt("config-version")) return;
        Messenger.sendConsoleMessage("Config versions do not match, backing up config and saving a new copy...");

        File backupFile = getFile("config_backup.yml");
        backupFile.delete();
        getConfigYMLFile().renameTo(backupFile);
        setupConfigYML();
    }

    private static String getLanguage(){
        return config.getString("language");
    }

    private static void setupLocale(){
        String langCode = getLanguage();
        File localeFile = getLocaleFile();
        if(localeFile.exists()){
            loadLocale();
            int version = locale.getInt("version");
            int latest = getLatestLocaleVersion(langCode);

            if(latest != version){//upgrade locale
                backupLocale(localeFile);
            } else return; //locale is up to date
        }

        if(!ArrayUtils.contains(LANGUAGES, langCode)){
            Messenger.sendConsoleMessage("Invalid language code in config.yml! Loading up english locale");
            langCode = "enGB";
        }
        saveLanguageFile(langCode);
        loadLocale();
    }

    private static void backupLocale(File localeFile){
        Messenger.sendConsoleMessage("Locale versions do not match, backing up locale and saving a new copy...");
        File backupFile = getFile("locale_backup.yml");
        backupFile.delete();
        localeFile.renameTo(backupFile);
    }

    private static int getLatestLocaleVersion(String langCode){
        return YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("locale-" + langCode + ".yml"))).getInt("version");
    }

    static YamlConfiguration getLocale(){
        return locale;
    }
    static String getPrefix(){
        return locale.getString("settingsChat.prefix");
    }
    private static void loadLocale(){
        locale = getYML(getLocaleFile());
    }

    private static File getFile(String name){
        return new File(plugin.getDataFolder() + File.separator + name + ".yml");
    }

    public YamlConfiguration getYML(String name){
        return getYML(getFile(name));
    }

    private static File getLocaleFile(){
        return getFile("locale");
    }

    private static File getConfigYMLFile(){
        return getFile("config");
    }
}