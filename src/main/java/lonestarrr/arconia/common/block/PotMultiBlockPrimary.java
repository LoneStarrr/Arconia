package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.tile.PotMultiBlockPrimaryTileEntity;
import lonestarrr.arconia.common.block.tile.PotMultiBlockSecondaryTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
        super(Block.Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(4.0F));
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
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.playerWillDestroy(worldIn, pos, state, player);
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
        if (world.isClientSide) {
            return false;
        }

        // The user touched a gold block that is 1 y-pos above the center of the multiblock to be formed, if all the required blocks in the world
        // have been placed. They will be replaced by the special multiblock blocks that are otherwise not obtainable
        if (!canFormMultiBlock(world, goldPos)) {
            return false;
        }

        BlockPos primaryPos = goldPos.below();
        world.setBlock(primaryPos, ModBlocks.potMultiBlockPrimary.defaultBlockState(), 3);

        BlockPos corner = goldPos.offset(-1, -1, -1);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toReplace = corner.offset(x, y, z);
                    if (toReplace.equals(primaryPos)) {
                        continue;
                    }
                    world.setBlock(toReplace, ModBlocks.potMultiBlockSecondary.defaultBlockState(), 3);
                    TileEntity te = world.getBlockEntity(toReplace);
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
        if (world.isClientSide) {
            return;
        }

        TileEntity te = world.getBlockEntity(primaryPos);
        if (te == null || !(te instanceof PotMultiBlockPrimaryTileEntity)) {
            return;
        }

        BlockPos corner = primaryPos.offset(-1, 0, -1);
        BlockPos goldPos = primaryPos.above();

        ItemStack goldBlock = new ItemStack(Blocks.GOLD_BLOCK, 0);
        ItemStack cauldrons = new ItemStack(Blocks.CAULDRON, 0);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toReplace = corner.offset(x, y, z);
                    BlockState bs = world.getBlockState(toReplace);

                    if (bs.getBlock().equals(ModBlocks.potMultiBlockSecondary) || bs.getBlock().equals(ModBlocks.potMultiBlockPrimary)) {
                        if (toReplace.equals(goldPos)) {
                            goldBlock.setCount(1);
                        } else {
                            cauldrons.setCount(cauldrons.getCount() + 1);
                        }
                        world.setBlockAndUpdate(toReplace, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        boolean playedSound = false;
        for (ItemStack item: new ItemStack[] { goldBlock, cauldrons }) {
            if (item.getCount() == 0) {
                continue;
            }
            ItemEntity entity = new ItemEntity(world, goldPos.getX(), goldPos.getY(), goldPos.getZ(), item);
            entity.setDeltaMovement(0, 0.1, 0);
            entity.setNoPickUpDelay();
            world.addFreshEntity(entity);
            if (!playedSound) {
                world.playSound(null, goldPos, SoundEvents.NETHERITE_BLOCK_BREAK, SoundCategory.BLOCKS, 1, 1);
                playedSound = true;
            }
        }
    }

    public static boolean canFormMultiBlock(World world, BlockPos goldPos) {
        // Expecting pos to be a block of gold, surrounded by cauldrons in a 3x3 grid, and another layer of 3x3 cauldrons below it
        if (world.getBlockState(goldPos).getBlock() != Blocks.GOLD_BLOCK) {
            return false;
        }

        BlockPos corner = goldPos.offset(-1, -1, -1);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toCheck = corner.offset(x, y, z);
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
