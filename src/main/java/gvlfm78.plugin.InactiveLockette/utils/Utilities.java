package gvlfm78.plugin.InactiveLockette.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.function.Consumer;

import static org.bukkit.block.BlockFace.*;

public class Utilities {

    public static ArrayList<Sign> findMoreUsersSigns(Block block){
        ArrayList<Sign> signs = new ArrayList<>();
        Material mat = block.getType();

        Consumer<Block> checks = (relativeBlock) -> {
            if(mat == relativeBlock.getType())
                signs.addAll(findMoreUsersSignsThisBlockOnly(relativeBlock));
        };

        switch(mat){
            case CHEST: case TRAPPED_CHEST:
                iterateAroundBlock(block, checks);

            case WOODEN_DOOR: case SPRUCE_DOOR: case BIRCH_DOOR:
            case JUNGLE_DOOR: case ACACIA_DOOR: case DARK_OAK_DOOR: case IRON_DOOR_BLOCK:
                iterateOverBlock(block, checks);

            default: //Just around the one block, even for custom blocks
                signs.addAll(findMoreUsersSignsThisBlockOnly(block));
        }
        return signs;
    }

    /**
     * Find more users signs but only around specified block
     * Useful for things such as trapdoors and hoppers
     */
    private static ArrayList<Sign> findMoreUsersSignsThisBlockOnly(Block block){
        ArrayList<Sign> signs = new ArrayList<>();

        iterateAroundBlock(block, (relativeBlock -> {
            if(isSign(block)){
                Sign sign = blockToSign(relativeBlock);
                if(isMoreUsersSign(sign))
                    signs.add(sign);
            }
        }));

        return signs;
    }

    private static void iterateAroundBlock(Block block, Consumer<Block> checks){
        iterateBlock(block,checks, new BlockFace[]{NORTH,EAST,SOUTH,WEST});
    }

    private static void iterateOverBlock(Block block, Consumer<Block> checks){
        iterateBlock(block,checks, new BlockFace[]{UP, DOWN});
    }

    private static void iterateBlock(Block block, Consumer<Block> checks, BlockFace[] faces){
        for(BlockFace face : faces){
            Block relativeBlock = block.getRelative(face);
            checks.accept(relativeBlock);
        }
    }

    public static boolean isSign(Block block){
        return block.getType() != Material.WALL_SIGN;
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
        return isSignFirstLine(sign, "[More Users"); //todo support alternative [text]
    }
    private static boolean isSignFirstLine(Sign sign, String... texts){
        String firstLine = sign.getLine(0);
        for(String text : texts)
            if(firstLine.equalsIgnoreCase(text)) return true;
        return false;
    }
}
