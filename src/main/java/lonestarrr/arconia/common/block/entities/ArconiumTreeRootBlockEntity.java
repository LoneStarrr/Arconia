package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.ArconiumTreeRootBlock;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Responsible for morphing tree leaves into the next tier's leaves.
 */
public class ArconiumTreeRootBlockEntity extends BlockEntity {
    private RainbowColor tier;
    private long lastInterval;
    private final Random rand = new Random();
    private static final Logger LOGGER = LogManager.getLogger();

    public ArconiumTreeRootBlockEntity(RainbowColor tier, BlockPos pos, BlockState state) {
        this(ArconiumTreeRootBlock.getBlockEntityTypeByTier(tier), tier, pos, state);
    }

    public ArconiumTreeRootBlockEntity(BlockEntityType<?> blockEntityTypeIn, RainbowColor tier, BlockPos pos, BlockState state) {
        super(blockEntityTypeIn, pos, state);
        this.tier = tier;
    }

    public RainbowColor getTier() {
        return tier;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArconiumTreeRootBlockEntity blockEntity) {
        blockEntity.tickInternal(level, pos, state);
    }

    public void tickInternal(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }

        long changeIntervalTicks = ConfigHandler.COMMON.treeLeafChangeInterval.get(this.tier).get() * 20l;
        long now = this.level.getGameTime();
        if (lastInterval == 0) {
            lastInterval = this.level.getGameTime();
            return;
        } else if (now - lastInterval < changeIntervalTicks) {
            return;
        }

        List<BlockPos> nearbyLeaves = findNearbyLeafPositions();
        RainbowColor nextTier = tier.getNextTier();

        if (nearbyLeaves.size() == 0 || nextTier == null) {
            BlockPos here = this.getBlockPos();
            this.level.setBlock(here, Blocks.OAK_LOG.defaultBlockState(), 3) ;
            this.level.playSound(null, here, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1, 1);
            return;
        }

        lastInterval = now;

        double changeChance = ConfigHandler.COMMON.treeLeafChangeChance.get(tier).get() / 100d;

        if (Math.random() <= changeChance) {
            BlockPos changePos = nearbyLeaves.get(0);
            BlockState leafState = this.level.getBlockState(changePos);
            BlockState newLeafState = ModBlocks.getArconiumTreeLeaves(nextTier).defaultBlockState()
                    .setValue(LeavesBlock.DISTANCE, leafState.getValue(LeavesBlock.DISTANCE))
                    .setValue(LeavesBlock.PERSISTENT, leafState.getValue(LeavesBlock.PERSISTENT));
            this.level.setBlock(changePos, newLeafState, 3);
            this.level.playSound(null, changePos, SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.BLOCKS, 1, 1);
        }
    }
    
    /**
     * Find nearby leaves
     * @return
     *  A list of block positions of nearby leaves, in randomized order
     */
    private List<BlockPos> findNearbyLeafPositions() {
        List<BlockPos> result = new ArrayList<>();

        // Find leaves of the matching tier to potentially change. Any nearby leaf not manually placed will do
        final int scanRadius = 3;
        final int scanHeight = 10;
        BlockPos startPos = this.getBlockPos().offset(-scanRadius, 0, -scanRadius);
        BlockPos endPos = this.getBlockPos().offset(scanRadius, scanHeight, scanRadius);
        Block leafBlock = ModBlocks.getArconiumTreeLeaves(this.getTier());
        for (BlockPos scanPos : BlockPos.betweenClosed(startPos, endPos)) {
            BlockState state = this.getLevel().getBlockState(scanPos);
            if (state.getBlock().equals(leafBlock) && !state.getValue(LeavesBlock.PERSISTENT)) {
                result.add(scanPos.immutable());
            }
        }

        Collections.shuffle(result);
        return result;
    }

}