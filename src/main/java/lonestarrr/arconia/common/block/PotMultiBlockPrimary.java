package lonestarrr.arconia.common.block;

import com.mojang.serialization.MapCodec;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.block.entities.PotMultiBlockSecondaryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

/**
 * Block that is part of a large multiblock pot - this is the primary block. It will render a large model, and has a ticking block entity dealing
 * with the pot's logic
 */
public class PotMultiBlockPrimary extends BaseEntityBlock {
    public static final Block INSIDE_BLOCK = Blocks.YELLOW_TERRACOTTA;
    public static final Block OUTSIDE_BLOCK = Blocks.BLACK_TERRACOTTA;

    public PotMultiBlockPrimary() {
        super(Block.Properties.of().mapColor(MapColor.METAL).strength(4.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PotMultiBlockPrimaryBlockEntity(pos, state); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.POT_MULTIBLOCK_PRIMARY.get(), PotMultiBlockPrimaryBlockEntity::tick);
        }
        return null;
    }

    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(worldIn, pos, state, player);
        breakMultiBlock(worldIn, pos);
        return result;
    }

    /**
     * Forms a multiblock pot of gold, if the conditions are met
     * @param world
     * @param goldPos Should be a position in the world holding a block of gold as part of the required multiblock blocks to be replaced
     * @return
     *     True on successful formation
     */
    public static boolean formMultiBlock(Level world, BlockPos goldPos) {
        if (world.isClientSide) {
            return false;
        }

        // The user touched a gold block that is 1 y-pos above the center of the multiblock to be formed, if all the required blocks in the world
        // have been placed. They will be replaced by the special multiblock blocks that are otherwise not obtainable
        if (!canFormMultiBlock(world, goldPos)) {
            return false;
        }

        BlockPos primaryPos = goldPos.below();
        world.setBlock(primaryPos, ModBlocks.potMultiBlockPrimary.get().defaultBlockState(), 3);

        BlockPos corner = goldPos.offset(-1, -1, -1);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toReplace = corner.offset(x, y, z);
                    if (toReplace.equals(primaryPos)) {
                        continue;
                    }
                    PotMultiBlockSecondary.PotPosition potPos = PotMultiBlockSecondary.PotPosition.getPositionFromOffset(x, z);
                    world.setBlock(toReplace, ModBlocks.potMultiBlockSecondary.get().defaultBlockState().setValue(PotMultiBlockSecondary.POT_POSITION, potPos), 3);
                    BlockEntity be = world.getBlockEntity(toReplace);
                    if (be == null || !(be instanceof PotMultiBlockSecondaryBlockEntity)) {
                        Arconia.logger.error("Error setting up pot multiblock - expected to find a secondary multiblock block entity at " + toReplace);
                        return false;
                    }
                    PotMultiBlockSecondaryBlockEntity secondaryBE = (PotMultiBlockSecondaryBlockEntity) be;
                    ((PotMultiBlockSecondaryBlockEntity) be).setPrimaryPos(primaryPos);
                }
            }
        }
        return true;
    }

    public static void breakMultiBlock(Level world, BlockPos primaryPos) {
        if (world.isClientSide) {
            return;
        }

        BlockEntity te = world.getBlockEntity(primaryPos);
        if (te == null || !(te instanceof PotMultiBlockPrimaryBlockEntity)) {
            return;
        }

        BlockPos corner = primaryPos.offset(-1, 0, -1);
        BlockPos goldPos = primaryPos.above();

        ItemStack insideBlock = new ItemStack(INSIDE_BLOCK, 0);
        ItemStack outsideBlocks = new ItemStack(OUTSIDE_BLOCK, 0);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toReplace = corner.offset(x, y, z);
                    BlockState bs = world.getBlockState(toReplace);

                    if (bs.getBlock().equals(ModBlocks.potMultiBlockSecondary) || bs.getBlock().equals(ModBlocks.potMultiBlockPrimary)) {
                        if (toReplace.equals(goldPos)) {
                            insideBlock.setCount(1);
                        } else {
                            outsideBlocks.setCount(outsideBlocks.getCount() + 1);
                        }
                        world.setBlockAndUpdate(toReplace, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        boolean playedSound = false;
        for (ItemStack item: new ItemStack[] { insideBlock, outsideBlocks }) {
            if (item.getCount() == 0) {
                continue;
            }
            ItemEntity entity = new ItemEntity(world, goldPos.getX(), goldPos.getY(), goldPos.getZ(), item);
            entity.setDeltaMovement(0, 0.1, 0);
            entity.setNoPickUpDelay();
            world.addFreshEntity(entity);
            if (!playedSound) {
                world.playSound(null, goldPos, SoundEvents.NETHERITE_BLOCK_BREAK, SoundSource.BLOCKS, 1, 1);
                playedSound = true;
            }
        }
    }

    public static boolean canFormMultiBlock(Level world, BlockPos centerPos) {
        // Expecting pos to be a block of gold, surrounded by cauldrons in a 3x3 grid, and another layer of 3x3 cauldrons below it
        if (world.getBlockState(centerPos).getBlock() != INSIDE_BLOCK) {
            return false;
        }

        BlockPos corner = centerPos.offset(-1, -1, -1);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos toCheck = corner.offset(x, y, z);
                    if (toCheck.equals(centerPos)) {
                        continue;
                    }
                    BlockState bs = world.getBlockState(toCheck);
                    if (bs.getBlock() != OUTSIDE_BLOCK) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
