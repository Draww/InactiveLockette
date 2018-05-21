package gvlfm78.plugin.InactiveLockette.listeners;

import gvlfm78.plugin.InactiveLockette.ILMain;
import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public abstract class ILListener implements Listener {

    protected void clearContainer(Block block, Player player){
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

    protected void makeUserPay(Player p){
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

    protected void broadcast(Block block, String breakerName, String ownerName){
        // Block is the block that was unlocked
        if(!ILConfigHandler.config.getBoolean("broadcast")) return;

        if(block == null) Messenger.sendConsoleErrorMessage("The attached block was null!");
        else {
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

    protected void ownerStillActive(Player player, long inactivityDays){
        Messenger.sendCommandSenderMessage(player, "onPunch.active");

        if(ILConfigHandler.config.getBoolean("onClickDisplayDays"))
            Messenger.sendCommandSenderMessage(player, "onPunch.inactive","%inactivedays%", Long.toString(inactivityDays));

        if(ILConfigHandler.config.getBoolean("onClickDisplayDaysToWait")){
            long daysToWait = ILConfigHandler.config.getInt("daysOfInactivity") - inactivityDays;
            Messenger.sendCommandSenderMessage(player, "onPunch.daysToWait","%daystowait%", Long.toString(daysToWait));
        }
    }

    protected long getInactivityDays(OfflinePlayer op){
        return getInactivityTime(op) / 86400000;
    }

    protected long getInactivityDays(UUID uuid){
        return getInactivityDays(Bukkit.getOfflinePlayer(uuid));
    }

    @SuppressWarnings("deprecation")
    protected long getInactivityDays(String name){
        return getInactivityDays(Bukkit.getOfflinePlayer(name));
    }

    protected long getInactivityTime(OfflinePlayer op){
        return System.currentTimeMillis() - op.getLastPlayed();
    }

    protected boolean isOverlyInactive(OfflinePlayer op){
        return getInactivityTime(op) / 86400000 > ILConfigHandler.config.getLong("daysOfInactivity");
    }

    @SuppressWarnings("deprecation")
    protected boolean isOverlyInactive(String s){
        return isOverlyInactive(Bukkit.getOfflinePlayer(s));
    }

    protected boolean isOverlyInactive(UUID uuid){
        return isOverlyInactive(Bukkit.getOfflinePlayer(uuid));
    }

    //This method was only in lockette listener
    protected boolean isBlackListed(UUID ownerUUID){
        List<?> list = ILConfigHandler.config.getList("list");
        if(list == null || list.isEmpty()) return false;
        return list.contains(ownerUUID) || list.contains(ownerUUID.toString());
    }
}
