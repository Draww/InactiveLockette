package gvlfm78.plugin.InactiveLockette.listeners;

import org.bukkit.event.Listener;

import java.io.File;

public class ILJoinListener implements Listener {

    private final File pluginFile;

    public ILJoinListener(File pluginFile){
        this.pluginFile = pluginFile;
    }

   /* @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e){
        if(!plugin.getConfig().getBoolean("checkForUpdates")) return;
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {

            Updater updater = new Updater(plugin, 52457, pluginFile, Updater.UpdateType.NO_DOWNLOAD, false);

            if(updater.getResult().equals(UpdateResult.UPDATE_AVAILABLE)){
                Player p = e.getPlayer();
                p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailable") + " " + updater.getLatestName().replaceAll("[A-Za-z\\s]", ""));
                p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailableLink") + " https://dev.bukkit.org/projects/inactive-lockette/files");
            }
        }, 20L);
    }*/
}
