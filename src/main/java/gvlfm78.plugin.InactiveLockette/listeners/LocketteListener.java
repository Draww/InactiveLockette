package gvlfm78.plugin.InactiveLockette.listeners;

import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.yi.acru.bukkit.Lockette.Lockette;

import java.util.UUID;

public class LocketteListener extends ILListener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event){

        if(event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if(block.getType() != Material.WALL_SIGN) return;
        //Player Left Clicked a wall sign

        Sign sign = (Sign) block.getState();
        String line1 = sign.getLine(0);

        if(line1.equalsIgnoreCase("[Private]") || //The first line of the sign contains [Private]
                line1.equalsIgnoreCase("[" + ILConfigHandler.config.getString("settingsChat.firstLine") + "]")){ //The first line of the sign contains custom [Private] text

            Player player = event.getPlayer();
            UUID ownerUUID = Lockette.getProtectedOwnerUUID(block);
            String ownerName = sign.getLine(1);

            //Checking the blacklist
            if(isBlackListed(ownerUUID)){
                Messenger.sendCommandSenderMessage(player, "onPunch.noPermission");
                return;
            }

            if(!player.isOp() && !(ILConfigHandler.config.getBoolean("permissionToOpenLocks") &&
                    player.hasPermission("inactivelockette.player") ||
                    player.hasPermission("inactivelockette.*") ||
                    player.hasPermission("inactivelockette.admin"))
                    ){
                Messenger.sendCommandSenderMessage(player, "onPunch.noPermission");
                return;
            }

            if(ownerUUID != null && !ownerUUID.toString().isEmpty()){//Use UUIDs
                if(isOverlyInactive(ownerUUID)){
                    //Owner is still active, tell user, and tell time remaining
                    ownerStillActive(player, getInactivityDays(ownerUUID));
                    return;
                }

                //Lock owner is inactive
                //If capitalism is enabled and they have the munniez, make them pay
                makeUserPay(player);

                //Empty the container
                Block attachedBlock = Lockette.getSignAttachedBlock(block);
                clearContainer(attachedBlock, player);

                //Break sign
                block.breakNaturally();

                //Broadcast to whole server
                broadcast(attachedBlock, player.getName(), ownerName);
            } else {//Don't use UUIDs
                if(isOverlyInactive(ownerName)){
                    makeUserPay(player);
                    Block attachedBlock = Lockette.getSignAttachedBlock(block);
                    clearContainer(attachedBlock, player);
                    block.breakNaturally();
                    broadcast(attachedBlock, player.getName(), ownerName);
                } else {
                    ownerStillActive(player, getInactivityDays(ownerName));
                }
            }
        }
    }

    //Get active player UUIDs from sign
	/*	private ArrayList<OfflinePlayer> getActivePlayersUUID(Sign s){
		String[] lines = s.getLines();
		ArrayList<OfflinePlayer> names = new ArrayList<OfflinePlayer>();

		for(int i = 0; i < lines.length; i++){
			UUID uuid = getUUIDFromMeta(s,i);
			if(isOverlyInactive(uuid)){
				names.add(Bukkit.getOfflinePlayer(uuid));
			}
		}
		return names;
	}

	//Get active player names from sign
	@SuppressWarnings("deprecation")
	private ArrayList<OfflinePlayer> getActivePlayersNames(Sign s){

		String[] lines = s.getLines();
		ArrayList<OfflinePlayer> names = new ArrayList<OfflinePlayer>();

		for(String line : lines){
			if(isOverlyInactive(line)){
				names.add(Bukkit.getOfflinePlayer((line)));
			}
		}
		return names;
	}*/
}