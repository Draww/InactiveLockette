package gvlfm78.plugin.InactiveLockette.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.yi.acru.bukkit.Lockette.Lockette;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class LocketteListener extends ILListener {

    @Override
    protected void handleLeftClick(Player player, String[] lines, Block signBlock, Sign sign){
        Block attachedBlock = Lockette.getSignAttachedBlock(signBlock);

        try{
            //We need this method as Lockette knows how to access its metadata
            Method method = Lockette.class.getDeclaredMethod("getUUIDFromMeta", Sign.class, int.class);
            method.setAccessible(true);

            UUID ownerUUID = (UUID) method.invoke(sign, 1);

            if(ownerUUID != null && !ownerUUID.toString().isEmpty()){//Use UUIDs
                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                performInactivityChecks(ownerName, player, signBlock, attachedBlock, Optional.ofNullable(ownerUUID));
            } else {//Use usernames

                performInactivityChecks(lines[1], player, signBlock, attachedBlock, Optional.empty());
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}