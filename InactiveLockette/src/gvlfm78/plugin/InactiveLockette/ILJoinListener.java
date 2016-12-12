package gvlfm78.plugin.InactiveLockette;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import gvlfm78.plugin.InactiveLockette.Updater.UpdateResult;

public class ILJoinListener implements Listener{

	private ILMain plugin;
	private final File pluginFile;
	
	public ILJoinListener(ILMain plugin, File pluginFile){
		this.plugin = plugin;
		this.pluginFile = pluginFile;
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent e){

		if(plugin.getConfig().getBoolean("checkForUpdates")){
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable () {
				public void run() {

					Updater updater = new Updater(plugin, 52457, pluginFile, Updater.UpdateType.NO_DOWNLOAD, false);

					if(updater.getResult().equals(UpdateResult.UPDATE_AVAILABLE)){
						Player p = e.getPlayer();
						p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailable") + " " + updater.getLatestName().replaceAll("[A-Za-z\\s]", ""));
						p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailableLink") + " " + updater.getLatestFileLink());
					}
				}
			},20L);
		}
	}

}
