package gvlfm78.plugin.InactiveLockette;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ILCommandHandler implements CommandExecutor {

    private ILMain plugin;
    private FileConfiguration config;
    private final ILConfigHandler conf;
    public ILCommandHandler(ILMain plugin){
    	this.plugin = plugin;
    	this.config = plugin.getConfig();
    	conf = new ILConfigHandler(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

        if(cmd.getLabel().equalsIgnoreCase("InactiveLockette") || cmd.getLabel().equalsIgnoreCase("IL")){

            if(sender.hasPermission("inactivelockette.admin") || sender.isOp() || sender.hasPermission("inactivelockette.*")){

            	switch(args[0].toLowerCase()){
            	case "help": onHelp(sender); break;
            	case "version": onVersion(sender); break;
            	case "reload": onReload(sender); break;
            	default: onDefault(sender);
            	}
                return false;
            }
            sender.sendMessage(ChatColor.DARK_RED + plugin.getConfig().getString("onCommand.messageNoPermission"));
            return false;
        }
        return false;
    }

    public void onDefault(CommandSender sender){
        sender.sendMessage(ChatColor.GOLD  + "== " + ChatColor.GREEN + "InactiveLockette plugin by gvlfm78 v" + plugin.getDescription().getVersion() + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD  + "== " + ChatColor.GREEN + "Bukkit Page: " + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD  + "== " + ChatColor.GREEN + "Spigot Page: " + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD  + "== " + ChatColor.GREEN + "GitHub Page: " + ChatColor.GOLD  + " ==");
        
        sender.sendMessage(ChatColor.GOLD  + "== " + ChatColor.GREEN + "/il help " + ChatColor.GOLD + "Displays help page" + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD  + "== " + ChatColor.GREEN + "/il reload " + ChatColor.GOLD + "Reloads config file" + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "/il version " + ChatColor.GOLD + "Displays plugin version" + ChatColor.GOLD  + " ==");
    }
    
    public void onHelp(CommandSender sender){
    	sender.sendMessage(ChatColor.GREEN+"InactiveLockette allows users to open locks of inactive players by right-clicking on the sign");
    	sender.sendMessage(ChatColor.GREEN+"InactiveLockette offers many configuration options to make it fit your server");
    	sender.sendMessage(ChatColor.GREEN+"You can charge players for opening locks, or make the opened containers empty");
    	sender.sendMessage(ChatColor.GREEN+"Lots more can be found in the settings!");
    }

    public void onReload(CommandSender sender){
        conf.reloadConfigs();
        sender.sendMessage(ChatColor.GREEN + config.getString("settingsChat.prefix") + " " + config.getString("onCommand.messageReloadConfig"));
    }

    public void onVersion(CommandSender sender){
        sender.sendMessage(ChatColor.GREEN + plugin.getDescription().getName() + " " + config.getString("onCommand.messageVersion") + " " + plugin.getDescription().getVersion());
    }

}