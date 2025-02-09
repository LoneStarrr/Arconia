package lonestarrr.arconia.common.block.entities;

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
    private static final String TAG_HAT_POSITIONS = "hat_positions";
    private static final String TAG_RESOURCES = "resources";
    public static final int STORAGE_SCAN_INTERVAL = 100; // How frequently to search for nearby storage

    private long lastIntervalGameTime = 0;
    private long lastStorageScanTime = 0;
    private long lastResourceGenerateTime = 0;
    private BlockPos storageBlockPos; // Location of naerby chest/storage
    private final List<ItemStack> generatedResources = new ArrayList<>();
    private static final int maxResources = 64; // TODO make config item

    public PotMultiBlockPrimaryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_MULTIBLOCK_PRIMARY.get(), pos, state);
        // TODO check for valid structure at an interval, if not, destroy ourselves
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
        return RainbowColor.RED; // TODO (re)implement
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotMultiBlockPrimaryBlockEntity blockEntity) {
        blockEntity.generateResources(level, pos, state);
    }

    private void generateResources(Level level, BlockPos pos, BlockState state) {
        if (generatedResources.isEmpty()) {
            return;
        }

        locateNearbyStorage(level);

        if (storageBlockPos == null) {
            return;
        }

        sendResources(level);
    }

    private void sendResources(Level level) {
        if (storageBlockPos == null) {
            return;
        }

        RainbowColor tier = RainbowColor.RED; /// TODO tiering
        int interval = ConfigHandler.COMMON.potGenerationInterval.get(tier).get();
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
        int count = ConfigHandler.COMMON.potGenerationCount.get(tier).get();
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

    private void locateNearbyStorage(Level level) {
        if (storageBlockPos == null && level.getGameTime() - lastStorageScanTime >= STORAGE_SCAN_INTERVAL) {
            final int searchRadius = 5;
            Optional<BlockPos> storage = BlockPos.findClosestMatch(this.worldPosition, searchRadius, searchRadius, pos -> InventoryHelper.getInventory(level, pos, Direction.UP) != null);
            storage.ifPresent(blockPos -> this.storageBlockPos = blockPos);
            this.lastStorageScanTime = level.getGameTime();

            if (storageBlockPos == null) {
                BlockPos particlePos = this.worldPosition.above(2);
                Vec3 particleVec = new Vec3(particlePos.getX(), particlePos.getY(), particlePos.getZ()).add(0.5, 1.5, 0.5);
                ServerLevel sLevel = (ServerLevel) level;
                sLevel.sendParticles(ParticleTypes.POOF, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0, 0, 0);
            }
        }
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