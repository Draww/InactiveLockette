package gvlfm78.plugin.InactiveLockette;

import org.bukkit.plugin.Plugin;


public class InactiveLocketteConfigHandler {

    static InactiveLocketteConfigHandler instance = new InactiveLocketteConfigHandler();

    public static InactiveLocketteConfigHandler getInstance() {
        return instance;
    }

    public void setupConfig(Plugin plugin){
        plugin.getLogger().info("Generating config file...");
        plugin.getConfig().options().header("InactiveLockette Plugin by gvlfm78");
        plugin.getConfig().addDefault("settings.language", String.valueOf("en"));
        plugin.getConfig().addDefault("settings.forceLanguageOverwriteOnNextStartup", Boolean.valueOf(false));
        plugin.getConfig().addDefault("settings.daysOfInactivity", Integer.valueOf(30));
        plugin.getConfig().addDefault("settings.clearItems", Boolean.valueOf(false));
        plugin.getConfig().addDefault("settings.onClickDisplayDays", Boolean.valueOf(true));
        plugin.getConfig().addDefault("settings.onClickDisplayDaysToWait", Boolean.valueOf(false));
        plugin.getConfig().addDefault("settings.broadcast", Boolean.valueOf(true));
        plugin.getConfig().addDefault("settings.permissionToOpenLocks", Boolean.valueOf(true));
        plugin.getConfig().addDefault("settings.useEconomy", Boolean.valueOf(false));
        plugin.getConfig().addDefault("settings.costToOpenLocks", Integer.valueOf(500));
        plugin.getConfig().addDefault("settings.checkForUpdates", Boolean.valueOf(true));

        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.getLogger().info("Config file generated");
    }

    public void setupLanguageEnglish(Plugin plugin){
        plugin.getConfig().addDefault("settingsChat.firstLine", String.valueOf("Private"));
        plugin.getConfig().addDefault("settingsChat.prefix", String.valueOf("[InactiveLockette]"));

        plugin.getConfig().addDefault("onPluginLoad.updateAvailable", String.valueOf("A new version is available:"));
        plugin.getConfig().addDefault("onPluginLoad.updateAvailableLink", String.valueOf("Get it from:"));

        plugin.getConfig().addDefault("onCommand.messageReloadConfig", String.valueOf("Configuration file reloaded"));
        plugin.getConfig().addDefault("onCommand.messageVersion", String.valueOf("version"));
        plugin.getConfig().addDefault("onCommand.messageNoPermission", String.valueOf("You are not allowed to use that command"));

        plugin.getConfig().addDefault("onPunch.messageActive", String.valueOf("The owner of this lock is still active"));
        plugin.getConfig().addDefault("onPunch.messageInactive", String.valueOf("The owner of this lock has been inactive for %inactivedays% days"));
        plugin.getConfig().addDefault("onPunch.messageDaysToWait", String.valueOf("You still have to wait %daystowait% days to open this lock"));
        plugin.getConfig().addDefault("onPunch.messageNoPermission", String.valueOf("You are not allowed to open inactive locks"));

        plugin.getConfig().addDefault("onUnlock.messageChest", String.valueOf("The chest locked is going to get emptied"));
        plugin.getConfig().addDefault("onUnlock.messageFurnace", String.valueOf("The furnace locked is going to get emptied"));
        plugin.getConfig().addDefault("onUnlock.messageDispenser", String.valueOf("The dispenser locked is going to get emptied"));
        plugin.getConfig().addDefault("onUnlock.messageHopper", String.valueOf("The hopper locked is going to get emptied"));
        plugin.getConfig().addDefault("onUnlock.messageDropper", String.valueOf("The dropper locked is going to get emptied"));

        plugin.getConfig().addDefault("messages.messageBroadcast", String.valueOf("A %block% owned by %owner% was unlocked at coordinates %coordinates%"));
        plugin.getConfig().addDefault("messages.messageMoneyWithdraw", String.valueOf("You have removed this lock for %cost%. New balance: %balance%"));
        plugin.getConfig().addDefault("messages.messageMoneyTransactionFailed1", String.valueOf("You do not have enough money to remove this lock."));
        plugin.getConfig().addDefault("messages.messageMoneyTransactionFailed2", String.valueOf("You need %cost% to open a lock."));
        plugin.getConfig().addDefault("messages.messageMoneyTransactionFailed3", String.valueOf("You have %balance% and require another %moneyneeded%."));

        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.getLogger().info("Language strings generated");
    }

