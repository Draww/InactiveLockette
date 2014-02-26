package gvlfm78.plugin.InactiveLockette;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class InactiveLocketteCommandHandler implements CommandExecutor {

    private InactiveLockette plugin;

    public InactiveLocketteCommandHandler(InactiveLockette iLCH)
    {
        this.plugin = iLCH;
    }

    InactiveLocketteConfigHandler ilch = InactiveLocketteConfigHandler.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

        if(cmd.getName().equalsIgnoreCase("InactiveLockette") || cmd.getName().equalsIgnoreCase("IL")){

            if(sender.hasPermission("inactivelockette.admin") || sender.isOp() || sender.hasPermission("inactivelockette.*")){

                if(!(args.length == 0)){

                    if(args[0].equalsIgnoreCase("help")){
                        onHelp(sender, commandLabel, args);
                        return true;
                    }
                    else if(args[0].equalsIgnoreCase("version")){
                        onVersion(sender, commandLabel, args);
                        return true;
                    }

                    else if(args[0].equalsIgnoreCase("reload")){
                        onReload(sender, commandLabel, args);
                    }
                    else
                        onHelp(sender, commandLabel, args);
                    return false;
                }
                else
                    onHelp(sender, commandLabel, args);
                return false;
            }
            sender.sendMessage(ChatColor.DARK_RED + plugin.getConfig().getString("onCommand.messageNoPermission"));
            return false;
        }
        return false;
    }


    public void onHelp(CommandSender sender, String commandLabel, String[] args){
        sender.sendMessage(ChatColor.GOLD + "--InactiveLockette plugin by gvlfm78--");
        sender.sendMessage(ChatColor.GREEN + "/InactiveLockette help " + ChatColor.GOLD + "Displays help page");
        sender.sendMessage(ChatColor.GREEN + "/InactiveLockette reload " + ChatColor.GOLD + "Reloads config file");
        sender.sendMessage(ChatColor.GREEN + "/InactiveLockette version " + ChatColor.GOLD + "Displays plugin version");
    }

    public void onReload(CommandSender sender, String commandLabel, String[] args){
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("settingsChat.prefix") + " " + plugin.getConfig().getString("onCommand.messageReloadConfig"));
    }

    public void onVersion(CommandSender sender, String commandLabel, String[] args){
        sender.sendMessage(ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getConfig().getString("onCommand.messageVersion") + " " + plugin.getDescription().getVersion());
    }

}