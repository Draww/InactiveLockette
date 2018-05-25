package gvlfm78.plugin.InactiveLockette.listeners;

import me.crafter.mc.lockettepro.LocketteProAPI;
import me.crafter.mc.lockettepro.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class LocketteProListener extends ILListener {

    @Override
    protected void handleLeftClick(Player player, String[] lines, Block signBlock, Sign sign){
        Block attachedBlock = LocketteProAPI.getAttachedBlock(signBlock);
        String ownerUUIDString = Utils.getUuidFromLine(lines[1]);

        if(ownerUUIDString != null && !ownerUUIDString.isEmpty()){
            UUID ownerUUID = UUID.fromString(ownerUUIDString);
            String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
            performInactivityChecks(ownerName, player, signBlock, attachedBlock, Optional.ofNullable(ownerUUID));
        }
        else {
            String ownerName = Utils.getUsernameFromLine(lines[1]);
            performInactivityChecks(ownerName, player, signBlock, attachedBlock, Optional.empty());
        }
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
        return Bukkit.getOfflinePlayer(Utils.getUuidFromLine(sign.getLine(index)));
    }
}
