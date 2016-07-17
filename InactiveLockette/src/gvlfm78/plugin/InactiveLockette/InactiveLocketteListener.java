package gvlfm78.plugin.InactiveLockette;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;
import java.util.logging.Level;

public class InactiveLocketteListener implements Listener {

	private InactiveLockette plugin;
	public InactiveLocketteListener(InactiveLockette plugin) {this.plugin = plugin;}
	String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating prefix variable


	@EventHandler(ignoreCancelled=true,priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			Block block = event.getClickedBlock();

			if ((block.getState() instanceof Sign)&&(block != null)&&(block.getType() == Material.WALL_SIGN)){ //Player Left Clicked a wall sign
				Sign sign = (Sign) block.getState();
				if (sign.getLine(0).equalsIgnoreCase("[" + plugin.getConfig().getString("settingsChat.firstLine") + "]")){ //The first line of the sign contains [Private]

					signHasPrivate(event);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private UUID playerOnSign(PlayerInteractEvent event){

		Block block = event.getClickedBlock(); //Getting the block the player clicked
		Sign sign = (Sign) block.getState(); //Getting the sign state

		return Bukkit.getPlayer(sign.getLine(1)).getUniqueId();
	}

	private Block getAttachedBlock(Block sb){
		if (sb.getType() == Material.WALL_SIGN) {
			Sign s = (Sign) sb.getState().getData();
			return sb.getRelative( ((org.bukkit.material.Sign)(s.getData())).getAttachedFace());
		} else {
			return null;
		}
	}

	private int inactivityDays(PlayerInteractEvent event){

		UUID PlayerOnSign = playerOnSign(event);

		long lastseen = Bukkit.getOfflinePlayer(PlayerOnSign).getLastPlayed();

		//Last time they were seen online (milliseconds)

		long current = System.currentTimeMillis();

		//Current system time (milliseconds)

		long difference = current - lastseen;

		//Difference in time (milliseconds)

		int inactivedays = (int) (difference/1000/60/60/24);

		//Changing the difference in time in milliseconds to days

		return inactivedays;

	}

	private void chestContainerOpening(PlayerInteractEvent event,Block signBlock){

		Block attachedBlock = getAttachedBlock(signBlock);
		if(attachedBlock == null){
			plugin.getLogger().log(Level.SEVERE,"The attachedBlock was null!");;
			return;
		}
		else{

			if(attachedBlock.getType().equals(Material.CHEST)){

				Player player = event.getPlayer(); //Getting the player who punched the sign


				if(plugin.getConfig().getBoolean ("ClearItems")){

					player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("onUnlock.messageChest"));

					Chest chest = (Chest) attachedBlock.getState();

					chest.getInventory().clear();

					BlockFace[] chestFaces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

					for (BlockFace bf : chestFaces) {

						Block faceBlock = attachedBlock.getRelative(bf);

						if (faceBlock.getType() == Material.CHEST)

						{

							((Chest) faceBlock.getState()).getInventory().clear();

						}

					}

				}

			}
		}
	}

	private void containerOpening(Material Container, PlayerInteractEvent event){

		Block attachedBlock = getAttachedBlock(event.getClickedBlock());

		if(attachedBlock == null){
			plugin.getLogger().log(Level.SEVERE,"The attachedBlock was null!");;
			return;
		}
		else{
			if(attachedBlock.getType() == Container){

				if(plugin.getConfig().getBoolean ("ClearItems")){

					Player player = event.getPlayer(); //Getting the player who clicked the sign
					String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating useful variable

					if(Container == Material.FURNACE){

						player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageFurnace"));
						Furnace furnace = (Furnace) attachedBlock.getState();
						furnace.getInventory().clear();

					}

					else if(Container == Material.DISPENSER){

						player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageDispenser"));
						Dispenser dispenser = (Dispenser) attachedBlock.getState();
						dispenser.getInventory().clear();

					}

					else if(Container == Material.HOPPER){

						player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageHopper"));
						Hopper hopper = (Hopper) attachedBlock.getState();
						hopper.getInventory().clear();

					}

					if(Container == Material.DROPPER){

						player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageDropper"));
						Dropper dropper = (Dropper) attachedBlock.getState();
						dropper.getInventory().clear();
					}
				}
			}
		}
	}

	private void broadcast(PlayerInteractEvent event){

		if(plugin.getConfig().getBoolean("settings.broadcast")){

			Block block = event.getClickedBlock(); //Getting the block the player clicked

			Block attachedBlock = getAttachedBlock(event.getClickedBlock());

			if(attachedBlock == null){
				plugin.getLogger().log(Level.SEVERE,"The attachedBlock was null!");;
				return;
			}
			else{
				String freedblock = attachedBlock.getType().name();

				Location l = block.getLocation();

				int x = (int) l.getX();

				int y = (int) l.getY();

				int z = (int) l.getZ();

				String location = x + ", " + y + ", " + z;

				String PlayerOnSign = playerOnSign(event).toString();

				String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating useful variable

				Bukkit.broadcastMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("messages.messageBroadcast").replace("%block%", freedblock).replace("%owner%", PlayerOnSign).replace("%coordinates%", location));

			}
		}
	}

	@SuppressWarnings("deprecation")
	private void signHasPrivate(PlayerInteractEvent event){
		Player player = event.getPlayer(); //Getting the player who clicked the sign
		String PlayerOnSign = playerOnSign(event).toString();
		int lengthPlayerOnSign = PlayerOnSign.length();
		if(lengthPlayerOnSign<14){
			int inactivedays = inactivityDays(event); //Inactivedays variable

			if(inactivedays == 0 && plugin.getConfig().getBoolean("settings.onClickDisplayDays")){

				String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating chat prefix variable

				player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("onPunch.messageActive"));

				//Player is still active

			}

			else if(plugin.getConfig().getBoolean("settings.onClickDisplayDays")){

				String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating prefix variable

				player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("onPunch.messageInactive").replace("%inactivedays%", Integer.toString(inactivedays)));

				if(plugin.getConfig().getBoolean("settings.onClickDisplayDaysToWait") && !((plugin.getConfig().getInt("settings.daysOfInactivity")) - (inactivedays) < 0)){

					player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("onPunch.messageDaysToWait").replace("%daystowait%", Integer.toString((plugin.getConfig().getInt("settings.daysOfInactivity")) - (inactivedays))));

				}
				else{}
				//No need to display days to wait

			}

