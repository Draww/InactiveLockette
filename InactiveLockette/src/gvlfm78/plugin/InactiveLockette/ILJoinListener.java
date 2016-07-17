package gvlfm78.plugin.InactiveLockette;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ILJoinListener implements Listener{

	private ILMain plugin;
	public ILJoinListener(ILMain plugin){
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent e){

		if(plugin.getConfig().getBoolean("checkForUpdates")){
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable () {
				public void run() {

					ILUpdateChecker updateChecker = new ILUpdateChecker(plugin);

					if(updateChecker.updateNeeded()){
						Player p = e.getPlayer();
						p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailable")+" "+updateChecker.getVersion());
						p.sendMessage(ILConfigHandler.mes("onPluginLoad.updateAvailableLink")+" "+updateChecker.getLink());
					}
				}
			},20L);
		}
	}

}
