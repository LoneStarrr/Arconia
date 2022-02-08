package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import lonestarrr.arconia.common.item.ModItems;
import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PotMultiBlockPrimaryTileEntity extends BaseTileEntity implements TickableBlockEntity {
    private static final int MAX_COIN_SUPPLIERS = 1; // How many hats may supply coins per coin collection tick?
    private static final String TAG_HAT_POSITIONS = "hat_positions";
    private static final String TAG_COIN_COUNT = "coin_count";

    private int coinCount;
    private int intervalsElapsed = 0;
    private long lastIntervalGameTime = 0;

    private final List<HatData> hats = new ArrayList<>();

    public PotMultiBlockPrimaryTileEntity() {
        super(ModTiles.POT_MULTIBLOCK_PRIMARY);
        // TODO check for valid structure at an interval, if not, destroy ourselves
        coinCount = 0;
    }

    public int addCoins(int count) {
        coinCount += count;
        setChanged();
        return count;
    }

    public final long getCoinCount() {
        return coinCount;
    }

    public int maxHats() {
        return ConfigHandler.COMMON.potOfGoldMaxHats.get();
    }

    public int ticksPerInterval() {
        return ConfigHandler.COMMON.potOfGoldTicksPerInterval.get();
    }

    public int maxHatDistance() {
        return ConfigHandler.COMMON.potOfGoldMaxHatDistance.get();
    }

    public void linkHat(BlockPos hatPos) throws LinkHatException {
        if (hats.size() >= maxHats()) {
            throw new LinkHatException(LinkErrorCode.TOO_MANY_HATS);
        }

        if (!worldPosition.closerThan(hatPos, maxHatDistance())) {
            throw new LinkHatException(LinkErrorCode.HAT_TOO_FAR);
        }

        if (isHatLinked(hatPos)) {
            throw new LinkHatException(LinkErrorCode.ALREADY_LINKED);
        }

        BlockEntity te = level.getBlockEntity(hatPos);
        if (te == null || !(te instanceof HatTileEntity)) {
            throw new LinkHatException(LinkErrorCode.HAT_NOT_FOUND);
        }

        HatTileEntity hatTE = (HatTileEntity)te;
        BlockPos potPos = hatTE.getLinkedPot();
        if (potPos != null) {
            if (potPos.equals(worldPosition)) {
                throw new LinkHatException(LinkErrorCode.ALREADY_LINKED);
            } else {
                throw new LinkHatException(LinkErrorCode.LINKED_TO_OTHER_POT);
            }
        }
        hats.add(new HatData(hatPos));
        hatTE.linkToPot(worldPosition);
        setChanged();
    }

    public boolean unlinkHat(BlockPos hatPos) {
        HatData hat = getHatByPos(hatPos);

        if (hat != null) {
            hats.remove(hat);
            setChanged();
            BlockEntity te = level.getBlockEntity(hatPos);
            if (te != null && te instanceof HatTileEntity) {
                HatTileEntity hatTE = (HatTileEntity) te;
                BlockPos linkedPot = hatTE.getLinkedPot();
                if (linkedPot != null && linkedPot.equals(worldPosition)) {
                    hatTE.unlink();
                }
            }
            return true;
        }
        return false;
    }

    public boolean isHatLinked(BlockPos pos) {
        HatData hat = getHatByPos(pos);
        return hat != null;
    }

    private HatData getHatByPos(BlockPos pos) {
        for (HatData hd: this.hats) {
            if (hd.hatPos.equals(pos)) {
                return hd;
            }
        }
        return null;
    }

    /**
     * @param goldArconiumPos Position of hat in world
     * @return If the hat has a gold arconium tile entity under it, return that, otherwise return null
     */
    private GoldArconiumTileEntity getGoldArconiumInWorld(BlockPos goldArconiumPos) {
        BlockEntity te = level.getBlockEntity(goldArconiumPos);

        if (te == null || !(te instanceof GoldArconiumTileEntity)) {
            return null;
        }

        return (GoldArconiumTileEntity)te;
    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            return;
        }

        // Track world game time to thwart tick accelerators
        if (lastIntervalGameTime == 0) {
            lastIntervalGameTime = level.getGameTime();
        }

        long now = level.getGameTime();
        if (now - lastIntervalGameTime < ticksPerInterval()) {
            return;
        }

        lastIntervalGameTime = now;
        intervalsElapsed++;
        tickHats();
    }

    /**
     * Collect coins from a gold arconium block under a hat. If this depletes the block, it will be turned into a regular arconium block.
     *
     * @param goldArconiumTE
     * @param goldArconiumPos
     *
     * @return Number of coins collected
     */
    private int collectCoins(GoldArconiumTileEntity goldArconiumTE, BlockPos goldArconiumPos) {
        // TODO interval for this should probably be independent of the item sending logic
        int coinCount = goldArconiumTE.collectCoins();
        if (coinCount > 0) {
            addCoins(coinCount);
            ItemStack sent = new ItemStack(ModItems.goldCoin, Math.min(coinCount, 64));
            // TODO Have a dedicated packet OR rename this one
            ModPackets.sendToNearby(level, goldArconiumPos, new PotItemTransferPacket(worldPosition.above(1).offset(0.5, 0.5, 0.5), goldArconiumPos.above().offset(0.5, 0.5, 0.5), sent));
        }

        if (goldArconiumTE.isDepleted()) {
            level.setBlock(goldArconiumPos, ModBlocks.getArconiumBlock(goldArconiumTE.getTier()).defaultBlockState(), 3);
            level.playSound(null, worldPosition, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1, 1);
        }

        return coinCount;
    }

    /**
     * Loop over all hats in random order and collect coins and/or send (generate) resources to them
     */
    private void tickHats() {
        // For now, assume 1 coin = 1 resource, regardless of other parameters
        BlockPos particlePos = worldPosition.above(2);
        Vec3 particleVec = new Vec3(particlePos.getX(), particlePos.getY(), particlePos.getZ());
        particleVec.add(0.5, 1.5, 0.5);

        if (level == null || hats.size() == 0) {
            level.addParticle(ParticleTypes.POOF, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0);
            return;
        }

        int hatsSentTo = 0;
        int hatsCollectedCoinsFrom = 0;
        List<HatData> hatsToRemove = new ArrayList<>(hats.size());

        Collections.shuffle(hats);

        for (HatData hat : hats) {
            BlockPos hatPos = hat.hatPos;
            // TODO consider chunk loading if the hat is in a chunk that is not loaded?
            HatTileEntity hatEntity = getHatEntity(hatPos);
            if (hatEntity == null) {
                // Hat's gone. Don't try this hat again.
                hatsToRemove.add(hat);
                continue;
            } else {
                BlockPos potPos = hatEntity.getLinkedPot();
                if (potPos == null || !potPos.equals(worldPosition)) {
                    Arconia.logger.warn("Hat linked to pot at " + worldPosition + " thinks it's not linked, or linked to another pot at " + potPos + ". Unlinking");
                    hatsToRemove.add(hat);
                    continue;
                }
            }

            if (!hatEntity.getResourceGenerated().isEmpty()) {
                if (this.intervalsElapsed - hat.lastResourceGenInterval < hatEntity.getResourceGenInterval()) {
                    continue;
                }
                if (coinCount > 0 && sendResourceToHat(hatEntity, hatPos)) {
                    hatsSentTo++;
                    hat.lastResourceGenInterval = this.intervalsElapsed;
                }
            } else {
                // This prevents sending more frequently than the dictated interval of the gold arconium block AND it limits to 1 coin collector, but..
                // placing multiple of a lower tier can still speed it up because the limit is applied per pot tick, which is shorter than the coin collector's
                // interval. I.e. one can always get coin collection down to 1 per pot tick by placing enough of them. Am I ok with that?
                BlockPos goldArconiumPos = hatPos.below();
                GoldArconiumTileEntity te = getGoldArconiumInWorld(goldArconiumPos);
                if (te != null && hatsCollectedCoinsFrom < MAX_COIN_SUPPLIERS && this.intervalsElapsed - hat.lastCoinCollectInterval >= te.getCoinGenerationInterval()) {
                    if (collectCoins(te, goldArconiumPos) > 0) {
                        hatsCollectedCoinsFrom++;
                        hat.lastCoinCollectInterval = this.intervalsElapsed;
                    }
                }
            }
        }

        if (hatsSentTo == 0) {
            level.addParticle(ParticleTypes.SMOKE, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0);

        }

        for (HatData hat: hatsToRemove) {
            unlinkHat(hat.hatPos);
        }
    }

    /**
     * @param hatEntity
     * @return Whether resources were sent to the specified hat
     */
    private boolean sendResourceToHat(HatTileEntity hatEntity, BlockPos hatPos) {
        if (coinCount < hatEntity.getResourceCoinCost()) {
            return false;
        }
        ItemStack sent = hatEntity.generateResource(level);
        if (!sent.isEmpty()) {
            coinCount -= hatEntity.getResourceCoinCost();
            setChanged();
            PotItemTransferPacket packet = new PotItemTransferPacket(hatPos.offset(0.5, 0.5, 0.5), worldPosition.above(1).offset(0.5, 0.5, 0.5), sent);
            ModPackets.sendToNearby(level, worldPosition, packet);
            return true;
        }
        return false;
    }

    private HatTileEntity getHatEntity(BlockPos hatPos) {
        BlockEntity te = level.getBlockEntity(hatPos);
        if (te == null || !(te instanceof HatTileEntity)) {
            return null;
        }

        return (HatTileEntity) te;
    }

    public void writePacketNBT(CompoundTag tag) {
        tag.putLongArray(TAG_HAT_POSITIONS, hats.stream().map(hat -> hat.hatPos.asLong()).collect(Collectors.toList()));
        tag.putInt(TAG_COIN_COUNT, coinCount);
    }

    public void readPacketNBT(CompoundTag tag) {
        long[] longPositions = tag.getLongArray(TAG_HAT_POSITIONS);
        hats.clear();
        for (long longPos: longPositions) {
            hats.add(new HatData(BlockPos.of(longPos)));
        }
        this.coinCount = tag.getInt(TAG_COIN_COUNT);
    }

    public enum LinkErrorCode { ALREADY_LINKED, TOO_MANY_HATS, HAT_NOT_FOUND, HAT_TOO_FAR, LINKED_TO_OTHER_POT }

    public class LinkHatException extends Exception {
        public LinkErrorCode code;

        public LinkHatException(LinkErrorCode code) {
            this.code = code;
        }
    }
}

/**
 * Tracks resourcegen/coin collection data for hats. Tracked here rather than on the individual hats so multiple pots may potentially be linked against the
 * same hat (is that smart though?)
 */
class HatData {
    public HatData(BlockPos hatPos) {
        this.hatPos = hatPos;
        this.lastResourceGenInterval = 0; //not persisted
        this.lastCoinCollectInterval = 0; //not persisted
    }
    public long lastResourceGenInterval;
    public long lastCoinCollectInterval;
    public BlockPos hatPos;
}
