package gvlfm78.plugin.InactiveLockette;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.MetadataValue;
import org.yi.acru.bukkit.Lockette.Lockette;

public class ILListener implements Listener {

	private ILMain plugin;
	private FileConfiguration config;
	private ILConfigHandler conf;

	public ILListener(ILMain plugin){
		this.plugin = plugin;
		this.conf = new ILConfigHandler(plugin);
		this.config = plugin.getConfig();
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true, priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			Block block = event.getClickedBlock();
			if ((block.getState() instanceof Sign)&&(block.getType() == Material.WALL_SIGN)){ //Player Left Clicked a wall sign
				Sign sign = (Sign) block.getState();
				if (sign.getLine(0).equalsIgnoreCase("[Private]") ||//The first line of the sign contains [Private]
						sign.getLine(0).equalsIgnoreCase(config.getString("settingsChat.firstLine"))){ //The first line of the sign contains custom [Private] text

					Player player = event.getPlayer();

					if(conf.useUUIDs()){//Use UUIDs
						UUID ownerUUID = Lockette.getProtectedOwnerUUID(block);
						String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
						if(isOverlyInactive(ownerUUID)){
							//Lock owner is inactive
							if(config.getBoolean("settings.permissionToOpenLocks")&&player.hasPermission("inactivelockette.player")||player.hasPermission("inactivelockette.*")){//If they have permission
								//If capitalism is enabled and they have the munniez, make them pay
								makeUserPay(player);

								//Empty the container
								Block attachedBlock = Lockette.getSignAttachedBlock(block);
								clearContainer(attachedBlock,player);

								//Break sign
								block.breakNaturally();

								//Broadcast to whole server
								broadcast(attachedBlock,player.getName(),ownerName);
							}
							else//They don't have permission
								player.sendMessage(ChatColor.DARK_RED+conf.mes("onPunch.messageNoPermission"));
						}
						else{
							//Owner is still active
							//Tell user, and tell time remaining
							ownerStillActive(player,getInactivityDays(ownerUUID));
						}
					}
					else{//Don't use UUIDs

					}
				}
			}
		}
	}
	private void clearContainer(Block block,Player player){
		//Block is block the sign was attached on
		if(config.getBoolean("clearItems")){
			String mat = block.getType().toString().toLowerCase();
			switch(mat){//Chest, trapped chest, furnace, dispenser, dropper, brewing stand, hopper,
			case "chest": case "trapped_chest": case "furnace": case "dispenser": case "dropper": case "brewing_stand": case "hopper":
				InventoryHolder ih = (InventoryHolder) block.getState();
				ih.getInventory().clear();
				player.sendMessage(conf.mes("onUnlock.cleared"));
				break;
			}
		}
	}
	private void broadcast(Block block, String breakerName, String ownerName){
		// Block is the block that was unlocked
		if(config.getBoolean("broadcast")){

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

				Bukkit.broadcastMessage(conf.mes("messages.broadcast").replaceAll("%block%", blockName).replaceAll("%owner%", ownerName).replaceAll("%breaker%", breakerName).replaceAll("%coordinates%", location));
			}
		}
	}
	private void ownerStillActive(Player player,long inactivityDays){
		player.sendMessage(conf.mes("onPunch.active"));
		if(config.getBoolean("settings.onClickDisplayDays")){
			player.sendMessage(conf.mes("onPunch.inactive").replaceAll("%inactiveDays%", Long.toString(inactivityDays)));
		}
		if(config.getBoolean("settings.onClickDisplayDaysToWait")){
			long daysToWait = config.getInt("daysOfInactivity")-inactivityDays;
			player.sendMessage(conf.mes("onPunch.daysToWait").replaceAll("%daystowait%", Long.toString(daysToWait)));
		}
	}
	private void makeUserPay(Player p){
		if(config.getBoolean("useEconomy")&&ILMain.econ.isEnabled()&&ILMain.econ.hasAccount(p)){//If economy is enabled
			double cost = config.getInt("cost");
			double balance = ILMain.econ.getBalance(p);
			if(ILMain.econ.has(p, cost)){//If player has the munniez
				ILMain.econ.withdrawPlayer(p, cost);//Take the munniez
				DecimalFormat df = new DecimalFormat("0.00");
				String moneyCost = df.format(cost);
				String newBalance = df.format(ILMain.econ.getBalance(p));
				p.sendMessage(conf.mes("messages.moneyWithdraw").replaceAll("%cost%", moneyCost).replaceAll("%balance%", newBalance));
			}
			else{//Player is poor
				DecimalFormat df = new DecimalFormat("0.00");
				String moneyNeeded = df.format(cost-balance);
				String moneyCost = df.format(cost);
				String newBalance = df.format(ILMain.econ.getBalance(p));
				p.sendMessage(conf.mes("messages.moneyTransactionFailed").replaceAll("%cost%", moneyCost).replaceAll("%balance%", newBalance).replaceAll("%needed%", moneyNeeded));
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
		return true;
		//return getInactivityTime(op)/86400000>config.getLong("daysOfInactivity") ? true : false;	
	}
	@SuppressWarnings("deprecation")
	private boolean isOverlyInactive(String s){
		return isOverlyInactive(Bukkit.getOfflinePlayer(s));
	}
	private boolean isOverlyInactive(UUID uuid){
		return isOverlyInactive(Bukkit.getOfflinePlayer(uuid));
	}

	private UUID getUUIDFromMeta(Sign sign, int index) {
		if (sign.hasMetadata("LocketteUUIDs")) {
			List<?> list = sign.getMetadata("LocketteUUIDs");

			return ((UUID[])(UUID[])((MetadataValue)list.get(0)).value())[(index - 1)];
		}
		return null;
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