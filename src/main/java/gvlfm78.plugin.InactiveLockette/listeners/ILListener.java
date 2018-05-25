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
import org.bukkit.metadata.MetadataValue;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class ILListener implements Listener {

    protected void clearContainer(Block block, Player player){
        //Block is block the sign was attached on
        if(ILConfigHandler.config.getBoolean("clearItems")){
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
        if(block.getType() != Material.WALL_SIGN) return;
        //Player Left Clicked a wall sign

        Sign sign = (Sign) block.getState();
        String[] lines = sign.getLines();
        String line1 = lines[0];

        if(!line1.equalsIgnoreCase("[Private]") &&
                !line1.equalsIgnoreCase(
                        "[" + ILConfigHandler.config.getString("settingsChat.firstLine") + "]")) return;

        //todo maybe get [] text directly from the plugins:
        /*
        if(text.equals("[private]") || text.equalsIgnoreCase(Lockette.altPrivate)) privateSign = true;
        else if(text.equals("[more users]") || text.equalsIgnoreCase(Lockette.altMoreUsers)){
         */

        Player player = event.getPlayer();

        if(hasPermissionToOpenLocks(player))
            handleLeftClick(player, lines, block, sign);
        else
            Messenger.sendPlayerMessage(player, "onPunch.noPermission");
    }

    private boolean hasPermissionToOpenLocks(Player player){
        return !isBlackListed(player.getUniqueId()) &&
                !ILConfigHandler.config.getBoolean("permissionToOpenLocks") ||
                player.hasPermission("inactivelockette.player") ||
                player.hasPermission("inactivelockette.*") ||
                player.hasPermission("inactivelockette.admin");
    }

    protected abstract void handleLeftClick(Player player, String[] lines, Block signBlock, Sign sign);

    protected void performInactivityChecks(String ownerName, Player player, Block signBlock, Block attachedBlock, Optional<UUID> ownerUUID){
        boolean ownerUUIDPresent = ownerUUID.isPresent();
        boolean isInactive;
        if(ownerUUIDPresent) isInactive = isInactive(ownerUUID.get());
        else isInactive = isInactive(ownerName);

        if(!isInactive){
            if(ownerUUIDPresent)
                ownerStillActive(player, getInactivityDays(ownerUUID.get()));
            else ownerStillActive(player, getInactivityDays(ownerName));
            return;
        }

        if(ILConfigHandler.config.getBoolean("onlyCheckFirstName")){
            removeLock(ownerName, player, signBlock, attachedBlock);
            return;
        }

        //Get all other players on sign and on more users sign
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
        ArrayList<OfflinePlayer> players = new ArrayList<>();
        //Only check line indices 1,2,3
        String [] lines = sign.getLines();

        if(isUUIDSign(sign)){
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

    /**
     * Method from Lockette to get player UUID from sign metadata
     */
    protected static UUID LockettegetUUIDFromMeta(Sign sign, int index){
        if (!sign.hasMetadata("LocketteUUIDs") || !(sign.getMetadata("LocketteUUIDs").size() > 0))
            return null;

        List<MetadataValue> list = sign.getMetadata("LocketteUUIDs");
        UUID uuid = ((UUID[]) list.get(0).value())[index - 1];
        return uuid;
    }
}
