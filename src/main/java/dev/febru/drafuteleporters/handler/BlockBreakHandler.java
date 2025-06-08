package dev.febru.drafuteleporters.handler;

import dev.febru.drafuteleporters.manager.TeleporterDataManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

import static dev.febru.drafuteleporters.manager.TeleporterDataManager.getAllTeleporters;
import static dev.febru.drafuteleporters.manager.TeleporterDataManager.removeRegion;
import static dev.febru.drafuteleporters.structures.TeleporterStructure.isBlockPartOfStructure;

public class BlockBreakHandler {

    public static void onBlockBroken(World world, BlockPos pos, BlockState blockState) {
        if (!world.getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD))
            return;

        if (blockState.getBlock() == Blocks.PURPLE_GLAZED_TERRACOTTA || blockState.getBlock() == Blocks.DIAMOND_BLOCK) {
            System.out.println("Block broken");
            for (TeleporterDataManager.TeleporterData teleporter : getAllTeleporters())
                if (isBlockPartOfStructure(teleporter.pos1, teleporter.pos2, pos)) {
                    removeRegion(teleporter.name);
                    return;
                }
        }
    }

}