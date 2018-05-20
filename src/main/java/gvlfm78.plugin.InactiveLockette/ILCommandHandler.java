package gvlfm78.plugin.InactiveLockette;

import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ILCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

        if(!cmd.getLabel().equalsIgnoreCase("InactiveLockette") && !cmd.getLabel().equalsIgnoreCase("IL")) return false;

        if(!sender.hasPermission("inactivelockette.admin") && !sender.hasPermission("inactivelockette.*"))
            Messenger.sendCommandSenderMessage(sender,"onCommand.noPermission");

        if(args.length < 1){
            onDefault(sender);
            return false;
        }

        switch(args[0].toLowerCase()){
            case "help":
                onHelp(sender);
                break;
            case "version":
                onVersion(sender);
                break;
            case "reload":
                onReload(sender);
                break;
            default:
                onDefault(sender);
        }
        return false;
    }

    private void onDefault(CommandSender sender){
        Messenger.sendCommandSenderMessage(sender,"&6== &2InactiveLockette plugin by gvlfm78 v" + ILMain.getDescriptionFile().getVersion() + "&6 ==");
        Messenger.sendCommandSenderMessage(sender, "&6== " + "&2Bukkit: " + "&6http://dev.bukkit.org/bukkit-plugins/inactive-lockette ==");
        Messenger.sendCommandSenderMessage(sender, "&6== " + "&2Spigot: " + "&6https://www.spigotmc.org/resources/inactive-lockette.25644 ==");
        Messenger.sendCommandSenderMessage(sender, "&6== " + "&2GitHub: " + "&6https://github.com/gvlfm78/InactiveLockette ==");

        Messenger.sendCommandSenderMessage(sender, "&6== " + "&2/il help " + "&6Displays help page" + "&6 ==");
        Messenger.sendCommandSenderMessage(sender, "&6== " + "&2/il reload " + "&6Reloads config file" + "&6 ==");
        Messenger.sendCommandSenderMessage(sender, "&6== " + "&2/il version " + "&6Displays plugin version" + "&6 ==");
    }

    private void onHelp(CommandSender sender){
        Messenger.sendCommandSenderMessage(sender, "&2InactiveLockette allows users to open locks of inactive players by right-clicking on the sign");
        Messenger.sendCommandSenderMessage(sender, "&2InactiveLockette offers many configuration options to make it fit your server");
        Messenger.sendCommandSenderMessage(sender, "&2You can charge players for opening locks, or make the opened containers empty");
        Messenger.sendCommandSenderMessage(sender, "&2Lots more can be found in the settings!");
    }

    private void onReload(CommandSender sender){
        ILConfigHandler.reloadConfigs();
        Messenger.sendCommandSenderMessage(sender, "onCommand.reloadConfig");
    }

    private void onVersion(CommandSender sender){
        Messenger.sendCommandSenderMessage(sender, "&2" + ILMain.getDescriptionFile().getName() + " v" + ILMain.getDescriptionFile().getVersion());
    }

}