			if((inactivedays > plugin.getConfig().getInt("settings.daysOfInactivity"))) {

				//If player has been inactive for at least set amount of days

				if(plugin.getConfig().getBoolean("settings.permissionToOpenLocks")){

					String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating prefix variable

					if(player.isOp()||player.hasPermission("inactivelockette.player")||player.hasPermission("inactivelockette.*")){ //If player who clicked has permission to open locks
						if(plugin.getConfig().getBoolean("settings.useEconomy")){ //If economy interface is used
							String playerName = player.getName(); //Player name String variable
							if((InactiveLockette.econ.isEnabled()) && (InactiveLockette.econ.hasAccount(playerName))){

								int cost = plugin.getConfig().getInt("settings.costToOpenLocks");//Cost variable
								double balance = InactiveLockette.econ.getBalance(playerName);//Balance of player

								if(InactiveLockette.econ.has(playerName, cost)){
									InactiveLockette.econ.withdrawPlayer(playerName, cost);
									player.sendMessage(ChatColor.GREEN + prefix + " " + plugin.getConfig().getString("messages.messageMoneyWithdraw").replace("%cost%", Integer.toString(cost)).replace("%balance%", Double.toString(InactiveLockette.econ.getBalance(playerName))));
								}
								else if(!(InactiveLockette.econ.has(playerName, plugin.getConfig().getInt("settings.costToOpenLocks")))){
									double moneyneeded = cost-balance;
									player.sendMessage(ChatColor.RED + prefix + " " + plugin.getConfig().getString("messages.messageMoneyTransactionFailed1"));
									player.sendMessage(ChatColor.RED + prefix + " " + plugin.getConfig().getString("messages.messageMoneyTransactionFailed2").replace("%cost%", Integer.toString(cost)));
									player.sendMessage(ChatColor.RED + prefix + " " + plugin.getConfig().getString("messages.messageMoneyTransactionFailed3").replace("%balance%", Double.toString(balance)).replace("%moneyneeded%", Double.toString(moneyneeded)));
									return;
								}
							}
						}
						//Economy isn't used

						chestContainerOpening(event, event.getClickedBlock()); //Passing to the checks to remove items

						containerOpening(Material.FURNACE, event);

						containerOpening(Material.DISPENSER, event);

						containerOpening(Material.HOPPER, event);

						containerOpening(Material.DROPPER, event);

						event.getClickedBlock().breakNaturally(); //Breaking the sign

						broadcast(event);

					}
					else{
						player.sendMessage(ChatColor.DARK_RED + prefix + " " + plugin.getConfig().getString("onPunch.messageNoPermission"));
					}
				}

			}

		}
		//Player name on sign is longer than 14 chars, cannot act further.
	}
}