package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.TreeFinder;
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
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.openjdk.nashorn.internal.runtime.options.Option;

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
    private TreeFinder.Tree treeToEat = null; // TODO persist

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
        final int NO_CREDITS_WARN_INTERVAL = 20;

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

        // We scan for trees even when the credits gained from eating the last tree hasn't been fully 'used up'
        // because the user may have planted a higher tier tree meanwhile. While we don't want to instantly
        // consume it, we do want a potential higher tier to become effective quickly.
        if (now >= nextTreeScanTime){
            LeafCountResult lcf = countNearbyLeaves(level);
            RainbowColor newTier = determineTierFromLeaves(lcf).orElse(null);

            if (itemGenerationCredits > 0) {
                // Only if the detected tier is higer do we update the detected tier. Because we immediately eat the
                // highest tier tree when looking for a new tree, and thus a scan right after would show a lower tier.
                if (newTier != null && newTier.getTier() > detectedTier.getTier()) {
                    setDetectedTier(newTier);
                }
            } else {
                if (treeToEat == null || treeToEat.isEmpty()) {
                    // No credits, tree's been eaten, let's eat a new one.
                    setDetectedTier(newTier);

                    // Eat a tree with the leaves of the detected tier. Trees with higher tier leaves MAY
                    // exist, but they do not count if not all the previous tiers are present.
                    if (newTier != null) {
                        BlockPos first = this.worldPosition.offset(-TREE_SEARCH_RADIUS, -TREE_SEARCH_RADIUS, -TREE_SEARCH_RADIUS);
                        BlockPos second = this.worldPosition.offset(TREE_SEARCH_RADIUS, TREE_SEARCH_RADIUS, TREE_SEARCH_RADIUS);
                        List<TreeFinder.Tree> trees = TreeFinder.findTrees(
                                level, Blocks.OAK_LOG.defaultBlockState(),
                                ModBlocks.getArconiumTreeLeaves(newTier).get().defaultBlockState(),
                                first,
                                second
                        );
                        if (!trees.isEmpty()) {
                            treeToEat = trees.getFirst();
                        }
                    }
                }
            }
            nextTreeScanTime = now + TREE_SCAN_INTERVAL_SECONDS* (long) level.tickRateManager().tickrate();
        }
    }

    private void showParticleAbovePot(SimpleParticleType pType, Level level) {
        ServerLevel sLevel = (ServerLevel) level;
        BlockPos particlePos = this.worldPosition.above(2);
        Vec3 particleVec = new Vec3(particlePos.getX(), particlePos.getY(), particlePos.getZ()).add(0.5, 1.5, 0.5);
        sLevel.sendParticles(pType, particleVec.x, particleVec.y, particleVec.z, 1, 0, 0, 0, 0.05);
    }

    private TreeFinder.Tree findNewTreeToEat(Level level) {
        LeafCountResult lcf = countNearbyLeaves(level);

        return null;
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
            if (!treeToEat.leafBlocks().isEmpty()) {
                toEatPos = treeToEat.leafBlocks().removeLast();
            } else if (!treeToEat.woodBlocks().isEmpty()) {
                toEatPos = treeToEat.woodBlocks().removeLast();
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

    Optional<RainbowColor> determineTierFromLeaves(LeafCountResult leafCounts) {
        final int MIN_LEAVES_PER_TIER = 16;
        RainbowColor tierFound = null;

        // The tier is determined by the highest tier leaves found, but all previous tiers must have at least
        // MIN_LEAVES_PER_TIER leaves present. This is to encourage placing actual trees nearby rather than just
        // a single leaves block.
        for (RainbowColor tier: RainbowColor.values()) {
            int leafCount = leafCounts.countByTier.getOrDefault(tier, 0);
            if (leafCount == 0) {
                break;
            }
            tierFound = tier;
            if (leafCount < MIN_LEAVES_PER_TIER) {
                break;
            }
        }

        return Optional.ofNullable(tierFound);
    }

    private LeafCountResult countNearbyLeaves(Level level) {
        BlockPos first = this.worldPosition.offset(-TREE_SEARCH_RADIUS, -TREE_SEARCH_RADIUS, -TREE_SEARCH_RADIUS);
        BlockPos second = this.worldPosition.offset(TREE_SEARCH_RADIUS, TREE_SEARCH_RADIUS, TREE_SEARCH_RADIUS);
        Map<RainbowColor, Integer> result = new HashMap<>();
        RainbowColor highestTierLeaf = null;

        for (BlockPos pos: BlockPos.betweenClosed(first, second)) {
            BlockState bs = level.getBlockState(pos);
            if (bs.getBlock() instanceof ArconiumTreeLeaves leaves) {
                result.put(leaves.getTier(), result.getOrDefault(leaves.getTier(), 0) + 1);

                // Find the highest tier leaf, specifically the one nearest to the pot
                if (highestTierLeaf == null || highestTierLeaf.getTier() < leaves.getTier().getTier()) {
                    highestTierLeaf = leaves.getTier();
                }

            }
        }

        return new LeafCountResult(result, highestTierLeaf);
    }

    private record LeafCountResult(
        Map<RainbowColor, Integer> countByTier,
        RainbowColor highestTierLeaf
    ) {}

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

    /**
     * Detect the tier for this pot of gold. Tiering is determined by rings of rainbow grass in color order surrounding the pot.
     * A ring is square, and each tier is 2 blocks wider in diameter than the previous.
     * The first ring is a 5x5 ring.
     * @return The detected tier, or null of nu tier was detected.
     */
    private RainbowColor detectTier() {
        BlockPos centerPos = this.worldPosition.below(); // check ground level directly under pot
        RainbowColor detectedTier = null;

        for (RainbowColor tier: RainbowColor.values()) {
            int ringDiameter = tier.getTier() * 2 + 3; // 5, 7, .., 17
            BlockState bs = ModBlocks.getRainbowGrassBlock(tier).get().defaultBlockState();
            if (!detectRing(centerPos, ringDiameter, bs)) {
                break;
            } else {
                detectedTier = tier;
            }

        }

        return detectedTier;
    }

    private boolean detectRing(BlockPos centerPos, int diameter, BlockState ringBlock) {
        int transformX = centerPos.getX() - ((diameter - (diameter % 2)) / 2);
        int transformZ = centerPos.getZ() - ((diameter - (diameter % 2)) / 2);
        boolean ringBlocksMatch = true;

        if (level == null) {
            return false;
        }

        for (int z = 0; z < diameter; z++) {
            int xIncrement = (z == 0 || z == diameter - 1) ? 1 : diameter - 1;
            for (int x = 0; x < diameter; x += xIncrement) {
                BlockPos toCheck = new BlockPos(x + transformX, centerPos.getY(), z + transformZ);
                BlockState bs = level.getBlockState(toCheck);
                if (!bs.equals(ringBlock)) {
                    ringBlocksMatch = false;
                    break;
                }
            }
            if (!ringBlocksMatch) {
                break;
            }
        }

        return ringBlocksMatch;
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
}