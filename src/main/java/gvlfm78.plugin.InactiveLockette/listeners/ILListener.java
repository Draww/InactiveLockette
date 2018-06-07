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
import org.bukkit.configuration.file.FileConfiguration;
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
                Messenger.sendLocalisedMessage(player, "onUnlock.cleared");
                break;
        }
    }

    protected boolean makeUserPay(Player p){
        if(!ILConfigHandler.config.getBoolean("useEconomy") || !ILMain.econ.isEnabled()) return true;

        double cost = ILConfigHandler.config.getInt("cost");
        double balance = ILMain.econ.getBalance(p);

        if(ILMain.econ.has(p, cost)){//If player has the munniez
            ILMain.econ.withdrawPlayer(p, cost);//Take the munniez
            DecimalFormat df = new DecimalFormat("0.00");
            String moneyCost = df.format(cost);
            String newBalance = df.format(ILMain.econ.getBalance(p));
            Messenger.sendLocalisedMessage(p, "messages.moneyWithdraw","%cost%", moneyCost,"%balance%", newBalance);
            return true;
        } else {//Player is poor
            DecimalFormat df = new DecimalFormat("0.00");
            String moneyNeeded = df.format(cost - balance);
            String moneyCost = df.format(cost);
            String newBalance = df.format(ILMain.econ.getBalance(p));
            Messenger.sendLocalisedMessage(p, "messages.moneyTransactionFailed","%cost%", moneyCost,"%balance%", newBalance,"%needed%", moneyNeeded);
            return false;
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
                    "%block%", blockName, "%owner%", ownerName, "%breaker%", breakerName, "%coordinates%", location);
        }
    }

    protected void ownerStillActive(Player player, long inactivityDays){
        Messenger.sendLocalisedMessage(player, "onPunch.active");

        if(ILConfigHandler.config.getBoolean("onClickDisplayDays"))
            Messenger.sendLocalisedMessage(player, "onPunch.inactive","%inactivedays%", Long.toString(inactivityDays));

        if(ILConfigHandler.config.getBoolean("onClickDisplayDaysToWait")){
            long daysToWait = ILConfigHandler.config.getInt("daysOfInactivity") - inactivityDays;
            Messenger.sendLocalisedMessage(player, "onPunch.daysToWait","%daystowait%", Long.toString(daysToWait));
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

        boolean isUUIDSign = isUUIDSign(sign);
        OfflinePlayer owner;
        if(isUUIDSign) owner = getPlayerFromUUIDLine(sign,1);
        else owner = getPlayerFromNameLine(sign.getLine(1));

        //If they are the owner of this lock don't do anything
        if(player.getUniqueId().equals(owner.getUniqueId())) return;

        if(!hasPermissionToOpenLocks(player, block)){
            Messenger.sendLocalisedMessage(player, "onPunch.noPermission");
            return;
        }

        if(!isInactive(owner)){ //Owner is still active
            ownerStillActive(player, getInactivityDays(owner));
            return;
        }

        if(!makeUserPay(player)) return;

        //Find all [more users] signs
        ArrayList<Sign> signs = new ArrayList<>();
        signs.add(sign); //Add the [Private] sign

        org.bukkit.material.Sign matSign = (org.bukkit.material.Sign) block.getState().getData();

        Block attachedBlock = block.getRelative(matSign.getAttachedFace());

        signs.addAll(Utilities.findMoreUsersSigns(attachedBlock));

        //Owner is inactive
        if(ILConfigHandler.config.getBoolean("onlyCheckFirstName")){
            lockRemovedActions(owner.getName(), player, attachedBlock);
            signs.forEach(sign1 -> sign1.getBlock().breakNaturally());
            return;
        }

        HashMap<OfflinePlayer, Sign> players = new HashMap<>();
        for(Sign currentSign : signs){
            for(OfflinePlayer offlinePlayer : getPlayersFromSign(sign, isUUIDSign))
                players.put(offlinePlayer, currentSign);
        }

        //Remove owner as we already know s/he is inactive
        players.remove(owner);

        Queue<OfflinePlayer> playerQueue= new ArrayDeque<>(players.keySet());

        //Remove inactive players
        for(OfflinePlayer offlinePlayer : players.keySet()){
            if(isInactive(offlinePlayer))
                playerQueue.remove(offlinePlayer);
        }

        //Loop through signs and place each player in sequential order

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
        lockRemovedActions(owner.getName(), player, attachedBlock);
    }

    private boolean hasPermissionToOpenLocks(Player player, Block block){
        FileConfiguration config = ILConfigHandler.config;
        if(config.getBoolean("breakLockIfRegionOwner") && !Utilities.isPlayerBlockOwner(player, block))
            return false;

        if(config.getBoolean("breakLockIfCanBuild") && !Utilities.getWorldGuard().canBuild(player, block))
            return false;

        return !isBlackListed(player.getUniqueId()) &&
                !ILConfigHandler.config.getBoolean("permissionToOpenLocks") ||
                player.hasPermission("inactivelockette.player") ||
                player.hasPermission("inactivelockette.*") ||
                player.hasPermission("inactivelockette.admin");
    }

    private void lockRemovedActions(String ownerName, Player player, Block attachedBlock){
        //Empty the container
        clearContainer(attachedBlock, player);

        //Broadcast to whole server
        broadcast(attachedBlock, player.getName(), ownerName);
    }

    private ArrayList<OfflinePlayer> getPlayersFromSign(Sign sign, boolean isUUIDSign){
        ArrayList<OfflinePlayer> players = new ArrayList<>();
        //Only check line indices 1,2,3
        String [] lines = sign.getLines();

        if(isUUIDSign){
            for(int index = 0; index < 4; index++){
                OfflinePlayer op = getPlayerFromUUIDLine(sign, index);
                if(op != null)
                    players.add(op);
            }
        }
        else {
            for(String line : lines){
                OfflinePlayer op = getPlayerFromNameLine(line);
                if(op != null)
                    players.add(op);
            }
        }

        return players;
    }

    protected abstract boolean isUUIDSign(Sign sign);
    protected abstract OfflinePlayer getPlayerFromNameLine(String line);
    protected abstract OfflinePlayer getPlayerFromUUIDLine(Sign sign, int index);
}
