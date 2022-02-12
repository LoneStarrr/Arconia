package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.ArconiumTreeRootBlock;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Responsible for morphing tree leaves into the next tier's leaves.
 */
public class ArconiumTreeRootBlockEntity extends BaseBlockEntity {
    private static final int LOOT_DROP_INTERVAL = 100; // How often to drop loot

    private static final String TAG_LEAF_CHANGER = "leafChanger";

    private RainbowColor tier;
    private int tickCount;
    private LeafDropLootDispenser dispenser;
    private final Random rand = new Random();
    private static final Logger LOGGER = LogManager.getLogger();
    private BlockState nextTierLeafBlock;
    private boolean hasNextTier;
    private LeafChanger leafChanger;

    public ArconiumTreeRootBlockEntity(RainbowColor tier, BlockPos pos, BlockState state) {
        this(ArconiumTreeRootBlock.getBlockEntityTypeByTier(tier), tier, pos, state);
    }

    public ArconiumTreeRootBlockEntity(BlockEntityType<?> blockEntityTypeIn, RainbowColor tier, BlockPos pos, BlockState state) {
        super(blockEntityTypeIn, pos, state);
        this.tier = tier;
        RainbowColor nextTier = tier.getNextTier();
        hasNextTier = false;

        if (nextTier != null) {
            hasNextTier = true;
            nextTierLeafBlock = ModBlocks.getArconiumTreeLeaves(nextTier).defaultBlockState();
            leafChanger = new LeafChanger(this, nextTierLeafBlock);
        }
        dispenser = new LeafDropLootDispenser(this, LOOT_DROP_INTERVAL);
    }

    public RainbowColor getTier() {
        return tier;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArconiumTreeRootBlockEntity blockEntity) {
        blockEntity.tickInternal(level, pos, state);
    }

    public void tickInternal(Level level, BlockPos pos, BlockState state) {
        tickCount++;

        if (level.isClientSide) {
            return;
        }

        dispenser.tick();

        if (leafChanger != null) {
            if (leafChanger.tick()) {
                setChanged();
            }
        }
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        if (leafChanger != null) {
            tag.put(TAG_LEAF_CHANGER, leafChanger.write());
        }
    }

    @Override
    public void readPacketNBT(CompoundTag compound) {
        if (compound.contains(TAG_LEAF_CHANGER)) {
            if (leafChanger != null) {
                leafChanger.read(compound.getCompound(TAG_LEAF_CHANGER));
            }
        }
    }
}

/**
 * Randomly changes leaves to the next tier's tree leaves
 */
