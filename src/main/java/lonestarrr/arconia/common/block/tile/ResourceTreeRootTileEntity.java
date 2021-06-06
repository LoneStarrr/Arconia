package lonestarrr.arconia.common.block.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.lang.Math.floor;
import static java.lang.Math.max;

public class ResourceTreeRootTileEntity extends TileEntity implements ITickableTileEntity {
    private static final int LOOT_DROP_INTERVAL = 100; // How often to drop loot
    private LootTable lootTable;
    private RainbowColor tier;
    private int tickCount;
    private LootDispenser dispenser;
    private final Random rand = new Random();
    private static final Logger LOGGER = LogManager.getLogger();

    public ResourceTreeRootTileEntity(RainbowColor tier) {
        this(ResourceTreeRootBlock.getTileEntityTypeByTier(tier), tier);
    }

    public ResourceTreeRootTileEntity(TileEntityType<?> tileEntityTypeIn, RainbowColor tier) {
        super(tileEntityTypeIn);
        this.tier = tier;
    }

    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public void tick() {
        tickCount++;

        if (!world.isRemote()) {
            if (dispenser == null) {
                updateDispenser();
            }
            dispenser.tick();
        }
    }

    private void sendUpdates() {
        if (world != null && !world.isRemote()) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            markDirty();
        }
    }

    private void updateDispenser() {
        if (world.isRemote()) {
            return;
        }

        this.dispenser = new LeafDropLootDispenser(this, (ServerWorld) world, LOOT_DROP_INTERVAL);
   }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if (!world.isRemote()) {
            /// write data if I have any
        }
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        /*
         * Data to be synced from the server to the clients when a client loads a chunk
         */
        return this.write(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        /*
         * Called on the server to generate a packet to be sent to the clients on world.notifyBlockUpdate()
         * May only want to send updates since last time this TE was synced to reduce traffic
         */
        CompoundNBT nbt = new CompoundNBT();
        this.write(nbt);

        // the number here is generally ignored for non-vanilla TileEntities, 0 is safest
        return new SUpdateTileEntityPacket(this.getPos(), 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
        /*
         * This is received on the client after getUpdatePacket() is handled on the server
         */
        super.onDataPacket(net, packet);
        this.read(world.getBlockState(packet.getPos()), packet.getNbtCompound());
    }
}

interface LootDispenser {
    public void tick();
}

class LeafDropLootDispenser implements LootDispenser {
    private static final int MAX_LEAVES = 20; // How many leaves to look for to drop loot from
    private static final int LEAF_SCAN_INTERVAL = 200; // How often to check for leaf updates

    final private ServerWorld world;
    private List<BlockPos> foundLeaves = new ArrayList<>(MAX_LEAVES);
    private int lastLeafScanTick;
    private int tickCount = 0;
    final private int dropInterval;
    final private ResourceTreeRootTileEntity tileEntity;
    final private Random rand;

    public LeafDropLootDispenser(ResourceTreeRootTileEntity tileEntity, ServerWorld world, int dropInterval) {
        this.world = world;
        this.dropInterval = dropInterval;
        this.tileEntity = tileEntity;
        lastLeafScanTick = -10000;
        rand = new Random();
    }

    /**
     * Loot is dropped from a leaf at an interval
     */
    public void tick() {
        tickCount++;

        if (tickCount % dropInterval != 0) {
            return;
        }

        // Locate ResourceGen blocks around the tree root that have a resource tree leaf above it of a valid tier
        List<Pair<ResourceGenTileEntity, BlockPos>> generatorsAndLeaves = new ArrayList<>();

        int scanRadius = 1;
        BlockPos startPos = tileEntity.getPos().add(-scanRadius, 0, -scanRadius);
        BlockPos endPos = tileEntity.getPos().add(scanRadius, 0, scanRadius);

        for(BlockPos scanPos: BlockPos.getAllInBoxMutable(startPos, endPos)) {
            TileEntity te = world.getTileEntity(scanPos);
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
            pos.setPos(x, y, z);
            BlockState bs = world.getBlockState(pos);
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
        BlockState state = world.getBlockState(pos);
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
        if (world.isRemote()) {
            return;
        }

        ItemStack plunder = generator.getItemStack(); // Includes item count
        if (!plunder.isEmpty()) {
            ItemEntity entity = new ItemEntity(world, leafPos.getX() + 0.5D, leafPos.getY() - 1 + 0.5D,
                    leafPos.getZ() + 0.5D,
                    plunder);
            entity.setMotion(0D, 0.0D, 0D);
            entity.setDefaultPickupDelay();
            entity.lifespan = 200; // Short lifespan - encourage players to use the crates, plus it's server-friendlier
            world.addEntity(entity);
        }
    }
}