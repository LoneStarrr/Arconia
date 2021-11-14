package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.client.particle.Particle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PotMultiBlockPrimaryTileEntity extends BaseTileEntity implements ITickableTileEntity {
    private static final int MAX_HATS = 50;
    private static final float MAX_HAT_DISTANCE = 16; // Max straight-line distance
    private static final String TAG_HAT_POSITIONS = "hat_positions";

    private int coinCount;
    public static final int TICK_INTERVAL = 5;
    private int ticksElapsed;

    private final List<BlockPos> hatPositions;

    public PotMultiBlockPrimaryTileEntity() {
        super(ModTiles.POT_MULTIBLOCK_PRIMARY);
        // TODO check for valid structure at an interval, if not, destroy ourselves
        hatPositions = new ArrayList<>();
        coinCount = 0;
        ticksElapsed = 0;
    }

    public void addCoins(int count) {
        coinCount += count;
    }

    public boolean linkHat(BlockPos hatPos) {
        if (hatPositions.size() >= MAX_HATS) {
            return false;
        }

        if (hatPositions.contains(hatPos)) {
            return false;
        }

        TileEntity te = world.getTileEntity(hatPos);
        if (te == null || !(te instanceof HatTileEntity)) {
            return false;
        }

        if (!pos.withinDistance(hatPos, MAX_HAT_DISTANCE)) {
            return false;
        }

        hatPositions.add(hatPos);
        markDirty();
        return true;
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }

        if (++ticksElapsed < TICK_INTERVAL) {
            return;
        }
        ticksElapsed = 0;

        generateResource();
    }

    private void unlinkHat(int hatIndex) {
        hatPositions.remove(hatIndex);
        markDirty();
    }

    private void generateResource() {
        // For now, assume 1 coin = 1 resource, regardless of other parameters
        BlockPos particlePos = pos.up(2);
        Vector3d particleVec = new Vector3d(particlePos.getX(), particlePos.getY(), particlePos.getZ());
        particleVec.add(0.5, 0.5, 0.5);

        if (world == null || hatPositions.size() == 0) {
            world.addParticle(ParticleTypes.POOF, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0);
            return;
        }

        Collections.shuffle(hatPositions);
        int hatsToSendTo = 4;
        int hatsSentTo = 0;
        List<BlockPos> hatsToRemove = new ArrayList<>(hatPositions.size());

        for (BlockPos hatPos: hatPositions) {
            if (hatsSentTo >= hatsToSendTo) {
                break;
            }

            // TODO consider chunk loading if the hat is in a chunk that is not loaded?
            HatTileEntity hatEntity = getHatEntity(hatPos);
            if (hatEntity == null) {
                // Hat's gone. Don't try this hat again.
                hatsToRemove.add(hatPos);
            } else {
                ItemStack sent = hatEntity.generateResource(world);
                if (!sent.isEmpty()) {
                    //            coinCount--; TODO enable me once coin collection works
                    markDirty();
                    PotItemTransferPacket packet = new PotItemTransferPacket(hatPos, pos, sent);
                    ModPackets.sendToNearby(world, pos, packet);
                    hatsSentTo++;
                }
            }
        }

        if (hatsSentTo == 0) {
            world.addParticle(ParticleTypes.SMOKE, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0);

        }

        for (BlockPos pos: hatsToRemove) {
            hatPositions.remove(pos);
        }
        if (hatsToRemove.size() > 0) {
            markDirty();
        }
    }

    private HatTileEntity getHatEntity(BlockPos hatPos) {
        TileEntity te = world.getTileEntity(hatPos);
        if (te == null || !(te instanceof HatTileEntity)) {
            return null;
        }

        return (HatTileEntity) te;
    }

    public void writePacketNBT(CompoundNBT tag) {
        tag.putLongArray(TAG_HAT_POSITIONS, hatPositions.stream().map(pos -> pos.toLong()).collect(Collectors.toList()));
    }

    public void readPacketNBT(CompoundNBT tag) {
        long[] longPositions = tag.getLongArray(TAG_HAT_POSITIONS);
        hatPositions.clear();
        for (long longPos: longPositions) {
            hatPositions.add(BlockPos.fromLong(longPos));
        }
    }

}