class
LeafChanger {
    public static final int MAX_LEAVES_CHANGED = 10;
    public static final int MAX_INTERVALS = 100;

    private static final String TAG_LEAVES_CHANGED = "leavesChanged";
    private static final String TAG_INTERVAL_COUNT = "intervalCount";


    private final ArconiumTreeRootBlockEntity rootBlockEntity;
    private final BlockState toChangeTo;

    // Parameters determining speed/chance, tiered
    // TODO modconfig
    private final long changeInterval; // Number of ticks in between attempts to upgrade a leaf
    private final double changeChance; // Chance % a leaf will be upgraded for each attempt

    // State
    private int leavesChanged;
    private int intervalCount;
    private long lastInterval;
    private LinkedList<BlockPos> nearbyLeaves;

    public LeafChanger(@Nonnull ArconiumTreeRootBlockEntity atrbe, @Nonnull BlockState toChangeTo) {
        this.rootBlockEntity = atrbe;
        final int tierNum = atrbe.getTier().getTier(); // higher tier -> higher ordinal, 1..
        changeInterval = 60 * 20 * (long) Math.pow(1.5, tierNum - 1);
        changeChance = Math.max(5, 100 - tierNum * 15) / 100d;
        this.toChangeTo = toChangeTo; // Block to change leaf into - should be the next tier's leaf block
    }

    /**
     *
     * @return True if state was changed that should be persisted
     */
    public boolean tick() {
        Level world = rootBlockEntity.getLevel();
        if (world.isClientSide) {
            return false;
        }

        if (nearbyLeaves == null) {
            nearbyLeaves = findNearbyLeafPositions();
        }

        if (leavesChanged == MAX_LEAVES_CHANGED || intervalCount == MAX_INTERVALS || nearbyLeaves.size() == 0) {
            return false;
        }


        long now = world.getGameTime();
        if (lastInterval == 0) {
            lastInterval = world.getGameTime();
            return false;
        } else if (now - lastInterval < changeInterval) {
            return false;
        }

        lastInterval = now;
        intervalCount++;

        if (Math.random() > changeChance) {
            return true;
        }
        Block leafBlock = ModBlocks.getArconiumTreeLeaves(rootBlockEntity.getTier());

        // Keep popping blocks until we find a leaf block to replace (should some have disappeared)
        while (nearbyLeaves.size() > 0) {
            BlockPos toChange = nearbyLeaves.pop();
            BlockState state = world.getBlockState(toChange);
            if (state.getBlock().equals(leafBlock)) {
                BlockState newState = toChangeTo
                        .setValue(LeavesBlock.DISTANCE, state.getValue(LeavesBlock.DISTANCE))
                        .setValue(LeavesBlock.PERSISTENT, state.getValue(LeavesBlock.PERSISTENT));
                world.setBlock(toChange, newState, 3);
                leavesChanged++;
                break;
            }
        }

        return true;
    }

    /**
     * Find nearby leaves
     * @return
     *  A list of block positions of nearby leaves, in randomized order
     */
    private LinkedList<BlockPos> findNearbyLeafPositions() {
        List<BlockPos> result = new ArrayList<>();

        // Find leaves of the matching tier to potentially change. Any nearby leaf not manually placed will do
        final int scanRadius = 3;
        final int scanHeight = 10;
        BlockPos startPos = rootBlockEntity.getBlockPos().offset(-scanRadius, 0, -scanRadius);
        BlockPos endPos = rootBlockEntity.getBlockPos().offset(scanRadius, scanHeight, scanRadius);
        Block leafBlock = ModBlocks.getArconiumTreeLeaves(rootBlockEntity.getTier());
        for (BlockPos scanPos : BlockPos.betweenClosed(startPos, endPos)) {
            BlockState state = rootBlockEntity.getLevel().getBlockState(scanPos);
            if (state.getBlock().equals(leafBlock) && !state.getValue(LeavesBlock.PERSISTENT)) {
                result.add(scanPos.immutable());
            }
        }

        Collections.shuffle(result);
        return new LinkedList<>(result);
    }

    protected CompoundTag write() {
        CompoundTag result = new CompoundTag();
        result.putInt(TAG_LEAVES_CHANGED, leavesChanged);
        result.putInt(TAG_INTERVAL_COUNT, intervalCount);
        return result;
    }

    protected void read(CompoundTag nbt) {
        leavesChanged = nbt.getInt(TAG_LEAVES_CHANGED);
        intervalCount = nbt.getInt(TAG_INTERVAL_COUNT);
    }
}

class LeafDropLootDispenser {
    private static final int MAX_LEAVES = 20; // How many leaves to look for to drop loot from
    private static final int LEAF_SCAN_INTERVAL = 200; // How often to check for leaf updates

    private List<BlockPos> foundLeaves = new ArrayList<>(MAX_LEAVES);
    private int lastLeafScanTick;
    private int tickCount = 0;
    final private int dropInterval;
    final private ArconiumTreeRootBlockEntity rootBlockEntity;
    final private Random rand;

    public LeafDropLootDispenser(ArconiumTreeRootBlockEntity atrbe, int dropInterval) {
        this.dropInterval = dropInterval;
        this.rootBlockEntity = atrbe;
        lastLeafScanTick = -10000;
        rand = new Random();
    }

