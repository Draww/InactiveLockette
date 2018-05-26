package gvlfm78.plugin.InactiveLockette.listeners;

import me.crafter.mc.lockettepro.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;

import java.util.UUID;

public class LocketteProListener extends ILListener {

    /**
     * Set sign line in UUID-compatible format
     */
    protected String setUUIDCompatibleSignLine(String name, UUID uuid){
        return name + "#" + uuid.toString();
        //todo possibly use Utils.updateUuidByUsername or other method from Utils class
    }

    @Override
    protected boolean isUUIDSign(Sign sign){
        //If 2nd line holds UUID we assume the whole sign does
        return Utils.isUsernameUuidLine(sign.getLine(1));
    }

    @Override
    protected OfflinePlayer getPlayerFromNameLine(String line){
        return Bukkit.getOfflinePlayer(Utils.getUsernameFromLine(line));
    }

    @Override
    protected OfflinePlayer getPlayerFromUUIDLine(Sign sign, int index){
        return Bukkit.getOfflinePlayer(UUID.fromString(Utils.getUuidFromLine(sign.getLine(index))));
    }
}
