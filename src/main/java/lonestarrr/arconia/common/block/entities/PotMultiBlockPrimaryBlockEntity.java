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

    private int itemGenerationCredits = 0; // TODO persist
    private long lastResourceGenerateTime = 0;
    private long nextTreeScanTime = 0;

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
        setChanged();
        updateClient();
    }

    public RainbowColor getTier() {
        return detectedTier;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotMultiBlockPrimaryBlockEntity blockEntity) {
        blockEntity.processTick(level, pos, state);
    }

    private void processTick(Level level, BlockPos pos, BlockState state) {
        final long TREE_SCAN_INTERVAL_SECONDS = 10; // TODO config

        final int STORAGE_SCAN_INTERVAL = 84; //move this to main tick()
        final int NO_CREDITS_WARN_INTERVAL = 20;
        final int TREE_EAT_INTERVAL = 1;

        if (level.getGameTime() % STORAGE_SCAN_INTERVAL == 0) {
            if (storageBlockPos == null) {
                storageBlockPos = locateNearbyStorage();
            }

            if (storageBlockPos == null) {
                // TODO particle with a chest item to indicate there is a storage issue
                showParticleAbovePot(ParticleTypes.POOF, level);
            }
        }

        if (storageBlockPos == null) {
            return;
        }

        long now = level.getGameTime();

        if (now % TREE_EAT_INTERVAL == 0 && treeToEat != null && !treeToEat.isEmpty()) {
            eatTree((ServerLevel)level);
        }

        if (itemGenerationCredits > 0) {
            sendResources(level); //updates itemGenerationCount
            return;
        } else {
            if (now % NO_CREDITS_WARN_INTERVAL == 0) {
                showParticleAbovePot(ParticleTypes.POOF, level);
                // TODO also display an item icon indicating no credits (tree leaves block?)
            }
        }
        // TODO particle indicating there are no itemGeneration credits - sapling + smoke?

        // No trees to eat, no more item generation credits, let's find some new trees to eat.
        if (treeToEat == null || treeToEat.isEmpty()) {
            if (now < nextTreeScanTime) {
                return; // Too soon.
            } else {
                // In case no trees are found, don't immediately go scanning for trees on the next loop
                nextTreeScanTime = now + TREE_SCAN_INTERVAL_SECONDS * (long) level.tickRateManager().tickrate();
                treeToEat = findNewTreeToEat(level);
            }
        }
    }

    private void showParticleAbovePot(SimpleParticleType pType, Level level) {
        ServerLevel sLevel = (ServerLevel) level;
        BlockPos particlePos = this.worldPosition.above(2);
        Vec3 particleVec = new Vec3(particlePos.getX(), particlePos.getY(), particlePos.getZ()).add(0.5, 1.5, 0.5);
        sLevel.sendParticles(pType, particleVec.x, particleVec.y, particleVec.z, 1, 0, 0, 0, 0.05);
    }

    private TreeFinder.Tree findNewTreeToEat(Level level) {
        final int TREE_SEARCH_RADIUS = 10; // Radius of bounding box around pot
        BlockPos first = this.worldPosition.offset(-TREE_SEARCH_RADIUS, -TREE_SEARCH_RADIUS, -TREE_SEARCH_RADIUS);
        BlockPos second = this.worldPosition.offset(TREE_SEARCH_RADIUS, TREE_SEARCH_RADIUS, TREE_SEARCH_RADIUS);
        LeafCountResult lcf = countNearbyLeaves(level, first, second);
        RainbowColor leavesTier = lcf.highestTierLeaf;
        // The tier of the pot is not necessarily the highest tier leaves found
        this.detectedTier = determineTierFromLeaves(lcf); // TODO indicate with particles of the right color

        // Eat a tree associated with the highest tier found.
        if (leavesTier != null) {
            List<TreeFinder.Tree> trees = TreeFinder.findTrees(
                    level, Blocks.OAK_LOG.defaultBlockState(),
                    ModBlocks.getArconiumTreeLeaves(leavesTier).get().defaultBlockState(),
                    first,
                    second
            );
            if (!trees.isEmpty()) {
                return trees.getFirst();
            }
        }

        return null;
    }

    private void sendResources(Level level) {
        if (storageBlockPos == null || generatedResources.isEmpty()) {
            return;
        }

        int tier = detectedTier == null ? 0 : detectedTier.getTier();
        int interval = ConfigHandler.COMMON.potGenerationInterval.get(tier).get();
        int count = Math.min(ConfigHandler.COMMON.potGenerationCount.get(tier).get(), this.itemGenerationCredits);

        long now = level.getGameTime();
        if (now - lastResourceGenerateTime < interval) {
            return;
        }
        lastResourceGenerateTime = now;

        ItemStack toGenerate = this.generatedResources.get(level.random.nextInt(generatedResources.size()));
        IItemHandler inventory = InventoryHelper.getInventory(level, this.storageBlockPos, Direction.UP);
        if (inventory == null) {
            this.storageBlockPos = null;
            return;
        }

        ItemStack toSend = toGenerate.copy();
        int sendCount = Math.min(count, toSend.getMaxStackSize());
        toSend.setCount(sendCount);
        ItemStack left = InventoryHelper.insertItem(inventory, toSend, false);
        ServerLevel sLevel = (ServerLevel)level;
        // TODO validate this logic
        int actuallySent = sendCount - left.getCount();
        this.itemGenerationCredits -= actuallySent;

        if (actuallySent == 0) {
            BlockPos particlePos = worldPosition.above(2);
            sLevel.sendParticles(ParticleTypes.SMOKE, particlePos.getX() + 0.5, particlePos.getY() + 0.5, particlePos.getZ() + 0.5, 3, 0, 0.5, 0, 0.05);
        } else {
            PotItemTransferPacket packet = new PotItemTransferPacket(storageBlockPos.above(), worldPosition.above(), toSend);
            ModPackets.sendToNearby(sLevel, worldPosition, packet);
        }
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
    RainbowColor determineTierFromLeaves(LeafCountResult leafCounts) {
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

        return tierFound;
    }

    private LeafCountResult countNearbyLeaves(Level level, final BlockPos first, final BlockPos second) {
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
    }

    public enum LinkErrorCode { ALREADY_LINKED, TOO_MANY_HATS, HAT_NOT_FOUND, HAT_TOO_FAR, LINKED_TO_OTHER_POT }

    public static class LinkHatException extends Exception {
        public LinkErrorCode code;

        public LinkHatException(LinkErrorCode code) {
            this.code = code;
        }
    }
}