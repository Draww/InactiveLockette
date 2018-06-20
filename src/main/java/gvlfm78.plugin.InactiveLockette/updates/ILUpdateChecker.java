package gvlfm78.plugin.InactiveLockette.updates;

import gvlfm78.plugin.InactiveLockette.ILMain;
import gvlfm78.plugin.InactiveLockette.utils.Messenger;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;

class ILUpdateChecker {

    private ILMain plugin;
    private final File pluginFile;

    ILUpdateChecker(ILMain plugin, File pluginFile){
        this.plugin = plugin;
        this.pluginFile = pluginFile;
    }

    private String[] getUpdateMessages(){
        String[] updateMessages = new String[2];

        boolean useSpigot = false;
        String updates = plugin.getConfig().getString("updates");
        if(updates.equalsIgnoreCase("spigot")
                || (!updates.equalsIgnoreCase("bukkit") && Bukkit.getVersion().toLowerCase().contains("spigot")
        )) useSpigot = true;

        if(useSpigot){
            final SpigotUpdateChecker SUC = new SpigotUpdateChecker();
            if(SUC.getNewUpdateAvailable()){
                updateMessages[0] = Messenger.getLocalisedMessage("updates.updateAvailable","%version%", SUC.getLatestVersion());
                updateMessages[1] = Messenger.getLocalisedMessage("updates.updateAvailableLink","%link%", SUC.getUpdateURL());
            }
        }
        else{//Get messages from bukkit update checker
            Updater updater = new Updater(plugin, 52457, pluginFile, Updater.UpdateType.NO_DOWNLOAD, false);
            if(updater.getResult().equals(Updater.UpdateResult.UPDATE_AVAILABLE)){
                //Updater knows local and remote versions are different, but not if it's an update
                String remoteVersion = updater.getLatestName().replaceAll("[A-Za-z\\s]", "");
                if(shouldUpdate(remoteVersion)){
                    updateMessages[0] = Messenger.getLocalisedMessage("updates.updateAvailable","%version%", remoteVersion);
                    updateMessages[1] = Messenger.getLocalisedMessage("updates.updateAvailableLink","%link%", updater.getLatestFileLink());
                }
            }
        }
        return updateMessages;
    }

    public void sendUpdateMessages(Player p){//Sends messages to a player
        for(String message : getUpdateMessages()){
            if(message != null && !message.isEmpty())//If there was no update/check is disabled message will be null
                p.sendMessage(message);
        }
    }
    public static boolean shouldUpdate(String remoteVersion){
        return shouldUpdate(ILMain.getDescriptionFile().getVersion(), remoteVersion);
    }
    private static boolean shouldUpdate(String localVersion, String remoteVersion) {
        return versionCompare(localVersion, remoteVersion) < 0;
    }
    private static Integer versionCompare(String oldVer, String newVer){
        String[] vals1 = oldVer.split("\\.");
        String[] vals2 = newVer.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))
            i++;
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length){
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else
            return Integer.signum(vals1.length - vals2.length);
    }
}
