package gvlfm78.plugin.InactiveLockette.listeners;

import gvlfm78.plugin.InactiveLockette.ILMain;
import gvlfm78.plugin.InactiveLockette.utils.ILConfigHandler;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import gvlfm78.plugin.InactiveLockette.utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import java.util.*;

public abstract class ILListener implements Listener {

    /**
     * Clear the contents of a container
     * @param block The container block the sign was attached to
     * @param player The player that has broken the lock
     */
    protected void clearContainer(Block block, Player player){
        if(!ILConfigHandler.config.getBoolean("clearItems")) return;
        String mat = block.getType().toString().toLowerCase();
        switch(mat){
            case "chest":
            case "trapped_chest":
            case "furnace":
            case "dispenser":
            case "dropper":
            case "brewing_stand":
            case "hopper":
                InventoryHolder ih = (InventoryHolder) block.getState();
                ih.getInventory().clear();
                Messenger.sendPlayerMessage(player, "onUnlock.cleared");
                break;
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
            Messenger.sendPlayerMessage(p, "messages.moneyWithdraw","%cost%", moneyCost,"%balance%", newBalance);
        } else {//Player is poor
            DecimalFormat df = new DecimalFormat("0.00");
            String moneyNeeded = df.format(cost - balance);
            String moneyCost = df.format(cost);
            String newBalance = df.format(ILMain.econ.getBalance(p));
            Messenger.sendPlayerMessage(p, "messages.moneyTransactionFailed","%cost%", moneyCost,"%balance%", newBalance,"%needed%", moneyNeeded);
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
        Messenger.sendPlayerMessage(player, "onPunch.active");

        if(ILConfigHandler.config.getBoolean("onClickDisplayDays"))
            Messenger.sendPlayerMessage(player, "onPunch.inactive","%inactivedays%", Long.toString(inactivityDays));

        if(ILConfigHandler.config.getBoolean("onClickDisplayDaysToWait")){
            long daysToWait = ILConfigHandler.config.getInt("daysOfInactivity") - inactivityDays;
            Messenger.sendPlayerMessage(player, "onPunch.daysToWait","%daystowait%", Long.toString(daysToWait));
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

    protected boolean isInactive(OfflinePlayer op){
        return getInactivityTime(op) / 86400000 > ILConfigHandler.config.getLong("daysOfInactivity");
    }

    @SuppressWarnings("deprecation")
    protected boolean isInactive(String s){
        return isInactive(Bukkit.getOfflinePlayer(s));
    }

    protected boolean isInactive(UUID uuid){
        return isInactive(Bukkit.getOfflinePlayer(uuid));
    }

    protected boolean isBlackListed(UUID ownerUUID){
        List<?> list = ILConfigHandler.config.getList("list");
        if(list == null || list.isEmpty()) return false;
        return list.contains(ownerUUID) || list.contains(ownerUUID.toString());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if(!Utilities.isSign(block)) return;
        //Player Left Clicked a wall sign

        Sign sign = Utilities.blockToSign(block);
        if(!Utilities.isPrivateSign(sign)) return;

        Player player = event.getPlayer();

        if(!hasPermissionToOpenLocks(player)){
            Messenger.sendPlayerMessage(player, "onPunch.noPermission");
            return;
        }

        boolean isUUIDSign = isUUIDSign(sign);
        OfflinePlayer owner;
        if(isUUIDSign) owner = getPlayerFromUUIDLine(sign,0);
        else owner = getPlayerFromNameLine(sign.getLine(0));

        if(!isInactive(owner)){ //Owner is still active
            ownerStillActive(player, getInactivityDays(owner));
            return;
        }

        //Owner is inactive
        if(ILConfigHandler.config.getBoolean("onlyCheckFirstName")){
            removeLock(owner.getName(), player, block, block.getRelative(((org.bukkit.material.Sign) sign).getAttachedFace()));
            return;
        }
        
        //Find all [more users] signs
        ArrayList<Sign> signs = new ArrayList<>();
        signs.add(sign); //Add the [Private] sign
        
        signs.addAll(Utilities.findMoreUsersSigns(block));

        HashMap<OfflinePlayer,Sign> players = new HashMap<>();
        for(Sign currentSign : signs){
            for(OfflinePlayer offlinePlayer : getPlayersFromSign(sign, isUUIDSign))
                players.put(offlinePlayer, currentSign);
        }

        //Remove owner as we already know s/he is inactive
        players.remove(owner);

        //Remove inactive players
        for(OfflinePlayer offlinePlayer : players.keySet()){
            if(isInactive(offlinePlayer))
                players.remove(offlinePlayer);
        }

        //Loop through signs and place each player in sequential order
        Queue<OfflinePlayer> playerQueue= new ArrayDeque<>(players.keySet());

        for(Sign currentSign : signs){
            for(int i = 1; i < 4; i++){
                OfflinePlayer op = playerQueue.poll();

                if(op == null){ //the queue is empty
                    if(i == 1) currentSign.getBlock().breakNaturally(); //Sign is empty so no longer needed
                    continue; //Instead of return so other now empty signs are also broken
                }

                String name = op.getName();
                if(isUUIDSign)
                    sign.setLine(i, name);
                else{
                    if(this instanceof LocketteListener){
                        LocketteListener ll = (LocketteListener) this;
                        ll.setUUIDCompatibleSignLine(sign,i,name,op);
                    }
                    else { //instance of LocketteProListener
                        LocketteProListener lpl = (LocketteProListener) this;
                        lpl.setUUIDCompatibleSignLine(name,op.getUniqueId());
                    }
                }
            }
            //todo might need sign.update()
        }
    }

    private boolean hasPermissionToOpenLocks(Player player){
        return !isBlackListed(player.getUniqueId()) &&
                !ILConfigHandler.config.getBoolean("permissionToOpenLocks") ||
                player.hasPermission("inactivelockette.player") ||
                player.hasPermission("inactivelockette.*") ||
                player.hasPermission("inactivelockette.admin");
    }

    private void removeLock(String ownerName, Player player, Block signBlock, Block attachedBlock){
        //If economy is enabled and they have the money, make them pay
        makeUserPay(player);

        //Empty the container
        clearContainer(attachedBlock, player);

        //Break sign
        signBlock.breakNaturally();

        //Broadcast to whole server
        broadcast(attachedBlock, player.getName(), ownerName);
    }

    private ArrayList<OfflinePlayer> getPlayersFromSign(Sign sign){
        return getPlayersFromSign(sign, isUUIDSign(sign));
    }

    private ArrayList<OfflinePlayer> getPlayersFromSign(Sign sign, boolean isUUIDSign){
        ArrayList<OfflinePlayer> players = new ArrayList<>();
        //Only check line indices 1,2,3
        String [] lines = sign.getLines();

        if(isUUIDSign){
            for(int index = 0; index < 4; index++)
                players.add(getPlayerFromUUIDLine(sign, index));
        }
        else {
            for(String line : lines)
                players.add(getPlayerFromNameLine(line));
        }

        return players;
    }

    protected abstract boolean isUUIDSign(Sign sign);
    protected abstract OfflinePlayer getPlayerFromNameLine(String line);
    protected abstract OfflinePlayer getPlayerFromUUIDLine(Sign sign, int index);
}
