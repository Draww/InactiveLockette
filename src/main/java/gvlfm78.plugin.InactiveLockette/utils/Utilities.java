package gvlfm78.plugin.InactiveLockette.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.bukkit.block.BlockFace.*;

public final class Utilities {

    /**
     * Finds More Users signs attached to block
     * @param block Block the sign is attached to
     * @return List of More Users signs attached to block
     */
    public static List<Sign> findMoreUsersSigns(Block block){
        List<Sign> signs = new ArrayList<>();
        Material mat = block.getType();

        switch(mat){
            case CHEST: case TRAPPED_CHEST:
                iterateAroundBlock(block, (relativeBlock, face) -> {
                    if(mat == relativeBlock.getType())
                        signs.addAll(findMoreUsersSignsThisBlockOnly(relativeBlock));
                });

            case WOODEN_DOOR: case SPRUCE_DOOR: case BIRCH_DOOR:
            case JUNGLE_DOOR: case ACACIA_DOOR: case DARK_OAK_DOOR: case IRON_DOOR_BLOCK:
                iterateOverBlock(block, (relativeBlock, face) -> {
                    signs.addAll(findMoreUsersSignsThisBlockOnly(relativeBlock));

                    if(mat == relativeBlock.getType())
                        //Doors can also be locked one block above and below, thus:
                        signs.addAll(findMoreUsersSignsThisBlockOnly(relativeBlock.getRelative(face)));
                });

            default: //Just around the one block, even for custom blocks
                signs.addAll(findMoreUsersSignsThisBlockOnly(block));
        }
        return signs;
    }

    /**
     * Find more users signs but only around specified block
     * Useful for things such as trapdoors and hoppers
     */
    private static List<Sign> findMoreUsersSignsThisBlockOnly(Block block){
        List<Sign> signs = new ArrayList<>();

        iterateAroundBlock(block, (relativeBlock, face) -> {
            if(isSign(relativeBlock)){
                Sign sign = blockToSign(relativeBlock);
                if(isMoreUsersSign(sign))
                    signs.add(sign);
            }
        });

        return signs;
    }

    private static void iterateAroundBlock(Block block, BiConsumer<Block, BlockFace> checks){
        iterateBlock(block, checks, new BlockFace[]{NORTH,EAST,SOUTH,WEST});
    }

    private static void iterateOverBlock(Block block, BiConsumer<Block, BlockFace> checks){
        iterateBlock(block, checks, new BlockFace[]{UP, DOWN});
    }

    private static void iterateBlock(Block block, BiConsumer<Block, BlockFace> checks, BlockFace[] faces){
        for(BlockFace face : faces){
            Block relativeBlock = block.getRelative(face);
            checks.accept(relativeBlock, face);
        }
    }

    public static boolean isSign(Block block){
        return block.getType() == Material.WALL_SIGN;
    }

    /**
     * Get sign from block. Make sure to first check if the block is a sign!
     */
    public static Sign blockToSign(Block block){
        return (Sign) block.getState();
    }

    public static boolean isPrivateSign(Sign sign){
        return isSignFirstLine(sign, "[Private]", "[" + ILConfigHandler.config.getString("settingsChat.firstLine") + "]");
    }
    private static boolean isMoreUsersSign(Sign sign){
        return isSignFirstLine(sign, "[More Users]"); //todo support alternative [text]
    }
    private static boolean isSignFirstLine(Sign sign, String... texts){
        String firstLine = sign.getLine(0);
        for(String text : texts)
            if(firstLine.equalsIgnoreCase(text)) return true;
        return false;
    }

    public static WorldGuardPlugin getWorldGuard(){
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        if (p instanceof WorldGuardPlugin) return (WorldGuardPlugin) p;
        else return null;
    }

    public static boolean isPlayerRegionOwner(Player player, ProtectedRegion protectedRegion){
        return protectedRegion.getOwners().contains(player.getUniqueId());
    }

    public static ApplicableRegionSet getRegionsFromLocation(Location location){
        WorldGuardPlugin wg = Utilities.getWorldGuard();
        RegionManager rm = wg.getRegionManager(location.getWorld());
        return rm.getApplicableRegions(location);
    }

    public static boolean isPlayerBlockOwner(Player player, Block block){
        boolean found = false;
        for(ProtectedRegion protectedRegion : getRegionsFromLocation(block.getLocation())){
            if(protectedRegion.getOwners().contains(player.getUniqueId()))
                found = true;
        }
        return found;
    }
}