    /**
     * Loot is dropped from a leaf at an interval
     */
    public void tick() {
        Level world = rootBlockEntity.getLevel();
        if (world.isClientSide) {
            return;
        }

        tickCount++;

        if (tickCount % dropInterval != 0) {
            return;
        }

        // Locate ResourceGen blocks around the tree root that have a resource tree leaf above it of a valid tier
        List<Pair<ResourceGenBlockEntity, BlockPos>> generatorsAndLeaves = new ArrayList<>();

        int scanRadius = 1;
        BlockPos startPos = rootBlockEntity.getBlockPos().offset(-scanRadius, 0, -scanRadius);
        BlockPos endPos = rootBlockEntity.getBlockPos().offset(scanRadius, 0, scanRadius);

        for(BlockPos scanPos: BlockPos.betweenClosed(startPos, endPos)) {
            BlockEntity te = world.getBlockEntity(scanPos);
            if (te !=null && te instanceof ResourceGenBlockEntity) {
                ResourceGenBlockEntity rte = (ResourceGenBlockEntity) te;
                if (rte.getTier().compareTo(rootBlockEntity.getTier()) <= 0) {
                    // only consider resource generators with a valid leaf over them, THEN pick one or more of those to dispense loot
                    BlockPos leafPos = findLeaf(scanPos.getX(), scanPos.getY(), scanPos.getZ(), rte.getTier());
                    if (leafPos != null) {
                        generatorsAndLeaves.add(Pair.of(rte, leafPos));
                    }
                }
            }

        }

        Collections.shuffle(generatorsAndLeaves);
        int maxGenerators = 1;
        for (int i = 0; i < maxGenerators && i < generatorsAndLeaves.size(); i++) {
            Pair<ResourceGenBlockEntity, BlockPos> pair = generatorsAndLeaves.get(i);
            dispenseLoot(pair.getLeft(), pair.getRight());
        }
    }

    /**
     * Find a leaf that in a (x,z) vertical column starting at y that meets the requirements to drop loot from
     * @param x
     * @param y Same y as tree root block
     * @param z
     * @return position of valid leaf
     */
    private BlockPos findLeaf(int x, int y, int z, RainbowColor minTier) {
        // A valid leaf:
        // * Has a block of air beneath it
        // * Has no other leaf blocks below it
        // * Is not too high up
        // y - y level 1 above pattern
        final int MAX_COLUMN_HEIGHT = 6;
        int maxY = y + MAX_COLUMN_HEIGHT;
        boolean lastBlockWasAir = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        while (++y < maxY) {
            pos.set(x, y, z);
            BlockState bs = rootBlockEntity.getLevel().getBlockState(pos);
            if (bs.isAir()) {
                lastBlockWasAir = true;
                continue;
            }

            if (isValidLeaf(pos, minTier)) {
                if (lastBlockWasAir) {
                    return new BlockPos(pos);
                } else {
                    // Once a leaf is found, and it's not above an air block, stop the search.
                    // This prevents loot raining down from inside the foliage if there is an air gap somehow.
                    return null;
                }
            }
            lastBlockWasAir = false;
        }

        return null;
    }

    private boolean isValidLeaf(BlockPos pos, RainbowColor minTier) {
        BlockState state = rootBlockEntity.getLevel().getBlockState(pos);
        if (!(state.getBlock() instanceof ArconiumTreeLeaves)) {
            return false;
        }

        ArconiumTreeLeaves leaf = (ArconiumTreeLeaves)state.getBlock();
        return (leaf.getTier().compareTo(minTier) >= 0);
    }

    /**
     * Dispense loot, such that it appears as if it 'rains' from the leaves.
     * @param leafPos Block position that contains an arconium tree leaf
     */
    private void dispenseLoot(ResourceGenBlockEntity generator, BlockPos leafPos) {
        Level world = rootBlockEntity.getLevel();
        if (world.isClientSide) {
            return;
        }

        ItemStack plunder = generator.getItemStack(); // Includes item count
        if (!plunder.isEmpty()) {
            ItemEntity entity = new ItemEntity(world, leafPos.getX() + 0.5D, leafPos.getY() - 1 + 0.5D,
                    leafPos.getZ() + 0.5D,
                    plunder);
            entity.setDeltaMovement(0D, 0.0D, 0D);
            entity.setDefaultPickUpDelay();
            entity.lifespan = 200; // Short lifespan - encourage players to use the crates, plus it's server-friendlier
            world.addFreshEntity(entity);
        }
    }
}