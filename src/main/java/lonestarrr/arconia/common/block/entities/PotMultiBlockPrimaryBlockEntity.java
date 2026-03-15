package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.client.particle.ModParticles;
import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.RainbowGrassBlock;
import lonestarrr.arconia.common.core.LevelBlockAccess;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.SimplifiedLevel;
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
import net.minecraft.nbt.IntTag;
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
    private static final String TAG_BONUS_TREE_COLORS = "bonus_tree_colors";

    private final int TREE_SEARCH_RADIUS = 10; // Radius of bounding box around pot

    private int itemGenerationCredits = 0; // TODO persist
    private long lastResourceGenerateTime = 0;
    private long nextTreeScanTime = 0;
    private boolean storageFull = false;
    private int numBonusTrees = 0;
    private Set<RainbowColor> bonusTreeColors = new HashSet<>();

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

    public RainbowColor getTier() {
        return detectedTier;
    }

    public Set<RainbowColor> getBonusTreeColors() {
        return bonusTreeColors;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotMultiBlockPrimaryBlockEntity blockEntity) {
        blockEntity.processTick((ServerLevel)level, pos, state);
    }

    private void processTick(ServerLevel level, BlockPos pos, BlockState state) {
        final long TREE_SCAN_INTERVAL = 97;
        final int STORAGE_SCAN_INTERVAL = 84;
        boolean syncToClient = false;

        if (level.getGameTime() % STORAGE_SCAN_INTERVAL == 0) {
            if (storageBlockPos == null) {
                this.storageBlockPos = locateNearbyStorage();
                if (this.storageBlockPos != null) {
                    syncToClient = true;
                }
            }
        }

        if (storageBlockPos != null) {
            long now = level.getGameTime();

            // This triggers eating a small part of a previously detected tree until it's all gone
            if (treeToEat != null && !treeToEat.isEmpty()) {
                int itemCredits = this.itemGenerationCredits;
                eatTree(level);
                if (itemCredits <= 0 && this.itemGenerationCredits > 0) {
                    // Update client to let it know we are no longer starved
                    syncToClient = true;
                }
            }

            displayTierParticles(level);

            if (itemGenerationCredits > 0) {
                try {
                    if (sendResources(level) > 0) {
                        // Clearly we have recovered from a storage full situation
                        if (this.storageFull) {
                            this.storageFull = false;
                            syncToClient = true;
                        }
                    }
                    if (this.itemGenerationCredits <= 0) {
                        syncToClient = true; // let the client know we ran out, so it can be visualized
                    }
                } catch (StorageFullException e) {
                    if (!this.storageFull) {
                        this.storageFull = true;
                        syncToClient = true;
                    }
                } catch (StorageMissingException e) {
                    if (this.storageBlockPos != null) {
                        this.storageBlockPos = null;
                        syncToClient = true;
                    }

                }
            }

            if (now >= nextTreeScanTime) {
                nextTreeScanTime = now + TREE_SCAN_INTERVAL;

                Map<RainbowColor, List<TreeLocator.Tree>> trees = TreeLocator.locateTrees(new LevelBlockAccess(this.level), this.worldPosition);
                RainbowColor foundTier = determineTierFromTrees(trees).orElse(null);

                if (itemGenerationCredits > 0) {
                    // Still eating a previous tree. In that case, make sure we don't go down a tier because we may have
                    // eaten a tree of that tier and that should make the tier stick until we've exhausted the credits.
                    // However, if we find a higher tier tree, it's fine to go up because we want to reward the player for
                    // planting down a higher tier tree at all times.
                    if (foundTier != null && (detectedTier == null || foundTier.getTier() > detectedTier.getTier())) {
                        this.detectedTier = foundTier;
                        syncToClient = true;
                    }
                } else {
                    if (foundTier != null && (treeToEat == null || treeToEat.isEmpty())) {
                        // No remaining credits, tree's been eaten, new one's been found, let's eat the bastard
                        treeToEat = trees.get(foundTier).getFirst();
                        this.detectedTier = foundTier;
                        syncToClient = true;
                    }
                }

                // We want to update bonus trees more frequently and not just at tier changes as they can be added and
                // removed meanwhile.
                if (detectedTier != null) {
                    Set<RainbowColor> newBonusTreeColors = findBonusTrees(trees, foundTier);
                    if (!newBonusTreeColors.equals(this.bonusTreeColors)) {
                        this.bonusTreeColors = newBonusTreeColors;
                        syncToClient = true;
                    }
                }
            }
        }

        if (syncToClient) {
            setChanged();
            updateClient();
        }
    }

    private Set<RainbowColor> findBonusTrees(@Nonnull Map<RainbowColor, List<TreeLocator.Tree>> trees, @Nonnull RainbowColor treeTier) {
        /* The bonus value: For each tier below the detected tier, check if there's a tree present (based on minimum
         * leaf count). If so, that adds one bonus multiplier. More trees for the same color do not add to it.
         * The total bonus is then a % from config to the power of the multiplier.
         * The bonus represents extra resources pulled from the pot, e.g. a bonus of 250% means 2 extra draws, and 50%
         * chance on another draw.
         */
        final int MIN_LEAVES_COUNT = 32; // minimum leaves count for a tree to be considered present for bonus reasons

        List<Map.Entry<RainbowColor, List<TreeLocator.Tree>>> bonusEntries = trees.entrySet().stream()
                // Filter only those entries where the RainbowColor's tier is smaller than the threshold and the
                // trees for that color have enough leaves to count as a "bonus tree"
                .filter(
                        entry -> entry.getKey().getTier() < treeTier.getTier()
                                && !entry.getValue().isEmpty()
                                && entry.getValue().stream().mapToInt(ob -> ob.leaves.size()).sum() >= MIN_LEAVES_COUNT
                ).toList();
        this.numBonusTrees = bonusEntries.size();
        Set<RainbowColor> newBonusTreeColors = new HashSet<>(bonusEntries.stream().map(Map.Entry::getKey).toList());
        return newBonusTreeColors;
    }

    private void displayTierParticles(ServerLevel level) {
        if (this.detectedTier != null && level.getGameTime() % 4 == 0) {
            List<BlockPos> ring = new ArrayList<>();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                        ring.add(this.worldPosition.offset(dx, 0, dz));
                    }
                }
            }
            BlockPos pos = ring.get(level.random.nextInt(ring.size()));
            SimpleParticleType pType = ModParticles.RAINBOW_PARTICLES.get();
            switch(this.detectedTier) {
                case RED -> pType = ModParticles.RAINBOW_PARTICLES_RED.get();
                case ORANGE -> pType = ModParticles.RAINBOW_PARTICLES_ORANGE.get();
                case YELLOW -> pType = ModParticles.RAINBOW_PARTICLES_YELLOW.get();
                case GREEN -> pType = ModParticles.RAINBOW_PARTICLES_GREEN.get();
                case LIGHT_BLUE -> pType = ModParticles.RAINBOW_PARTICLES_LIGHT_BLUE.get();
                case BLUE -> pType = ModParticles.RAINBOW_PARTICLES_BLUE.get();
                case PURPLE -> pType = ModParticles.RAINBOW_PARTICLES_PURPLE.get();
            }
            level.sendParticles(pType, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0.02, 0, 0.01);
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
     * @return Number of items actually sent to storage
     */
    private int sendResources(Level level) throws StorageFullException, StorageMissingException {
        if (storageBlockPos == null || generatedResources.isEmpty() || detectedTier == null) {
            return 0;
        }

        int interval = ConfigHandler.COMMON.potGenerationInterval.get(detectedTier).get();

        long now = level.getGameTime();
        if (now - lastResourceGenerateTime < interval) {
            return 0;
        }
        lastResourceGenerateTime = now;

        IItemHandler inventory = InventoryHelper.getInventory(level, this.storageBlockPos, Direction.UP);
        if (inventory == null) {
            throw new StorageMissingException();
        }

        int drawCount = 1;

        if (this.numBonusTrees > 0) {
            int bonusChancePerTree = ConfigHandler.COMMON.bonusPerExtraTree.getAsInt();
            int bonusChance = numBonusTrees * bonusChancePerTree;
            drawCount += bonusChance / 100;
            int extraDrawChancePct = bonusChance % 100;
            if (level.random.nextInt(100) < extraDrawChancePct) {
                drawCount++;
            }
        }

        /**
         * Even is storage is full, continue looping, it may not be full for another item
         */
        int totallySent = 0;

        for (int i = 0; i < drawCount; i++) {
            if (itemGenerationCredits <= 0) {
                break;
            }
            int maxSendCount = Math.min(ConfigHandler.COMMON.potGenerationCount.get(detectedTier).get(), itemGenerationCredits);
            ItemStack toGenerate = this.generatedResources.get(level.random.nextInt(generatedResources.size()));

            ItemStack toSend = toGenerate.copy();
            int sendCount = Math.min(maxSendCount, toSend.getMaxStackSize());
            toSend.setCount(sendCount);
            ItemStack left = InventoryHelper.insertItem(inventory, toSend, false);
            ServerLevel sLevel = (ServerLevel) level;
            // TODO validate this logic
            int actuallySent = sendCount - left.getCount();
            itemGenerationCredits -= actuallySent;

            if (actuallySent == 0) {
                BlockPos particlePos = worldPosition.above(2);
                sLevel.sendParticles(ParticleTypes.SMOKE, particlePos.getX() + 0.5, particlePos.getY() + 0.5, particlePos.getZ() + 0.5, 3, 0, 0.5, 0, 0.05);
                throw new StorageFullException();
            } else {
                PotItemTransferPacket packet = new PotItemTransferPacket(storageBlockPos.above(), worldPosition.above(), toSend);
                ModPackets.sendToNearby(sLevel, worldPosition, packet);
                totallySent += actuallySent;
            }
        }

        return totallySent;
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
            this.itemGenerationCredits += ConfigHandler.COMMON.leavesItemCredits.get(leavesBlock.getTier()).getAsInt();
            level.sendParticles(ModParticles.RAINBOW_PARTICLES.get(), toEatPos.getX() + 0.5, toEatPos.getY() + 1.5, toEatPos.getZ() + 0.5, 2, 0, 0.02, 0, 0.05);
            level.playSound(null, toEatPos, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 1, 1);
        }
    }

    Optional<RainbowColor> determineTierFromTrees(Map<RainbowColor, List<TreeLocator.Tree>> trees) {
        RainbowColor tierFound = null;

        // The tier is determined by the highest tier tree found. Even if they do not have leaves, as long as the
        // colored grass area at the base is there it counts. This is to avoid getting stuck with automation if a tree
        // was only partially eaten and just the trunk is left behind.
        for (RainbowColor tier: Arrays.stream(RainbowColor.values()).toList().reversed()) {
            List<TreeLocator.Tree> tierTrees = trees.getOrDefault(tier, new ArrayList<>());
            if (!tierTrees.isEmpty()) {
                tierFound = tier;
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

    public int getItemGenerationCredits() {
        return itemGenerationCredits;
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
        ListTag bonusTreeColorsTag = new ListTag();
        for (RainbowColor color : bonusTreeColors) {
            bonusTreeColorsTag.add(IntTag.valueOf(color.getTier()));
        }
        tag.put(TAG_BONUS_TREE_COLORS, bonusTreeColorsTag);
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
        ListTag bonusTreeColorsTag = tag.getList(TAG_BONUS_TREE_COLORS, Tag.TAG_INT);
        this.bonusTreeColors = new HashSet<>();
        for (int i = 0; i < bonusTreeColorsTag.size(); i++) {
            RainbowColor color = RainbowColor.byTier(bonusTreeColorsTag.getInt(i));
            if (color != null) {
                this.bonusTreeColors.add(color);
            }
        }
    }

    public static class StorageFullException extends Exception {
        public StorageFullException() {}
    }

    public static class StorageMissingException extends Exception {
        public StorageMissingException() {}
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
        public static Map<RainbowColor, List<Tree>> locateTrees(SimplifiedLevel level, BlockPos potPos) {
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

        private static Tree findLeavesAndTrunk(SimplifiedLevel level, BlockPos treeBase, RainbowColor tier) {
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

        private static RainbowColor findColoredGrassBase(SimplifiedLevel level, BlockPos logPos) {
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