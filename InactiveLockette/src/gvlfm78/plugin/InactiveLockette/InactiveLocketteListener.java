package gvlfm78.plugin.InactiveLockette;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import org.bukkit.Material;

import org.bukkit.block.Block;

import org.bukkit.block.BlockFace;

import org.bukkit.block.Chest;

import org.bukkit.block.Dispenser;

import org.bukkit.block.Dropper;

import org.bukkit.block.Furnace;

import org.bukkit.block.Hopper;

import org.bukkit.block.Sign;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;

import org.bukkit.event.EventPriority;

import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;

import org.bukkit.event.player.PlayerInteractEvent;

public class InactiveLocketteListener implements Listener {

    private InactiveLockette plugin;

    public InactiveLocketteListener(InactiveLockette plugin) {this.plugin = plugin;}

    @EventHandler(priority = EventPriority.HIGH)

    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.isCancelled()) return; //If event is cancelled stop here

        if (event.getAction() == Action.LEFT_CLICK_BLOCK){ //Player Left Clicked
            onLeftClick(event);
        }
    }

    private void onLeftClick(PlayerInteractEvent event){

        Block block = event.getClickedBlock(); //Getting the block the player clicked

        if (block == null){ return; } //Player seems to have clicked in air.

        if(block.getType() == Material.WALL_SIGN){ //Player Left Clicked a wall sign

            onWallSignClick(event);

        }

    }

    private void onWallSignClick(PlayerInteractEvent event){

        Block block = event.getClickedBlock(); //Getting the block the player clicked

        Sign sign = (Sign) block.getState(); //Getting the sign state

        if ( //The sign contains [Private]

                sign.getLine(0).equalsIgnoreCase("[" + plugin.getConfig().getString("settingsChat.firstLine") + "]")){ //The first line of the sign

            signHasPrivate(event);

        }

    }

    private String playerOnSign(PlayerInteractEvent event){

        Block block = event.getClickedBlock(); //Getting the block the player clicked

        Sign sign = (Sign) block.getState(); //Getting the sign state

        String PlayerOnSignVariable = sign.getLine(1);

        return PlayerOnSignVariable;

    }

    private int inactivityDays(PlayerInteractEvent event){

        String PlayerOnSign = playerOnSign(event);

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

    private Block blockSignIsAttachedTo(PlayerInteractEvent event){

        Block block = event.getClickedBlock(); //Getting the block the player clicked

        Sign sign = (Sign) block.getState(); //Getting the sign state

        Block face = block.getRelative( ((org.bukkit.material.Sign)(sign.getData())).getAttachedFace());

        //Getting the block the sign is attached to

        return face;

    }

    private void chestContainerOpening(PlayerInteractEvent event){

        Block face = blockSignIsAttachedTo(event); //Block sign is attached to variable

        if(face.getType() == Material.CHEST){

            Player player = event.getPlayer(); //Getting the player who clicked the sign

            String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating useful variable

            if(plugin.getConfig().getBoolean ("ClearItems")){

                player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("onUnlock.messageChest"));

                Chest chest = (Chest) face.getState();

                chest.getInventory().clear();

                BlockFace[] chestFaces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

                for (BlockFace bf : chestFaces) {

                    Block faceBlock = face.getRelative(bf);

                    if (faceBlock.getType() == Material.CHEST)

                    {

                        ((Chest) faceBlock.getState()).getInventory().clear();

                    }

                }

            }

        }

    }

    private void containerOpening(Material Container, PlayerInteractEvent event){

        Block face = blockSignIsAttachedTo(event); //Block sign is attached to variable

        if(face.getType() == Container){

            if(plugin.getConfig().getBoolean ("ClearItems")){

                Player player = event.getPlayer(); //Getting the player who clicked the sign

                String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating useful variable

                if(Container == Material.FURNACE){

                    player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageFurnace"));

                    Furnace furnace = (Furnace) face.getState();

                    furnace.getInventory().clear();

                }

                else if(Container == Material.DISPENSER){

                    player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageDispenser"));

                    Dispenser dispenser = (Dispenser) face.getState();

                    dispenser.getInventory().clear();

                }

                else if(Container == Material.HOPPER){

                    player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageHopper"));

                    Hopper hopper = (Hopper) face.getState();

                    hopper.getInventory().clear();

                }

                if(Container == Material.DROPPER){

                    player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString ("onUnlock.messageDropper"));

                    Dropper dropper = (Dropper) face.getState();

                    dropper.getInventory().clear();

                }

            }

        }

    }

    private void broadcast(PlayerInteractEvent event){

        if(plugin.getConfig().getBoolean ("settings.broadcast")){

            Block block = event.getClickedBlock(); //Getting the block the player clicked

            Block face = blockSignIsAttachedTo(event); //Block sign is attached to variable

            Block freeblock = (Block) face;

            String freedblock = freeblock.getType().name();

            Location l = block.getLocation();

            int x = (int) l.getX();

            int y = (int) l.getY();

            int z = (int) l.getZ();

            String location = x + ", " + y + ", " + z;

            String PlayerOnSign = playerOnSign(event);

            String prefix = plugin.getConfig().getString("settingsChat.prefix"); //Creating useful variable

            Bukkit.broadcastMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("messages.messageBroadcast").replace("%block%", freedblock).replace("%owner%", PlayerOnSign).replace("%coordinates%", location));

        }

    }

    private void signHasPrivate(PlayerInteractEvent event){
        Player player = event.getPlayer(); //Getting the player who clicked the sign
        String playerString = player.getName();
        int stringlength = playerString.length();
        if(stringlength<15){
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
                //Player has been inactive

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
                                    player.sendMessage(ChatColor.GREEN + prefix + " " + plugin.getConfig().getString("messages.messageMoneyWithdraw").replace("%cost%", Integer.toString(cost)).replace("%balance%", Double.toString(balance)));
                                }
                                else if(!(InactiveLockette.econ.has(playerName, plugin.getConfig().getInt("settings.costToOpenLocks")))){
                                    double moneyneeded = balance-cost;
                                    player.sendMessage(ChatColor.RED + prefix + " " + plugin.getConfig().getString("messages.messageMoneyTransactionFailed"));
                                    player.sendMessage(ChatColor.RED + prefix + " " + plugin.getConfig().getString("messages.messageMoneyTransactionFailed").replace("%cost%", Integer.toString(cost)));
                                    player.sendMessage(ChatColor.RED + prefix + " " + plugin.getConfig().getString("messages.messageMoneyTransactionFailed").replace("%balance%", Double.toString(balance)).replace("%moneyneeded%", Double.toString(moneyneeded)));
                                }
                            }
                        }

                        chestContainerOpening(event); //Passing to the checks to remove items

                        containerOpening(Material.FURNACE, event);

                        containerOpening(Material.DISPENSER, event);

                        containerOpening(Material.HOPPER, event);

                        containerOpening(Material.DROPPER, event);

                        event.getClickedBlock().breakNaturally(); //Breaking the sign

                        broadcast(event);

                    }
                }
                else{
                    String prefix = plugin.getConfig().getString("settingsChat.prefix");
                    player.sendMessage(ChatColor.BLUE + prefix + " " + plugin.getConfig().getString("onCommand.messageNoPermission"));

                }

            }

        }

    }

}