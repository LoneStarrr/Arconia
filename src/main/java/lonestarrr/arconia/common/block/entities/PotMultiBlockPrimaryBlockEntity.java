package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.RainbowGrassBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import lonestarrr.arconia.common.core.helper.InventoryHelper;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class PotMultiBlockPrimaryBlockEntity extends BaseBlockEntity {
    // tags for keys for persisted data
    private static final String TAG_RESOURCES = "resources";
    private static final String TAG_ITEM_GEN_CREDITS = "item_gen_credits";
    private static final String TAG_DETECTED_TIER = "detected_tier";
    private static final String TAG_STORAGE_FULL = "storage_full";
    private static final String TAG_STORAGE_BLOCKPOS = "storage_blockpos";

    private final int TREE_SEARCH_RADIUS = 10; // Radius of bounding box around pot

    private int itemGenerationCredits = 0; // TODO persist
    private long lastResourceGenerateTime = 0;
    private long nextTreeScanTime = 0;
    private boolean storageFull = false;

    private RainbowColor detectedTier = null;
    private BlockPos storageBlockPos; // Location of naerby chest/storage
    private final List<ItemStack> generatedResources = new ArrayList<>();
    private static final int maxResources = 64; // TODO make config item
    private TreeLocator.Tree treeToEat = null; // TODO persist

    public PotMultiBlockPrimaryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_MULTIBLOCK_PRIMARY.get(), pos, state);
    }

    public @Nonnull List<ItemStack> getGeneratedResources() { return generatedResources; }

    public boolean addResourceGenerated(ItemStack itemStack) {
        if (generatedResources.size() >= maxResources) {
            return false;
        }
        generatedResources.add(itemStack);
        // Update Entity on client side so the Block Entity Renderer renders correctly
        setChanged();
        updateClient();
        return true;
    }

    public @Nonnull ItemStack removeResourceGenerated(ItemStack resourceToRemove) {
        if (generatedResources.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (resourceToRemove.isEmpty()) {
            ItemStack resourceItem = generatedResources.get(generatedResources.size() - 1);
            removeResourceGeneratedAtIndex(generatedResources.size() - 1);
            return resourceItem;
        } else {
            for (int idx = 0; idx < generatedResources.size(); idx++) {
                ItemStack item = generatedResources.get(idx);
                if (ItemStack.isSameItemSameComponents(item, resourceToRemove)) {
                    removeResourceGeneratedAtIndex(idx);
                    return item;
                }
            }
            return ItemStack.EMPTY;
        }
    }

    private void removeResourceGeneratedAtIndex(int idx) {
        generatedResources.remove(idx);
        // Update Entity on client side so the Block Entity Renderer renders correctly
        setChanged();
        updateClient();
    }

    public boolean isStorageFull() {
        return storageFull;
    }

    public BlockPos getStorageBlockPos() {
        return storageBlockPos;
    }

    private void setStorageBlockPos(BlockPos pos) {
        this.storageBlockPos = pos;
        // Update Entity on client side so the Block Entity Renderer renders correctly
        setChanged();
        updateClient();
    }

    private void setStorageFull(boolean storageFull) {
        this.storageFull = storageFull;
        // Update Entity on client side so the Block Entity Renderer renders correctly
        setChanged();
        updateClient();
    }

    public RainbowColor getTier() {
        return detectedTier;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotMultiBlockPrimaryBlockEntity blockEntity) {
        blockEntity.processTick((ServerLevel)level, pos, state);
    }

    private void processTick(ServerLevel level, BlockPos pos, BlockState state) {
        final long TREE_SCAN_INTERVAL_SECONDS = 5; // TODO config
        final int STORAGE_SCAN_INTERVAL = 84; //move this to main tick()

        if (level.getGameTime() % STORAGE_SCAN_INTERVAL == 0) {
            if (storageBlockPos == null) {
                setStorageBlockPos(locateNearbyStorage());
            }
        }

        if (storageBlockPos == null) {
            return;
        }

        long now = level.getGameTime();

        // This triggers eating a small part of a previously detected tree until it's all gone
        if (treeToEat != null && !treeToEat.isEmpty()) {
            eatTree(level);
        }

        if (itemGenerationCredits > 0) {
            sendResources(level);
        }

        if (now >= nextTreeScanTime){
            nextTreeScanTime = now + TREE_SCAN_INTERVAL_SECONDS * (long) level.tickRateManager().tickrate();

            Map<RainbowColor, List<TreeLocator.Tree>> trees = TreeLocator.locateTrees(this.level, this.worldPosition);
            RainbowColor foundTier = determineTierFromTrees(trees).orElse(null);

            boolean mayEatTree = false;
            /* The tier can be updated when the current trea being eaten hasn't been "used up" yet since it can take a
             * while before that is the case, and we don't want to have the player wait that long to advance a tier.
             */
            if (foundTier != null) {
                // The pot will never go down a previously detected tier. This is to prevent eating
                // lower tier trees if the highest tier tree hasn't been replanted quick enough, which
                // would make automation potentially difficult.
                if (detectedTier == null || foundTier.getTier() >= detectedTier.getTier()) {
                    setDetectedTier(foundTier);
                    mayEatTree = true;
                }
            }

            if (itemGenerationCredits <= 0) {
                if (treeToEat == null || treeToEat.isEmpty()) {
                    if (mayEatTree) {
                        // No remaining credits, tree's been eaten, new one's been found, let's eat the bastard
                        treeToEat = trees.get(foundTier).getFirst();
                    }
                }
            }
        }
    }

    private void showParticleAbovePot(SimpleParticleType pType, Level level) {
        ServerLevel sLevel = (ServerLevel) level;
        BlockPos particlePos = this.worldPosition.above(2);
        Vec3 particleVec = new Vec3(particlePos.getX(), particlePos.getY(), particlePos.getZ()).add(0.5, 1.5, 0.5);
        sLevel.sendParticles(pType, particleVec.x, particleVec.y, particleVec.z, 1, 0, 0, 0, 0.05);
    }

    /**
     * Attempts to send generated items to storage.
     * @param level
     * @return The actual number of items sent to storage
     */
    private int sendResources(Level level) {
        if (storageBlockPos == null || generatedResources.isEmpty()) {
            return 0;
        }

        int tier = detectedTier == null ? 0 : detectedTier.getTier();
        int interval = ConfigHandler.COMMON.potGenerationInterval.get(tier).get();
        int count = Math.min(ConfigHandler.COMMON.potGenerationCount.get(tier).get(), itemGenerationCredits);

        long now = level.getGameTime();
        if (now - lastResourceGenerateTime < interval) {
            return 0;
        }
        lastResourceGenerateTime = now;

        ItemStack toGenerate = this.generatedResources.get(level.random.nextInt(generatedResources.size()));
        IItemHandler inventory = InventoryHelper.getInventory(level, this.storageBlockPos, Direction.UP);
        if (inventory == null) {
            setStorageBlockPos(null);
            return 0;
        }

        ItemStack toSend = toGenerate.copy();
        int sendCount = Math.min(count, toSend.getMaxStackSize());
        toSend.setCount(sendCount);
        ItemStack left = InventoryHelper.insertItem(inventory, toSend, false);
        ServerLevel sLevel = (ServerLevel)level;
        // TODO validate this logic
        int actuallySent = sendCount - left.getCount();
        itemGenerationCredits -= actuallySent;

        if (actuallySent == 0) {
            BlockPos particlePos = worldPosition.above(2);
            sLevel.sendParticles(ParticleTypes.SMOKE, particlePos.getX() + 0.5, particlePos.getY() + 0.5, particlePos.getZ() + 0.5, 3, 0, 0.5, 0, 0.05);
            setStorageFull(true);
        } else {
            PotItemTransferPacket packet = new PotItemTransferPacket(storageBlockPos.above(), worldPosition.above(), toSend);
            ModPackets.sendToNearby(sLevel, worldPosition, packet);
            setStorageFull(false);
        }

        return actuallySent;
    }

    /**
     * Consumes the leaves and trunk of a nearby arconium tree. Each leaf eaten provides 'credits' (fuel) for the pot
     * of gold to use for item generation.
     */
    private void eatTree(ServerLevel level) {
        BlockPos toEatPos = null;
        Block toEatBlock = null;

        if (treeToEat == null || treeToEat.isEmpty()) {
            return;
        }

        while (true) {
            toEatPos = null;
            if (!treeToEat.leaves.isEmpty()) {
                toEatPos = treeToEat.leaves.removeLast();
            } else if (!treeToEat.trunkBlocks.isEmpty()) {
                toEatPos = treeToEat.trunkBlocks.removeLast();
            }

            if (toEatPos == null)
                break; // All tree blocks have been eaten or otherwise processed

            BlockState blockState = level.getBlockState(toEatPos);
            Block block = blockState.getBlock();
            if (!block.equals(Blocks.OAK_LOG) && !(block instanceof ArconiumTreeLeaves))
                toEatPos = null; // The world has changed since we scanned the tree. Just skip over this block.
            else {
                toEatBlock = block;
                break;
            }
        }

        if (toEatPos == null)
            return;

        // play leaves breaking sound
        // show a rainbow particle
        // remove the block
        level.setBlockAndUpdate(toEatPos, Blocks.AIR.defaultBlockState());
        if (toEatBlock instanceof ArconiumTreeLeaves) {
            ArconiumTreeLeaves leavesBlock = (ArconiumTreeLeaves) toEatBlock;
            this.itemGenerationCredits += (int)Math.pow(2, leavesBlock.getTier().getTier()); // TODO tune, from config
            level.sendParticles(ModParticles.RAINBOW_PARTICLES.get(), toEatPos.getX() + 0.5, toEatPos.getY() + 1.5, toEatPos.getZ() + 0.5, 2, 0, 0.02, 0, 0.05);
            level.playSound(null, toEatPos, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 1, 1);
        }
    }

    Optional<RainbowColor> determineTierFromTrees(Map<RainbowColor, List<TreeLocator.Tree>> trees) {
        final int MIN_LEAVES_PER_TIER = 16;
        RainbowColor tierFound = null;

        // The tier is determined by the highest tier leaves found, but all previous tiers must have at least
        // MIN_LEAVES_PER_TIER leaves present. This is to encourage placing actual trees nearby rather than just
        // a single leaves block.
        for (RainbowColor tier: RainbowColor.values()) {
            List<TreeLocator.Tree> tierTrees = trees.getOrDefault(tier, new ArrayList<>());
            int leafCount = tierTrees.stream().mapToInt(ob -> ob.leaves.size()).sum();
            if (leafCount == 0) {
                if (!tierTrees.isEmpty() && !tierTrees.getFirst().trunkBlocks.isEmpty()) {
                    // A tree without leaves but with a trunk does count as a detected tier, because it will make the
                    // pot eat the trunk, allowing (automated) replanting of saplings.
                    tierFound = tier;
                }
                // But no leaves for a lower tier means the detected tier cannot be higher.
                break;
            }
            tierFound = tier;
            if (leafCount < MIN_LEAVES_PER_TIER) {
                break;
            }
        }

        return Optional.ofNullable(tierFound);
    }

    private BlockPos locateNearbyStorage() {
        final int searchRadius = 5;

        if (level == null) {
            return null;
        }

        Optional<BlockPos> storage = BlockPos.findClosestMatch(this.worldPosition, searchRadius, searchRadius, pos -> InventoryHelper.getInventory(level, pos, Direction.UP) != null);
        return storage.orElse(null);
    }

    public RainbowColor getDetectedTier() {
        return detectedTier;
    }

    private void setDetectedTier(RainbowColor detectedTier) {
        this.detectedTier = detectedTier;
        // Update Entity on client side so the Block Entity Renderer renders correctly
        setChanged();
        updateClient();
    }

    public void writePacketNBT(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag resourceListTag = new ListTag();
        generatedResources.forEach(resource -> resourceListTag.add(resource.saveOptional(registries)));
        tag.put(TAG_RESOURCES, resourceListTag);
        tag.putInt(TAG_ITEM_GEN_CREDITS, itemGenerationCredits);
        // This needs to be stored as, while the presence of trees determines the tier, the highest tier tree might have
        // been eaten. It still counts towards the highest tier.
        if (this.detectedTier != null) {
            tag.putInt(TAG_DETECTED_TIER, detectedTier.getTier());
        }
        // This is required for the block entity renderer
        tag.putBoolean(TAG_STORAGE_FULL, storageFull);
        // As is this
        if (storageBlockPos != null) {
            tag.put(TAG_STORAGE_BLOCKPOS, NbtUtils.writeBlockPos(storageBlockPos));
        }
    }

    public void readPacketNBT(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag resourceListTag = tag.getList(TAG_RESOURCES, Tag.TAG_COMPOUND);
        generatedResources.clear();
        for (int idx = 0; idx < resourceListTag.size(); idx++) {
            if (generatedResources.size() < maxResources) {
                generatedResources.add(ItemStack.parseOptional(registries, resourceListTag.getCompound(idx)));
            }
        }
        this.itemGenerationCredits = tag.getInt(TAG_ITEM_GEN_CREDITS);
        int detectedTierInt = tag.getInt(TAG_DETECTED_TIER);
        if (detectedTierInt > 0) {
            this.detectedTier = RainbowColor.byTier(detectedTierInt);
        }
        this.storageFull = tag.getBoolean(TAG_STORAGE_FULL);
        this.storageBlockPos = NbtUtils.readBlockPos(tag, TAG_STORAGE_BLOCKPOS).orElse(null);
    }

    public enum LinkErrorCode { ALREADY_LINKED, TOO_MANY_HATS, HAT_NOT_FOUND, HAT_TOO_FAR, LINKED_TO_OTHER_POT }

    public static class LinkHatException extends Exception {
        public LinkErrorCode code;

        public LinkHatException(LinkErrorCode code) {
            this.code = code;
        }
    }

    public class TreeLocator {
        /**
         * Locate grown arconium trees around the base of the pot in 8 fixed x/z locations
         *
         * @return For each tree found, return the blockpos of the base of the trunk of the tree, and its color.
         */
        public static Map<RainbowColor, List<Tree>> locateTrees(Level level, BlockPos potPos) {
            Map<RainbowColor, List<Tree>> result = new HashMap<>();
            Set<BlockPos> foundTreeSet = new HashSet<>();
            for(int y = potPos.getY() - 1; y <= potPos.getY() + 1; y++) {
                // There are 5 blocks between the center of the pot and the sapling locations in the four cardinal
                // directions. And then there are the four additional corner spots that have both x and x offsets.
                for (int x = potPos.getX() - 6; x <= potPos.getX() + 6; x+= 6) {
                    for (int z = potPos.getZ() - 6; z <= potPos.getZ() + 6; z+= 6) {
                        if (x == potPos.getX() && z == potPos.getZ()) {
                            continue;
                        }

                        if(foundTreeSet.contains(new BlockPos(x, 0, z))) {
                            // Already found a tree at this x/z position, at another y level
                            continue;
                        }

                        // Is there a log at this position? And is it surrounded by colored grass? Then it counts as a
                        // tree trunk base.
                        BlockPos treeBase = new BlockPos(x, y, z);
                        BlockState bs = level.getBlockState(treeBase);
                        if (bs.getBlock().equals(Blocks.OAK_LOG)) {
                            RainbowColor coloredGrassBase = findColoredGrassBase(level, treeBase);
                            if (coloredGrassBase != null) {
                                // It's possible the tree only has a trunk and no leaves. That's ok, the trunk should
                                // be consumed as well to not hinder (automated) replanting.
                                Tree tree = findLeavesAndTrunk(level, treeBase, coloredGrassBase);
                                List<Tree> trees = result.getOrDefault(coloredGrassBase, new ArrayList<Tree>());
                                trees.add(tree);
                                result.put(coloredGrassBase, trees);
                                foundTreeSet.add(new BlockPos(x, 0, z));
                            }
                        }
                    }
                }
            }
            return result;
        }

        private static Tree findLeavesAndTrunk(Level level, BlockPos treeBase, RainbowColor tier) {
            ArrayList<BlockPos> result = new ArrayList<>();
            final int MAX_Y_OFFSET = 12; // Max y position of leaves, relative from base of tree trunk
            List<BlockPos> leaves = new ArrayList<>();
            List<BlockPos> trunkBlocks = new ArrayList<>();

            int startY = treeBase.getY();

            // Add blocks lower-y first so that eating the tree will eat them from the top down as they pop the
            // last element off the list.
            for (int y = startY; y < treeBase.getY() + MAX_Y_OFFSET; y++) {
                for (int x = treeBase.getX() - 3; x <= treeBase.getX() + 3; x++) {
                    for (int z = treeBase.getZ() - 3; z <= treeBase.getZ() + 3; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        Block b = level.getBlockState(pos).getBlock();
                        if (b instanceof ArconiumTreeLeaves leavesBlock) {
                            if (leavesBlock.getTier().equals(tier)) {
                                leaves.add(pos);
                            }
                        } else if (b.equals(Blocks.OAK_LOG)) {
                            trunkBlocks.add(pos);
                        }
                    }
                }
            }

            return new Tree(treeBase, leaves, trunkBlocks, tier);
        }

        private static RainbowColor findColoredGrassBase(Level level, BlockPos logPos) {
            RainbowColor result = null;

            // All blocks surrounding the logPos at 1 level lower must be colored grass blocks of the same tier.
            int y = logPos.getY() - 1;
            for (int x = logPos.getX() - 1; x <= logPos.getX() + 1; x++) {
                for (int z = logPos.getZ() - 1; z <= logPos.getZ() + 1; z++) {
                    if (x == logPos.getX() && z == logPos.getZ())
                        continue;
                    Block b = level.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (b instanceof RainbowGrassBlock grassBlock) {
                        if (result == null)
                            result = grassBlock.getTier();
                        else if (!result.equals(grassBlock.getTier())) {
                            // Must all be of the same color
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            }
            return result;
        }

        public record Tree(
                BlockPos trunkBasePos,
                List<BlockPos> leaves,
                List<BlockPos> trunkBlocks,
                RainbowColor color
        ){
            public boolean isEmpty() {
                return leaves.isEmpty() && trunkBlocks.isEmpty();
            }
        }
    }
}