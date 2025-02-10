package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import lonestarrr.arconia.common.core.helper.InventoryHelper;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotMultiBlockPrimaryBlockEntity extends BaseBlockEntity {
    private static final String TAG_RESOURCES = "resources";

    private long lastResourceGenerateTime = 0;
    private RainbowColor detectedTier = null;
    private BlockPos storageBlockPos; // Location of naerby chest/storage
    private final List<ItemStack> generatedResources = new ArrayList<>();
    private static final int maxResources = 64; // TODO make config item

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

    public @Nonnull ItemStack removeResourceGenerated() {
        if (generatedResources.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack resourceItem = generatedResources.get(generatedResources.size() - 1);
        generatedResources.remove(generatedResources.size() - 1);
        setChanged();
        updateClient();
        return resourceItem;
    }

    public RainbowColor getTier() {
        return detectedTier;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotMultiBlockPrimaryBlockEntity blockEntity) {
        blockEntity.generateResources(level, pos, state);
    }

    private void generateResources(Level level, BlockPos pos, BlockState state) {
        if (generatedResources.isEmpty()) {
            return;
        }

        if (level.getGameTime() % 84L == 0) {
            if (storageBlockPos == null) {
                storageBlockPos = locateNearbyStorage();
            }

            if (storageBlockPos == null) {
                BlockPos particlePos = this.worldPosition.above(2);
                Vec3 particleVec = new Vec3(particlePos.getX(), particlePos.getY(), particlePos.getZ()).add(0.5, 1.5, 0.5);
                ServerLevel sLevel = (ServerLevel) level;
                sLevel.sendParticles(ParticleTypes.POOF, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0, 0, 0);
            } else {
                this.detectedTier = detectTier();
            }
        }

        if (storageBlockPos == null) {
            return;
        }

        sendResources(level);
    }

    private void sendResources(Level level) {
        if (storageBlockPos == null) {
            return;
        }

        RainbowColor tier = detectedTier == null ? RainbowColor.RED : detectedTier;

        int interval = ConfigHandler.COMMON.potGenerationInterval.get(tier).get();
        int count = ConfigHandler.COMMON.potGenerationCount.get(tier).get();

        if (detectedTier == null) {
            // TODO A bit weird to not put this in the config, I should probably fix that
            interval = interval * 2;
            count = count / 2;
        }
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
        if (left.getCount() > 0) {
            BlockPos particlePos = worldPosition.above(2);
            ServerLevel sLevel = (ServerLevel)level;
            sLevel.sendParticles(ParticleTypes.SMOKE, particlePos.getX() + 0.5, particlePos.getY() + 0.5, particlePos.getZ() + 0.5, 3, 0, 0.5, 0, 0.05);
        } else {
            PotItemTransferPacket packet = new PotItemTransferPacket(storageBlockPos.above(), worldPosition.above(), toSend);
            ModPackets.sendToNearby(level, worldPosition, packet);

        }
    }

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

    public void writePacketNBT(CompoundTag tag) {
        ListTag resourceListTag = new ListTag();
        generatedResources.forEach(resource -> resourceListTag.add(resource.save(new CompoundTag())));
        tag.put(TAG_RESOURCES, resourceListTag);
    }

    public void readPacketNBT(CompoundTag tag) {
        ListTag resourceListTag = tag.getList(TAG_RESOURCES, Tag.TAG_COMPOUND);
        generatedResources.clear();
        for (int idx = 0; idx < resourceListTag.size(); idx++) {
            if (generatedResources.size() < maxResources) {
                generatedResources.add(ItemStack.of(resourceListTag.getCompound(idx)));
            }
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