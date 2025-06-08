package dev.febru.drafuteleporters.structures;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static dev.febru.drafuteleporters.block.ModBlocks.TELEPORTER_CORE_BLOCK;

public class TeleporterStructure {

    public static class StructureResult {
        public final boolean isValid;
        public final BlockPos center;
        public final BlockPos pos1; // bottom corner (diamond block)
        public final BlockPos pos2; // upper corner (air block)

        public StructureResult(boolean isValid, BlockPos center, BlockPos pos1, BlockPos pos2) {
            this.isValid = isValid;
            this.center = center;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        // For invalid structures
        public static StructureResult invalid() {
            return new StructureResult(false, null, null, null);
        }
    }

    public static StructureResult validateTeleporterStructure(World world, BlockPos thrownPos) {
        // Try all 4 possible centers
        BlockPos[] possibleCenters = {
                thrownPos.add(1, 0, 1),   // thrown on top-left
                thrownPos.add(0, 0, 1),   // thrown on top-right
                thrownPos.add(1, 0, 0),   // thrown on bottom-left
                thrownPos.add(0, 0, 0)    // thrown on bottom-right
        };

        for (BlockPos center : possibleCenters) {
            // Check 2x2 purple glazed terracotta layer and 4x4 diamond block layer
            if (checkLayer2x2(world, center.down(2)) &&
                    checkLayer4x4(world, center.down(3))) {

                // Calculate the region bounds
                BlockPos bottomCenter = center.down(3);
                BlockPos pos1 = bottomCenter.add(-2, 0, -2); // bottom corner (diamond block)
                BlockPos pos2 = center.add(1, -2, 1);         // upper corner (air block above structure)

                return new StructureResult(true, center, pos1, pos2);
            }
        }

        return StructureResult.invalid();
    }

    public static boolean isBlockPartOfStructure(BlockPos blockpos1, BlockPos blockpos2, BlockPos blockpos3) {

        if (!isValid4x4x2Region(blockpos1, blockpos2))
            return false;

        int minX = Math.min(blockpos1.getX(), blockpos2.getX());
        int maxX = Math.max(blockpos1.getX(), blockpos2.getX());
        int minY = Math.min(blockpos1.getY(), blockpos2.getY());
        int maxY = Math.max(blockpos1.getY(), blockpos2.getY());
        int minZ = Math.min(blockpos1.getZ(), blockpos2.getZ());
        int maxZ = Math.max(blockpos1.getZ(), blockpos2.getZ());

        int x = blockpos3.getX();
        int y = blockpos3.getY();
        int z = blockpos3.getZ();

        // Check if blockpos3 is within the 4x4x2 region
        if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ)
            return false;

        int layerFromBottom = y - minY;

        switch (layerFromBottom) {
            case 0: // Bottom layer (4x4 diamond blocks) - all blocks are required
                return true;

            case 1: // Top layer (2x2 purple glazed terracotta) - only center 2x2 is required
                return isInCenter2x2(x, z, minX, maxX, minZ, maxZ);

            default:
                return false;
        }
    }

    private static boolean isValid4x4x2Region(BlockPos pos1, BlockPos pos2) {
        int deltaX = Math.abs(pos1.getX() - pos2.getX());
        int deltaY = Math.abs(pos1.getY() - pos2.getY());
        int deltaZ = Math.abs(pos1.getZ() - pos2.getZ());

        // Check if it's exactly a 4x4x2 region (3 blocks difference = 4 block width, 1 block difference = 2 block height)
        return deltaX == 3 && deltaY == 1 && deltaZ == 3;
    }

    private static boolean isInCenter2x2(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        // Center 2x2 area is from (minX+1, minZ+1) to (minX+2, minZ+2)
        int centerMinX = minX + 1;
        int centerMaxX = minX + 2;
        int centerMinZ = minZ + 1;
        int centerMaxZ = minZ + 2;

        return x >= centerMinX && x <= centerMaxX && z >= centerMinZ && z <= centerMaxZ;
    }

    private static boolean checkLayer2x2(World world, BlockPos center) {
        BlockPos[] positions = {
                center.add(-1, 0, -1), center.add(0, 0, -1),
                center.add(-1, 0, 0),  center.add(0, 0, 0)
        };

        for (BlockPos pos : positions)
            if (!world.getBlockState(pos).isOf(TELEPORTER_CORE_BLOCK))
                return false;

        return true;
    }

    private static boolean checkLayer4x4(World world, BlockPos center) {
        for (int x = -2; x <= 1; x++)
            for (int z = -2; z <= 1; z++) {
                BlockPos pos = center.add(x, 0, z);
                if (!world.getBlockState(pos).isOf(Blocks.DIAMOND_BLOCK)) {
                    return false;
                }
            }
        return true;
    }
}