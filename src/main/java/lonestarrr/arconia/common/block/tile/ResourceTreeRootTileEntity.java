package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ResourceTreeLeaves;
import lonestarrr.arconia.common.block.ResourceTreeRootBlock;
import lonestarrr.arconia.common.block.RainbowCrateBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.Structures;
import org.apache.logging.log4j.core.jmx.Server;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.lang.Math.floor;
import static java.lang.Math.max;

/**
 * Runs the Resource Tree logic - dispensing loot and all that good stuff
 */
public class ResourceTreeRootTileEntity extends TileEntity implements ITickableTileEntity {
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

    public ResourceTreeRootTileEntity(RainbowColor tier) {
        this(ResourceTreeRootBlock.getTileEntityTypeByTier(tier), tier);
    }

    public ResourceTreeRootTileEntity(TileEntityType<?> tileEntityTypeIn, RainbowColor tier) {
        super(tileEntityTypeIn);
        this.tier = tier;
        RainbowColor nextTier = tier.getNextTier();
        hasNextTier = false;

        if (nextTier != null) {
            hasNextTier = true;
            nextTierLeafBlock = ModBlocks.getMoneyTreeLeaves(nextTier).defaultBlockState();
            leafChanger = new LeafChanger(this, nextTierLeafBlock);
        }
        dispenser = new LeafDropLootDispenser(this, LOOT_DROP_INTERVAL);
    }

    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public void tick() {
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

    private void sendUpdates() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
            setChanged();
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        if (!level.isClientSide()) {
            if (leafChanger != null) {
                compound.put(TAG_LEAF_CHANGER, leafChanger.write());
            }
        }
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (compound.contains(TAG_LEAF_CHANGER)) {
            if (leafChanger != null) {
                leafChanger.read(compound.getCompound(TAG_LEAF_CHANGER));
            }
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        /*
         * Data to be synced from the server to the clients when a client loads a chunk
         */
        return this.save(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        /*
         * Called on the server to generate a packet to be sent to the clients on world.notifyBlockUpdate()
         * May only want to send updates since last time this TE was synced to reduce traffic
         */
        CompoundNBT nbt = new CompoundNBT();
        this.save(nbt);

        // the number here is generally ignored for non-vanilla TileEntities, 0 is safest
        return new SUpdateTileEntityPacket(this.getBlockPos(), 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
        /*
         * This is received on the client after getUpdatePacket() is handled on the server
         */
        super.onDataPacket(net, packet);
        this.load(level.getBlockState(packet.getPos()), packet.getTag());
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


    private final ResourceTreeRootTileEntity tile;
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

    public LeafChanger(@Nonnull ResourceTreeRootTileEntity tile, @Nonnull BlockState toChangeTo) {
        this.tile = tile;
        final int tierNum = tile.getTier().getTier(); // higher tier -> higher ordinal, 1..
        changeInterval = 60 * 20 * (long) Math.pow(1.5, tierNum - 1);
        changeChance = Math.max(5, 100 - tierNum * 15) / 100d;
        this.toChangeTo = toChangeTo; // Block to change leaf into - should be the next tier's leaf block
    }

    /**
     *
     * @return True if state was changed that should be persisted
     */
    public boolean tick() {
        World world = tile.getLevel();
        if (world.isClientSide) {
            return false;
        }

        if (nearbyLeaves == null) {
            nearbyLeaves = findNearbyLeavePositions();
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
        Block leafBlock = ModBlocks.getMoneyTreeLeaves(tile.getTier());

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
    private LinkedList<BlockPos> findNearbyLeavePositions() {
        List<BlockPos> result = new ArrayList<>();

        // Find leaves of the matching tier to potentially change. Any nearby leaf not manually placed will do
        final int scanRadius = 3;
        final int scanHeight = 10;
        BlockPos startPos = tile.getBlockPos().offset(-scanRadius, 0, -scanRadius);
        BlockPos endPos = tile.getBlockPos().offset(scanRadius, scanHeight, scanRadius);
        Block leafBlock = ModBlocks.getMoneyTreeLeaves(tile.getTier());
        for (BlockPos scanPos : BlockPos.betweenClosed(startPos, endPos)) {
            BlockState state = tile.getLevel().getBlockState(scanPos);
            if (state.getBlock().equals(leafBlock) && !state.getValue(LeavesBlock.PERSISTENT)) {
                result.add(scanPos.immutable());
            }
        }

        Collections.shuffle(result);
        return new LinkedList<>(result);
    }

    protected CompoundNBT write() {
        CompoundNBT result = new CompoundNBT();
        result.putInt(TAG_LEAVES_CHANGED, leavesChanged);
        result.putInt(TAG_INTERVAL_COUNT, intervalCount);
        return result;
    }

    protected void read(CompoundNBT nbt) {
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
    final private ResourceTreeRootTileEntity tileEntity;
    final private Random rand;

    public LeafDropLootDispenser(ResourceTreeRootTileEntity tileEntity, int dropInterval) {
        this.dropInterval = dropInterval;
        this.tileEntity = tileEntity;
        lastLeafScanTick = -10000;
        rand = new Random();
    }

    /**
     * Loot is dropped from a leaf at an interval
     */
    public void tick() {
        World world = tileEntity.getLevel();
        if (world.isClientSide) {
            return;
        }

        tickCount++;

        if (tickCount % dropInterval != 0) {
            return;
        }

        // Locate ResourceGen blocks around the tree root that have a resource tree leaf above it of a valid tier
        List<Pair<ResourceGenTileEntity, BlockPos>> generatorsAndLeaves = new ArrayList<>();

        int scanRadius = 1;
        BlockPos startPos = tileEntity.getBlockPos().offset(-scanRadius, 0, -scanRadius);
        BlockPos endPos = tileEntity.getBlockPos().offset(scanRadius, 0, scanRadius);

        for(BlockPos scanPos: BlockPos.betweenClosed(startPos, endPos)) {
            TileEntity te = world.getBlockEntity(scanPos);
            if (te !=null && te instanceof ResourceGenTileEntity) {
                ResourceGenTileEntity rte = (ResourceGenTileEntity) te;
                if (rte.getTier().compareTo(tileEntity.getTier()) <= 0) {
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
            Pair<ResourceGenTileEntity, BlockPos> pair = generatorsAndLeaves.get(i);
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
        BlockPos.Mutable pos = new BlockPos.Mutable();
        while (++y < maxY) {
            pos.set(x, y, z);
            BlockState bs = tileEntity.getLevel().getBlockState(pos);
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
        BlockState state = tileEntity.getLevel().getBlockState(pos);
        if (!(state.getBlock() instanceof ResourceTreeLeaves)) {
            return false;
        }

        ResourceTreeLeaves leaf = (ResourceTreeLeaves)state.getBlock();
        return (leaf.getTier().compareTo(minTier) >= 0);
    }

    /**
     * Dispense loot, such that it appears as if it 'rains' from the leaves.
     * @param leafPos Block position that contains a money tree leaf
     */
    private void dispenseLoot(ResourceGenTileEntity generator, BlockPos leafPos) {
        World world = tileEntity.getLevel();
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