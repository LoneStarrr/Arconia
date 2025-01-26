package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PotMultiBlockPrimaryBlockEntity extends BaseBlockEntity {
    private static final String TAG_HAT_POSITIONS = "hat_positions";
    public static final int MIN_TICK_INTERVAL = 5;

    private long lastIntervalGameTime = 0;
    private final List<HatData> hats = new ArrayList<>();

    public PotMultiBlockPrimaryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_MULTIBLOCK_PRIMARY.get(), pos, state);
        // TODO check for valid structure at an interval, if not, destroy ourselves
    }

    public int maxHats() {
        return ConfigHandler.COMMON.potOfGoldMaxHats.get();
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
        if (level == null) {
            return false;
        }
        HatData hat = getHatByPos(hatPos);

        if (hat != null) {
            hats.remove(hat);
            setChanged();
            BlockEntity be = level.getBlockEntity(hatPos);
            if (be instanceof HatBlockEntity hatBE) {
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

    public static void tick(Level level, BlockPos pos, BlockState state, PotMultiBlockPrimaryBlockEntity blockEntity) {
        blockEntity.tickInternal(level, pos, state);
    }

    public void tickInternal(Level level, BlockPos pos, BlockState state) {
        // Track world game time to thwart tick accelerators
        if (lastIntervalGameTime == 0) {
            lastIntervalGameTime = level.getGameTime();
        }

        long now = level.getGameTime();
        if (now - lastIntervalGameTime < MIN_TICK_INTERVAL) {
            return;
        }

        RainbowColor tier = detectTier(); //TODO cache?
        if (tier == null) {
            return;
        }

        int tickInterval = ConfigHandler.COMMON.potGenerationInterval.get(tier).get();

        if (now - lastIntervalGameTime < tickInterval) {
            return;
        }

        lastIntervalGameTime = now;
        tickHats();
    }

    /*
     * Tier of the pot is determined by the highest tier of a linked hat
     */
    private @Nonnull RainbowColor detectTier() {
        // RED tier is the minimum tier
        RainbowColor detectedTier = RainbowColor.RED;

        for (HatData hat : hats) {
            BlockPos hatPos = hat.hatPos;
            // TODO consider chunk loading if the hat is in a chunk that is not loaded?
            HatBlockEntity hatEntity = getHatEntity(hatPos);
            if (hatEntity == null) {
                continue;
            }

            if (hatEntity.getTier().getTier() > detectedTier.getTier()) {
                detectedTier = hatEntity.getTier();
            }
        }

        return detectedTier;
    }

    /**
     * Loop over all hats in random order and send (generate) resources to them
     */
    private void tickHats() {
        if (level == null) {
            return;
        }

        BlockPos particlePos = worldPosition.above(2);
        Vec3 particleVec = new Vec3(particlePos.getX(), particlePos.getY(), particlePos.getZ());
        particleVec.add(0.5, 1.5, 0.5);

        if (hats.isEmpty()) {
            level.addParticle(ParticleTypes.POOF, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0);
            return;
        }

        int hatsSentTo = 0;
        List<HatData> hatsToRemove = new ArrayList<>(hats.size());
        RainbowColor potTier = detectTier();

        int maxHatsToSendto = ConfigHandler.COMMON.potGenerationCount.get(potTier).get().intValue();

        Collections.shuffle(hats);

        for (HatData hat : hats) {
            if (hatsSentTo >= maxHatsToSendto) {
                break;
            }

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
                    Arconia.logger.warn("Hat linked to pot at {} thinks it's not linked, or linked to another pot at {}. Unlinking", worldPosition, potPos);
                    hatsToRemove.add(hat);
                    continue;
                }
            }

            if (hatEntity.getResourceGenerated().isEmpty()) {
                continue;
            }

            if (sendResourceToHat(hatEntity, hatPos)) {
                hatsSentTo++;
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
        if (level == null) {
            return false;
        }

        ItemStack sent = hatEntity.generateResource(level);
        if (!sent.isEmpty()) {
            setChanged();
            PotItemTransferPacket packet = new PotItemTransferPacket(hatPos.above(), worldPosition.above(), sent);
            ModPackets.sendToNearby(level, worldPosition, packet);
            return true;
        }
        return false;
    }

    private HatBlockEntity getHatEntity(BlockPos hatPos) {
        if (level == null) {
            return null;
        }

        BlockEntity te = level.getBlockEntity(hatPos);
        if (!(te instanceof HatBlockEntity)) {
            return null;
        }

        return (HatBlockEntity) te;
    }

    public void writePacketNBT(CompoundTag tag) {
        tag.putLongArray(TAG_HAT_POSITIONS, hats.stream().map(hat -> hat.hatPos.asLong()).collect(Collectors.toList()));
    }

    public void readPacketNBT(CompoundTag tag) {
        long[] longPositions = tag.getLongArray(TAG_HAT_POSITIONS);
        hats.clear();
        for (long longPos: longPositions) {
            hats.add(new HatData(BlockPos.of(longPos)));
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

/**
 * Tracks resourcegen data for hats. Tracked here rather than on the individual hats so multiple pots may potentially be linked against the
 * same hat (is that smart though?)
 */
class HatData {
    public HatData(BlockPos hatPos) {
        this.hatPos = hatPos;
    }
    public BlockPos hatPos;
}
