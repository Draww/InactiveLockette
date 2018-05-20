package gvlfm78.plugin.InactiveLockette.listeners;

import gvlfm78.plugin.InactiveLockette.ILMain;
import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import me.crafter.mc.lockettepro.LocketteProAPI;
import me.crafter.mc.lockettepro.Utils;
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

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class LocketteProListener implements Listener {

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
        if(ILConfigHandler.config.getBoolean("broadcast")){

            if(block == null){
                plugin.getLogger().severe("The attachedBlock was null!");
                return;
            } else {
                String blockName = block.getType().name().toLowerCase().replaceAll("_", " ");

                Location l = block.getLocation();

                int x = (int) l.getX();

                int y = (int) l.getY();

                int z = (int) l.getZ();

                String location = x + ", " + y + ", " + z;

                Messenger.broadcastMessage("messages.broadcast","%block%",blockName,"%owner%",ownerName,"%breaker%",breakerName,"%coordinates%",location);
            }
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
        if(!ILConfigHandler.config.getBoolean("useEconomy") || ILMain.econ.isEnabled()) return;

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
}
