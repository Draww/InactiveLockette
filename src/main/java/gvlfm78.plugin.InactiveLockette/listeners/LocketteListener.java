package gvlfm78.plugin.InactiveLockette.listeners;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.metadata.MetadataValue;
import org.yi.acru.bukkit.Lockette.Lockette;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class LocketteListener extends ILListener {

    /**
     * Set sign line in UUID-compatible format
     */
    protected void setUUIDCompatibleSignLine(Sign sign, int index, String name, OfflinePlayer op){
        try{
            Lockette lockette = new Lockette();
            Method method = lockette.getClass().getDeclaredMethod("setLine", Sign.class, int.class, String.class, OfflinePlayer.class);
            method.setAccessible(true);
            method.invoke(lockette,sign,index,name,op);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isUUIDSign(Sign sign){
        return !sign.hasMetadata("LocketteUUIDs") || !(sign.getMetadata("LocketteUUIDs").size() > 0);
    }

    @Override
    protected OfflinePlayer getPlayerFromNameLine(String line){
        return Bukkit.getOfflinePlayer(line);
    }

    @Override
    protected OfflinePlayer getPlayerFromUUIDLine(Sign sign, int index){
        List<MetadataValue> list = sign.getMetadata("LocketteUUIDs");
        UUID uuid = ((UUID[]) list.get(0).value())[index - 1];
        return Bukkit.getOfflinePlayer(uuid);
    }
}