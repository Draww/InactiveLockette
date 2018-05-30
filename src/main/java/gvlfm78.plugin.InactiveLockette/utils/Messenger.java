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

    public static void sendPlayerMessage(CommandSender sender, String path, String... replacements){
        String mes = locale.getString(path);
        performReplacements(mes, replacements);

        if(mes != null && !mes.isEmpty())
            mes = prefix + " " + colourise(mes);
        else
            mes = "&4[InactiveLockette] Message String " + path + " is null!";

        sender.sendMessage(mes);
    }

    public static void broadcastMessage(String path, String... replacements){
        String message = locale.getString(path);
        performReplacements(message, replacements);
        Bukkit.broadcastMessage(message);
    }

    public static String getLocalisedMessage(String path, String... replacements){
        String message = locale.getString(path);
        performReplacements(message, replacements);
        return message;
    }

    private static void performReplacements(String message, String[] replacements){
        for(int i = 0; i < replacements.length; i+=2){
            String toReplace = replacements[i];
            String replacement = replacements[i+1];
            if(toReplace != null && replacement != null)
                message.replaceAll(toReplace, replacement);
        }
    }

    private static String colourise(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
