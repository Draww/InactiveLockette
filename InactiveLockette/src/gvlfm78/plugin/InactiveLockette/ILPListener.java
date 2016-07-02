package gvlfm78.plugin.InactiveLockette;

import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import me.crafter.mc.lockettepro.LocketteProAPI;
import me.crafter.mc.lockettepro.Utils;

public class ILPListener implements Listener{

	private ILMain plugin;

	public ILPListener(ILMain plugin){
		this.plugin = plugin;
	}
	@EventHandler(ignoreCancelled=true, priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			Block block = event.getClickedBlock();
			if ((block.getState() instanceof Sign)&&(block.getType() == Material.WALL_SIGN)){ //Player Left Clicked a wall sign
				Sign sign = (Sign) block.getState();
				String line1 = sign.getLine(0);
				if (line1.equalsIgnoreCase("[Private]") ||//The first line of the sign contains [Private]
						line1.equalsIgnoreCase(ILConfigHandler.config.getString("["+"settingsChat.firstLine"+"]"))){ //The first line of the sign contains custom [Private] text

					Player player = event.getPlayer();
					String line2 = sign.getLine(1);
					String ownerUUIDString = Utils.getUuidFromLine(line2);

					if(ownerUUIDString!=null&&!ownerUUIDString.isEmpty()){//Use UUIDs

						UUID ownerUUID = UUID.fromString(ownerUUIDString);
						String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();

						if(plugin.getConfig().getList("list").contains(ownerName)||plugin.getConfig().getList("list").contains(ownerUUID.toString())){
							player.sendMessage(ILConfigHandler.mes("onPunch.noPermission"));
							return;
						}

						if(isOverlyInactive(ownerUUID)){
							//Lock owner is inactive
							if(ILConfigHandler.config.getBoolean("settings.permissionToOpenLocks")&&player.hasPermission("inactivelockette.player")||player.hasPermission("inactivelockette.*")){//If they have permission
								//If capitalism is enabled and they have the munniez, make them pay
								makeUserPay(player);

								//Empty the container
								Block attachedBlock = LocketteProAPI.getAttachedBlock(block);
								clearContainer(attachedBlock,player);

								//Break sign
								block.breakNaturally();

								//Broadcast to whole server
								broadcast(attachedBlock,player.getName(),ownerName);
							}
							else//They don't have permission
								player.sendMessage(ChatColor.DARK_RED+ILConfigHandler.mes("onPunch.NoPermission"));
						}
						else{
							//Owner is still active
							//Tell user, and tell time remaining
							ownerStillActive(player,getInactivityDays(ownerUUID));
						}
					}
					else{//Don't use UUIDs
						String ownerName = Utils.getUsernameFromLine(line2);
						
						if(plugin.getConfig().getList("list").contains(ownerName)){
							player.sendMessage(ILConfigHandler.mes("onPunch.noPermission"));
							return;
						}
						
						if(isOverlyInactive(ownerName)){
							if(ILConfigHandler.config.getBoolean("settings.permissionToOpenLocks")&&player.hasPermission("inactivelockette.player")||player.hasPermission("inactivelockette.*")){
								makeUserPay(player);
								Block attachedBlock = LocketteProAPI.getAttachedBlock(block);
								clearContainer(attachedBlock,player);
								block.breakNaturally();
								broadcast(attachedBlock,player.getName(),ownerName);
							}
							else
								player.sendMessage(ChatColor.DARK_RED+ILConfigHandler.mes("onPunch.noPermission"));
						}
						else{
							ownerStillActive(player,getInactivityDays(ownerName));
						}
					}
				}
			}
		}
	}
	private void clearContainer(Block block,Player player){
		//Block is block the sign was attached on
		if(ILConfigHandler.config.getBoolean("clearItems")){
			String mat = block.getType().toString().toLowerCase();
			switch(mat){//Chest, trapped chest, furnace, dispenser, dropper, brewing stand, hopper,
			case "chest": case "trapped_chest": case "furnace": case "dispenser": case "dropper": case "brewing_stand": case "hopper":
				InventoryHolder ih = (InventoryHolder) block.getState();
				ih.getInventory().clear();
				player.sendMessage(ILConfigHandler.mes("onUnlock.cleared"));
				break;
			}
		}
	}
	private void broadcast(Block block, String breakerName, String ownerName){
		// Block is the block that was unlocked
		if(ILConfigHandler.config.getBoolean("broadcast")){

			if(block == null){
				plugin.getLogger().severe("The attachedBlock was null!");;
				return;
			}
			else{
				String blockName = block.getType().name().toLowerCase().replaceAll("_", " ");

				Location l = block.getLocation();

				int x = (int) l.getX();

				int y = (int) l.getY();

				int z = (int) l.getZ();

				String location = x + ", " + y + ", " + z;

				Bukkit.broadcastMessage(ILConfigHandler.mes("messages.broadcast").replaceAll("%block%", blockName).replaceAll("%owner%", ownerName).replaceAll("%breaker%", breakerName).replaceAll("%coordinates%", location));
			}
		}
	}
	private void ownerStillActive(Player player,long inactivityDays){
		player.sendMessage(ILConfigHandler.mes("onPunch.active"));
		if(ILConfigHandler.config.getBoolean("onClickDisplayDays")){
			player.sendMessage(ILConfigHandler.mes("onPunch.inactive").replaceAll("%inactivedays%", Long.toString(inactivityDays)));
		}
		if(ILConfigHandler.config.getBoolean("onClickDisplayDaysToWait")){
			long daysToWait = ILConfigHandler.config.getInt("daysOfInactivity")-inactivityDays;
			player.sendMessage(ILConfigHandler.mes("onPunch.daysToWait").replaceAll("%daystowait%", Long.toString(daysToWait)));
		}
	}
	private void makeUserPay(Player p){
		if(ILConfigHandler.config.getBoolean("useEconomy")&&ILMain.econ.isEnabled()&&ILMain.econ.hasAccount(p)){//If economy is enabled
			double cost = ILConfigHandler.config.getInt("cost");
			double balance = ILMain.econ.getBalance(p);
			if(ILMain.econ.has(p, cost)){//If player has the munniez
				ILMain.econ.withdrawPlayer(p, cost);//Take the munniez
				DecimalFormat df = new DecimalFormat("0.00");
				String moneyCost = df.format(cost);
				String newBalance = df.format(ILMain.econ.getBalance(p));
				p.sendMessage(ILConfigHandler.mes("messages.moneyWithdraw").replaceAll("%cost%", moneyCost).replaceAll("%balance%", newBalance));
			}
			else{//Player is poor
				DecimalFormat df = new DecimalFormat("0.00");
				String moneyNeeded = df.format(cost-balance);
				String moneyCost = df.format(cost);
				String newBalance = df.format(ILMain.econ.getBalance(p));
				p.sendMessage(ILConfigHandler.mes("messages.moneyTransactionFailed").replaceAll("%cost%", moneyCost).replaceAll("%balance%", newBalance).replaceAll("%needed%", moneyNeeded));
			}
		}
	}
	private long getInactivityDays(OfflinePlayer op){
		return getInactivityTime(op)/86400000;
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
		return getInactivityTime(op)/86400000>ILConfigHandler.config.getLong("daysOfInactivity") ? true : false;	
	}
	@SuppressWarnings("deprecation")
	private boolean isOverlyInactive(String s){
		return isOverlyInactive(Bukkit.getOfflinePlayer(s));
	}
	private boolean isOverlyInactive(UUID uuid){
		return isOverlyInactive(Bukkit.getOfflinePlayer(uuid));
	}
}
