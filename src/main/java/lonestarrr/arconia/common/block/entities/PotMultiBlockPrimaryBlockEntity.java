package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import lonestarrr.arconia.common.item.ModItems;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PotMultiBlockPrimaryBlockEntity extends BaseBlockEntity {
    private static final int MAX_COIN_SUPPLIERS = 1; // How many hats may supply coins per coin collection tick?
    private static final String TAG_HAT_POSITIONS = "hat_positions";
    private static final String TAG_COIN_COUNT = "coin_count";

    private int coinCount;
    private int intervalsElapsed = 0;
    private int lastCoinCollectInterval = 0; // not persisted
    private long lastIntervalGameTime = 0;

    private final List<HatData> hats = new ArrayList<>();

    public PotMultiBlockPrimaryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_MULTIBLOCK_PRIMARY, pos, state);
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

    public RainbowColor getTier() { return detectTier(); }

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

        BlockEntity be = level.getBlockEntity(hatPos);
        if (be == null || !(be instanceof HatBlockEntity)) {
            throw new LinkHatException(LinkErrorCode.HAT_NOT_FOUND);
        }

        HatBlockEntity hatBE = (HatBlockEntity)be;
        BlockPos potPos = hatBE.getLinkedPot();
        if (potPos != null) {
            if (potPos.equals(worldPosition)) {
                throw new LinkHatException(LinkErrorCode.ALREADY_LINKED);
            } else {
                throw new LinkHatException(LinkErrorCode.LINKED_TO_OTHER_POT);
            }
        }
        hats.add(new HatData(hatPos));
        hatBE.linkToPot(worldPosition);
        setChanged();
    }

    public boolean unlinkHat(BlockPos hatPos) {
        HatData hat = getHatByPos(hatPos);

        if (hat != null) {
            hats.remove(hat);
            setChanged();
            BlockEntity be = level.getBlockEntity(hatPos);
            if (be != null && be instanceof HatBlockEntity) {
                HatBlockEntity hatBE = (HatBlockEntity) be;
                BlockPos linkedPot = hatBE.getLinkedPot();
                if (linkedPot != null && linkedPot.equals(worldPosition)) {
                    hatBE.unlink();
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
     * @return If the hat has a gold arconium block entity under it, return that, otherwise return null
     */
    private GoldArconiumBlockEntity getGoldArconiumInWorld(BlockPos goldArconiumPos) {
        BlockEntity be = level.getBlockEntity(goldArconiumPos);

        if (be == null || !(be instanceof GoldArconiumBlockEntity)) {
            return null;
        }

        return (GoldArconiumBlockEntity)be;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PotMultiBlockPrimaryBlockEntity blockEntity) {
        blockEntity.tickInternal(level, pos, state);
    }

    public void tickInternal(Level level, BlockPos pos, BlockState state) {
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
    private int collectCoins(GoldArconiumBlockEntity goldArconiumTE, BlockPos goldArconiumPos) {
        // TODO interval for this should probably be independent of the item sending logic
        int coinCount = goldArconiumTE.collectCoins();
        if (coinCount > 0) {
            addCoins(coinCount);
            ItemStack sent = new ItemStack(ModItems.goldCoin, Math.min(coinCount, 64));
            ModPackets.sendToNearby(level, goldArconiumPos, new PotItemTransferPacket(worldPosition.above(2), goldArconiumPos.above(), sent));
        }

        return coinCount;
    }

    /*
     * Tier of the pot is determined by a linked hat sitting on top of a gold arconium block. The tier of the gold arconium block determines the tier of the pot.
     * If multiple gold arconium blocks are linked this way, it will pick the highest tier. Linking multiple blocks does not do anything for the production rate.
     */
    private RainbowColor detectTier() {
        // Without a gold arconium block, the minimum tier is always red.
        RainbowColor detectedTier = RainbowColor.RED;
        for (HatData hat : hats) {
            BlockPos hatPos = hat.hatPos;
            // TODO consider chunk loading if the hat is in a chunk that is not loaded?
            HatBlockEntity hatEntity = getHatEntity(hatPos);
            if (hatEntity == null) {
                continue;
            }
            if (!hatEntity.getResourceGenerated().isEmpty()) {
                continue;
            }
            BlockPos goldArconiumPos = hatPos.below();
            GoldArconiumBlockEntity te = getGoldArconiumInWorld(goldArconiumPos);
            if (te != null && te.getTier().getTier() > detectedTier.getTier()) {
                detectedTier = te.getTier();
            }
        }

        return detectedTier;
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
        List<HatData> hatsToRemove = new ArrayList<>(hats.size());
        RainbowColor potTier = detectTier();

        Collections.shuffle(hats);

        for (HatData hat : hats) {
            BlockPos hatPos = hat.hatPos;
            // TODO consider chunk loading if the hat is in a chunk that is not loaded?
            HatBlockEntity hatEntity = getHatEntity(hatPos);
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
                if (coinCount > 0 && hatEntity.getTier().getTier() <= potTier.getTier() && sendResourceToHat(hatEntity, hatPos)) {
                    hatsSentTo++;
                }
            } else {
                // If the hat sits on top of a coin producer, and the coin generation interval has passed, let's get some gold.
                // If there are more coin producers, the first one that meets the criteria and has coins available will be used.
                BlockPos goldArconiumPos = hatPos.below();
                GoldArconiumBlockEntity te = getGoldArconiumInWorld(goldArconiumPos);
                if (te != null && this.intervalsElapsed - this.lastCoinCollectInterval >= te.getCoinGenerationInterval()) {
                    if (collectCoins(te, goldArconiumPos) > 0) {
                        this.lastCoinCollectInterval = this.intervalsElapsed;
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
    private boolean sendResourceToHat(HatBlockEntity hatEntity, BlockPos hatPos) {
        RainbowColor tier = hatEntity.getTier();
        int coinCost = ConfigHandler.COMMON.itemCost.get(tier).get();

        if (coinCount < coinCost) {
            return false;
        }
        ItemStack sent = hatEntity.generateResource(level);
        if (!sent.isEmpty()) {
            coinCount -= coinCost;
            setChanged();
            PotItemTransferPacket packet = new PotItemTransferPacket(hatPos.above(), worldPosition.above(), sent);
            ModPackets.sendToNearby(level, worldPosition, packet);
            return true;
        }
        return false;
    }

    private HatBlockEntity getHatEntity(BlockPos hatPos) {
        BlockEntity te = level.getBlockEntity(hatPos);
        if (te == null || !(te instanceof HatBlockEntity)) {
            return null;
        }

        return (HatBlockEntity) te;
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
    }
    public BlockPos hatPos;
}
