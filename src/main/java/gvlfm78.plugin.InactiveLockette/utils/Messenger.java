package gvlfm78.plugin.InactiveLockette.utils;

import gvlfm78.plugin.InactiveLockette.ILMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messenger {

    private static ILMain plugin;
    private static YamlConfiguration locale;
    private static String prefix;

    public static void initialise(ILMain plugin){
        Messenger.plugin = plugin;
        locale = ILConfigHandler.getLocale();
        prefix = colourise(ILConfigHandler.getPrefix());
    }

    public static void sendConsoleMessage(String message){
        plugin.getLogger().info(message);
    }

    public static void sendConsoleErrorMessage(String error){
        plugin.getLogger().severe("[InactiveLockette] " + error);
    }

    public static void sendPlayerMessage(CommandSender sender, String message, String... replacements){
        sender.sendMessage(colourise(performReplacements(message, replacements)));
    }

    public static void sendLocalisedMessage(CommandSender sender, String path, String... replacements){
        String mes = locale.getString(path);

        if(mes != null && !mes.isEmpty())
            mes = prefix + " " + mes;
        else
            mes = "&4[InactiveLockette] Message String " + path + " is null!";

        sendPlayerMessage(sender,mes,replacements);
    }

    public static void broadcastMessage(String path, String... replacements){
        String mes = locale.getString(path);
        mes = colourise(performReplacements(prefix + " " + mes, replacements));
        Bukkit.broadcastMessage(mes);
    }

    public static String getLocalisedMessage(String path, String... replacements){
        return performReplacements(locale.getString(path), replacements);
    }

    private static String performReplacements(String message, String[] replacements){
        for(int i = 0; i < replacements.length; i+=2){
            String toReplace = replacements[i];
            String replacement = replacements[i+1];
            if(toReplace != null && replacement != null)
                message = message.replaceAll(toReplace, replacement);
        }
        return message;
    }

    private static String colourise(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
