package gvlfm78.plugin.InactiveLockette.listeners;

import gvlfm78.plugin.InactiveLockette.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.ILMain;
import gvlfm78.plugin.InactiveLockette.Updater;
import gvlfm78.plugin.InactiveLockette.Updater.UpdateResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class ILJoinListener implements Listener {

    private ILMain plugin;
    private final File pluginFile;

    public ILJoinListener(ILMain plugin, File pluginFile){
        this.plugin = plugin;
        this.pluginFile = pluginFile;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e){

        if(plugin.getConfig().getBoolean("checkForUpdates")){
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                public void run(){

                    Updater updater = new Updater(plugin, 52457, pluginFile, Updater.UpdateType.NO_DOWNLOAD, false);

                    if(updater.getResult().equals(UpdateResult.UPDATE_AVAILABLE)){
                        Player p = e.getPlayer();
                        p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailable") + " " + updater.getLatestName().replaceAll("[A-Za-z\\s]", ""));
                        p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailableLink") + " https://dev.bukkit.org/projects/inactive-lockette/files");
                    }
                }
            }, 20L);
        }
    }

}
