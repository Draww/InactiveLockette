package gvlfm78.plugin.InactiveLockette;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ILCommandHandler implements CommandExecutor {

    private ILMain plugin;
    private final ILConfigHandler conf;
    public ILCommandHandler(ILMain plugin){
    	this.plugin = plugin;
    	conf = new ILConfigHandler(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

        if(cmd.getLabel().equalsIgnoreCase("InactiveLockette") || cmd.getLabel().equalsIgnoreCase("IL")){

            if(sender.hasPermission("inactivelockette.admin") || sender.isOp() || sender.hasPermission("inactivelockette.*")){
            	if(args.length<1){
            		onDefault(sender); 
            		return false;
            	}
            	
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
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "InactiveLockette plugin by gvlfm78 v" + plugin.getDescription().getVersion() + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "Bukkit Page: " + ChatColor.GOLD  + "http://dev.bukkit.org/bukkit-plugins/inactive-lockette ==");
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "Spigot Page: " + ChatColor.GOLD  + "https://www.spigotmc.org/resources/inactive-lockette.25644 ==");
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "GitHub Page: " + ChatColor.GOLD  + "https://github.com/gvlfm78/InactiveLockette ==");
        
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "/il help " + ChatColor.GOLD + "Displays help page" + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "/il reload " + ChatColor.GOLD + "Reloads config file" + ChatColor.GOLD  + " ==");
        sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.GREEN + "/il version " + ChatColor.GOLD + "Displays plugin version" + ChatColor.GOLD  + " ==");
    }
    
    public void onHelp(CommandSender sender){
    	sender.sendMessage(ChatColor.GREEN + "InactiveLockette allows users to open locks of inactive players by right-clicking on the sign");
    	sender.sendMessage(ChatColor.GREEN + "InactiveLockette offers many configuration options to make it fit your server");
    	sender.sendMessage(ChatColor.GREEN + "You can charge players for opening locks, or make the opened containers empty");
    	sender.sendMessage(ChatColor.GREEN + "Lots more can be found in the settings!");
    }

    public void onReload(CommandSender sender){
        conf.reloadConfigs();
        sender.sendMessage(ILConfigHandler.mes("onCommand.reloadConfig"));
    }

    public void onVersion(CommandSender sender){
        sender.sendMessage(ChatColor.GREEN + plugin.getDescription().getName() + " " + ILConfigHandler.mesnopre("onCommand.version") + " " + plugin.getDescription().getVersion());
    }

}