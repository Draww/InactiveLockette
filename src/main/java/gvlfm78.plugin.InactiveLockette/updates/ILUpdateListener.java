package gvlfm78.plugin.InactiveLockette.updates;

import gvlfm78.plugin.InactiveLockette.ILMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class ILUpdateListener implements Listener {


    private ILMain plugin;
    private final File pluginFile;

    public ILUpdateListener(ILMain plugin, File pluginFile){
        this.plugin = plugin;
        this.pluginFile = pluginFile;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        final Player p = e.getPlayer();
        if(p.hasPermission("inactivelockette.*") || p.hasPermission("inactivelockette.admin")){
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {

                ILUpdateChecker updateChecker = new ILUpdateChecker(plugin, pluginFile);

                updateChecker.sendUpdateMessages(p);
            },20L);
        }
    }
}
