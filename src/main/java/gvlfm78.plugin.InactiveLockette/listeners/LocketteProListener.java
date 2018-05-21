package gvlfm78.plugin.InactiveLockette.listeners;

import gvlfm78.plugin.InactiveLockette.ILMain;
import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import me.crafter.mc.lockettepro.LocketteProAPI;
import me.crafter.mc.lockettepro.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.UUID;

public class LocketteProListener extends ILListener {

    private ILMain plugin;

    public LocketteProListener(ILMain plugin){
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if(block.getType() != Material.WALL_SIGN) return; //Player Left Clicked a wall sign
        Sign sign = (Sign) block.getState();
        String line1 = sign.getLine(0);

        if(line1.equalsIgnoreCase("[Private]") || //The first line of the sign contains [Private]
                line1.equalsIgnoreCase("[" + ILConfigHandler.config.getString("settingsChat.firstLine") + "]")){ //The first line of the sign contains custom [Private] text

            Player player = event.getPlayer();
            String line2 = sign.getLine(1);
            String ownerUUIDString = Utils.getUuidFromLine(line2);

            if(!player.isOp() && !(ILConfigHandler.config.getBoolean("permissionToOpenLocks") &&
                    player.hasPermission("inactivelockette.player") ||
                    player.hasPermission("inactivelockette.*") ||
                    player.hasPermission("inactivelockette.admin"))
                    ){
                Messenger.sendCommandSenderMessage(player, "onPunch.noPermission");
                return;
            }

            if(ownerUUIDString != null && !ownerUUIDString.isEmpty()){
                //Use UUIDs
                UUID ownerUUID = UUID.fromString(ownerUUIDString);
                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                List<?> list = plugin.getConfig().getList("list");
                if(list == null || list.contains(ownerName) || list.contains(ownerUUID.toString())){
                    Messenger.sendCommandSenderMessage(player, "onPunch.noPermission");
                    return;
                }

                if(isOverlyInactive(ownerUUID)){
                    //Lock owner is inactive
                    //If capitalism is enabled and they have the munniez, make them pay
                    makeUserPay(player);

                    //Empty the container
                    Block attachedBlock = LocketteProAPI.getAttachedBlock(block);
                    clearContainer(attachedBlock, player);

                    //Break sign
                    block.breakNaturally();

                    //Broadcast to whole server
                    broadcast(attachedBlock, player.getName(), ownerName);
                } else
                    ownerStillActive(player, getInactivityDays(ownerUUID));
                //Owner is still active
                //Tell user, and tell time remaining
            } else {//Don't use UUIDs
                String ownerName = Utils.getUsernameFromLine(line2);

                if(plugin.getConfig().getList("list").contains(ownerName)){
                    Messenger.sendCommandSenderMessage(player, "onPunch.noPermission");
                    return;
                }

                if(!isOverlyInactive(ownerName)){
                    ownerStillActive(player, getInactivityDays(ownerName));
                    return;
                }

                makeUserPay(player);
                Block attachedBlock = LocketteProAPI.getAttachedBlock(block);
                clearContainer(attachedBlock, player);
                block.breakNaturally();
                broadcast(attachedBlock, player.getName(), ownerName);
            }
        }
    }
}
