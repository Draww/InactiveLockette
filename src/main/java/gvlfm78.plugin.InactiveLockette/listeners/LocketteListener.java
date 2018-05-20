package gvlfm78.plugin.InactiveLockette.listeners;

import gvlfm78.plugin.InactiveLockette.ILMain;
import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.yi.acru.bukkit.Lockette.Lockette;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class LocketteListener implements Listener {

    private ILMain plugin;

    public LocketteListener(ILMain plugin){
        this.plugin = plugin;
    }

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

    private void clearContainer(Block block, Player player){
        //Block is block the sign was attached on
        if(ILConfigHandler.config.getBoolean("clearItems")){
            String mat = block.getType().toString().toLowerCase();
            switch(mat){//Chest, trapped chest, furnace, dispenser, dropper, brewing stand, hopper,
                case "chest":
                case "trapped_chest":
                case "furnace":
                case "dispenser":
                case "dropper":
                case "brewing_stand":
                case "hopper":
                    InventoryHolder ih = (InventoryHolder) block.getState();
                    ih.getInventory().clear();
                    Messenger.sendCommandSenderMessage(player, "onUnlock.cleared");
                    break;
            }
        }
    }

    private void broadcast(Block block, String breakerName, String ownerName){
        // Block is the block that was unlocked
        if(!ILConfigHandler.config.getBoolean("broadcast")){
            return;
        }

        if(block == null){
            plugin.getLogger().severe("The attachedBlock was null!");
        } else {
            String blockName = block.getType().name().toLowerCase().replaceAll("_", " ");

            Location l = block.getLocation();

            int x = (int) l.getX();

            int y = (int) l.getY();

            int z = (int) l.getZ();

            String location = x + ", " + y + ", " + z;

            Messenger.broadcastMessage("messages.broadcast",
                    "%block%", blockName, "%owner%", ownerName, "%breaker%", breakerName, "%coordinates", location);
        }
    }

    private void ownerStillActive(Player player, long inactivityDays){
        Messenger.sendCommandSenderMessage(player, "onPunch.active");
        if(ILConfigHandler.config.getBoolean("onClickDisplayDays"))
            Messenger.sendCommandSenderMessage(player, "onPunch.inactive","%inactivedays%", Long.toString(inactivityDays));
        if(ILConfigHandler.config.getBoolean("onClickDisplayDaysToWait")){
            long daysToWait = ILConfigHandler.config.getInt("daysOfInactivity") - inactivityDays;
            Messenger.sendCommandSenderMessage(player, "onPunch.daysToWait","%daystowait%", Long.toString(daysToWait));
        }
    }

    private void makeUserPay(Player p){
        if(!ILConfigHandler.config.getBoolean("useEconomy") || !(ILMain.econ.isEnabled() && ILMain.econ.hasAccount(p)))
            return;
        double cost = ILConfigHandler.config.getInt("cost");
        double balance = ILMain.econ.getBalance(p);
        if(ILMain.econ.has(p, cost)){//If player has the munniez
            ILMain.econ.withdrawPlayer(p, cost);//Take the munniez
            DecimalFormat df = new DecimalFormat("0.00");
            String moneyCost = df.format(cost);
            String newBalance = df.format(ILMain.econ.getBalance(p));
            Messenger.sendCommandSenderMessage(p, "messages.moneyWithdraw","%cost%", moneyCost,"%balance%", newBalance);
        } else {//Player is poor
            DecimalFormat df = new DecimalFormat("0.00");
            String moneyNeeded = df.format(cost - balance);
            String moneyCost = df.format(cost);
            String newBalance = df.format(ILMain.econ.getBalance(p));
            Messenger.sendCommandSenderMessage(p, "messages.moneyTransactionFailed","%cost%", moneyCost,"%balance%", newBalance,"%needed%", moneyNeeded);
        }
    }

    private long getInactivityDays(OfflinePlayer op){
        return getInactivityTime(op) / 86400000;
    }

    private long getInactivityDays(UUID uuid){
        return getInactivityDays(Bukkit.getOfflinePlayer(uuid));
    }

    @SuppressWarnings("deprecation")
    private long getInactivityDays(String name){
        return getInactivityDays(Bukkit.getOfflinePlayer(name));
    }

    private long getInactivityTime(OfflinePlayer op){
        return System.currentTimeMillis() - op.getLastPlayed();
    }

    private boolean isOverlyInactive(OfflinePlayer op){
        return getInactivityTime(op) / 86400000 > ILConfigHandler.config.getLong("daysOfInactivity");
    }

    @SuppressWarnings("deprecation")
    private boolean isOverlyInactive(String s){
        return isOverlyInactive(Bukkit.getOfflinePlayer(s));
    }

    private boolean isOverlyInactive(UUID uuid){
        return isOverlyInactive(Bukkit.getOfflinePlayer(uuid));
    }

    private boolean isBlackListed(UUID ownerUUID){
        if(plugin.getConfig().getList("list") != null && !plugin.getConfig().getList("list").isEmpty()){
            List<?> list = plugin.getConfig().getList("list");

            return list.contains(ownerUUID) || list.contains(ownerUUID.toString());
        }
        return false;
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