    public void setLanguageEnglish(Plugin plugin){
        plugin.getConfig().set("settingsChat.firstLine", String.valueOf("Private"));
        plugin.getConfig().set("settingsChat.prefix", String.valueOf("[InactiveLockette]"));

        plugin.getConfig().set("onPluginLoad.updateAvailable", String.valueOf("A new version is available:"));
        plugin.getConfig().set("onPluginLoad.updateAvailableLink", String.valueOf("Get it from:"));

        plugin.getConfig().set("onCommand.messageReloadConfig", String.valueOf("Configuration file reloaded"));
        plugin.getConfig().set("onCommand.messageVersion", String.valueOf("version"));
        plugin.getConfig().set("onCommand.messageNoPermission", String.valueOf("You are not allowed to use that command!"));

        plugin.getConfig().set("onPunch.messageActive", String.valueOf("The owner of this lock is still active"));
        plugin.getConfig().set("onPunch.messageInactive", String.valueOf("The owner of this lock has been inactive for %inactivedays% days"));
        plugin.getConfig().set("onPunch.messageDaysToWait", String.valueOf("You still have to wait %daystowait% days to open this lock"));
        plugin.getConfig().set("onPunch.messageNoPermission", String.valueOf("You are not allowed to break inactive locks"));

        plugin.getConfig().set("onUnlock.messageChest", String.valueOf("The chest locked is going to get emptied"));
        plugin.getConfig().set("onUnlock.messageFurnace", String.valueOf("The furnace locked is going to get emptied"));
        plugin.getConfig().set("onUnlock.messageDispenser", String.valueOf("The dispenser locked is going to get emptied"));
        plugin.getConfig().set("onUnlock.messageHopper", String.valueOf("The hopper locked is going to get emptied"));
        plugin.getConfig().set("onUnlock.messageDropper", String.valueOf("The dropper locked is going to get emptied"));

        plugin.getConfig().set("messages.messageBroadcast", String.valueOf("A %block% owned by %owner% was unlocked at coordinates %coordinates%"));
        plugin.getConfig().set("messages.messageMoneyWithdraw", String.valueOf("You have removed this lock for %cost%. New balance: %balance%"));
        plugin.getConfig().set("messages.messageMoneyTransactionFailed1", String.valueOf("You do not have enough money to remove this lock."));
        plugin.getConfig().set("messages.messageMoneyTransactionFailed2", String.valueOf("You need %cost% to open a lock."));
        plugin.getConfig().set("messages.messageMoneyTransactionFailed3", String.valueOf("You have %balance% and require another %moneyneeded%."));

        plugin.saveConfig();
        plugin.getLogger().info("Language strings generated");
    }

    public void setLanguageItalian(Plugin plugin){
        plugin.getConfig().set("settingsChat.firstLine", String.valueOf("Privato"));
        plugin.getConfig().set("settingsChat.prefix", String.valueOf("[InactiveLockette]"));

        plugin.getConfig().set("onPluginLoad.updateAvailable", String.valueOf("Una nuova versione e'' disponibile:"));
        plugin.getConfig().set("onPluginLoad.updateAvailableLink", String.valueOf("Scaricala da:"));

        plugin.getConfig().set("onCommand.messageReloadConfig", String.valueOf("File di configurazione ricaricato"));
        plugin.getConfig().set("onCommand.messageVersion", String.valueOf("versione"));
        plugin.getConfig().set("onCommand.messageNoPermission", String.valueOf("Non hai il permesso di utilizzare quel comando!"));

        plugin.getConfig().set("onPunch.messageActive", String.valueOf("Il proprietario di questo blocco e' ancora attivo"));
        plugin.getConfig().set("onPunch.messageInactive", String.valueOf("Il proprietario di questo blocco e' stato inattivo per %inactivedays% giorni"));
        plugin.getConfig().set("onPunch.messageDaysToWait", String.valueOf("Devi ancora aspettare %daystowait% giorni per sbloccare questo blocco"));
        plugin.getConfig().set("onPunch.messageNoPermission", String.valueOf("Non hai il permesso di rimuovere blocchi inattivi altrui"));

        plugin.getConfig().set("onUnlock.messageChest", String.valueOf("La chest sbloccata sara' svuotata"));
        plugin.getConfig().set("onUnlock.messageFurnace", String.valueOf("La fornace sbloccata sara' svuotata"));
        plugin.getConfig().set("onUnlock.messageDispenser", String.valueOf("Il distributore sbloccato sara' svuotato"));
        plugin.getConfig().set("onUnlock.messageHopper", String.valueOf("La tramoggia sbloccata sara' svuotata"));
        plugin.getConfig().set("onUnlock.messageDropper", String.valueOf("Il gettatore sbloccato sara' svuotato"));

        plugin.getConfig().set("messages.messageBroadcast", String.valueOf("Un %block% di proprieta' di %owner% e' stato/a sbloccato/a alle coordinate %coordinates%"));
        plugin.getConfig().set("messages.messageMoneyWithdraw", String.valueOf("Hai rimosso questo blocco al costo di %cost%. Nuovo bilancio: %balance%"));
        plugin.getConfig().set("messages.messageMoneyTransactionFailed1", String.valueOf("Nnon hai abbastanza soldi per rimuovere questo blocco."));
        plugin.getConfig().set("messages.messageMoneyTransactionFailed2", String.valueOf("Ti servono %cost% per rimuovere un blocco."));
        plugin.getConfig().set("messages.messageMoneyTransactionFailed3", String.valueOf("Hai %balance% e necessiti altri %moneyneeded%."));

        plugin.saveConfig();
        plugin.getLogger().info("[InactiveLockette] Stringhe di lingua generate");
    }
}