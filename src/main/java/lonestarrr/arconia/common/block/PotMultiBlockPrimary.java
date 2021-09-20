package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.tile.PotMultiBlockPrimaryTileEntity;
import lonestarrr.arconia.common.block.tile.PotMultiBlockSecondaryTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Block that is part of a large multiblock pot - this is the primary block. It will render a large model, and has a ticking tile entity dealing
 * with the pot's logic
 */
public class PotMultiBlockPrimary extends Block {
    public PotMultiBlockPrimary() {
        super(Block.Properties.create(Material.IRON, MaterialColor.BLACK).hardnessAndResistance(2.0F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PotMultiBlockPrimaryTileEntity();
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        breakMultiBlock(worldIn, pos);
    }

    /**
     * Forms a multiblock pot of gold, if the conditions are met
     * @param world
     * @param goldPos Should be a position in the world holding a block of gold as part of the required multiblock blocks to be replaced
     * @return
     *     True on successful formation
     */
    public static boolean formMultiBlock(World world, BlockPos goldPos) {
        if (world.isRemote) {
            return false;
        }

        // TODO lighting looks off - guess the invisible block above it does not pass light?
        // TODO bounding box is too large

        // The user touched a gold block that is 1 y-pos above the center of the multiblock to be formed, if all the required blocks in the world
        // have been placed. They will be replaced by the special multiblock blocks that are otherwise not obtainable
        if (!canFormMultiBlock(world, goldPos)) {
            return false;
        }

        BlockPos primaryPos = goldPos.down();
        world.setBlockState(primaryPos, ModBlocks.potMultiBlockPrimary.getDefaultState(), 3);

        BlockPos corner = goldPos.add(-1, -1, -1);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toReplace = corner.add(x, y, z);
                    if (toReplace.equals(primaryPos)) {
                        continue;
                    }
                    world.setBlockState(toReplace, ModBlocks.potMultiBlockSecondary.getDefaultState(), 3);
                    TileEntity te = world.getTileEntity(toReplace);
                    if (te == null || !(te instanceof PotMultiBlockSecondaryTileEntity)) {
                        Arconia.logger.error("Error setting up pot multiblock - expected to find a secondary multiblock tile entity at " + toReplace);
                        return false;
                    }
                    PotMultiBlockSecondaryTileEntity secondaryTE = (PotMultiBlockSecondaryTileEntity) te;
                    ((PotMultiBlockSecondaryTileEntity) te).setPrimaryPos(primaryPos);
                }
            }
        }
        return true;
    }

    public static void breakMultiBlock(World world, BlockPos primaryPos) {
        if (world.isRemote) {
            return;
        }

        TileEntity te = world.getTileEntity(primaryPos);
        if (te == null || !(te instanceof PotMultiBlockPrimaryTileEntity)) {
            return;
        }

        BlockPos corner = primaryPos.add(-1, 0, -1);
        BlockPos goldPos = primaryPos.up();

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toReplace = corner.add(x, y, z);
                    BlockState bs = world.getBlockState(toReplace);

                    if (bs.getBlock().equals(ModBlocks.potMultiBlockSecondary) || bs.getBlock().equals(ModBlocks.potMultiBlockPrimary)) {
                        if (toReplace.equals(goldPos)) {
                            world.setBlockState(toReplace, Blocks.GOLD_BLOCK.getDefaultState(), 3);
                        } else {
                            world.setBlockState(toReplace, Blocks.CAULDRON.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
    }

    public static boolean canFormMultiBlock(World world, BlockPos goldPos) {
        // Expecting pos to be a block of gold, surrounded by cauldrons in a 3x3 grid, and another layer of 3x3 cauldrons below it
        if (world.getBlockState(goldPos).getBlock() != Blocks.GOLD_BLOCK) {
            return false;
        }

        BlockPos corner = goldPos.add(-1, -1, -1);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toCheck = corner.add(x, y, z);
                    if (toCheck.equals(goldPos)) {
                        continue;
                    }
                    BlockState bs = world.getBlockState(toCheck);
                    if (bs.getBlock() != Blocks.CAULDRON) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
