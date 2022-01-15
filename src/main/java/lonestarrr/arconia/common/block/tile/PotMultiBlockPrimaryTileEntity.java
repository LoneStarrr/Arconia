package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.item.ModItems;
import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.stream.Collectors;

public class PotMultiBlockPrimaryTileEntity extends BaseTileEntity implements ITickableTileEntity {
    private static final int MAX_HATS = 50;
    private static final float MAX_HAT_DISTANCE = 16; // Max straight-line distance
    private static final String TAG_HAT_POSITIONS = "hat_positions";
    private static final String TAG_COIN_COUNT = "coin_count";

    private int coinCount;
    public static final int MIN_TICK_INTERVAL = 5;
    private int ticksElapsed;

    private final List<BlockPos> hatPositions = new ArrayList<>();;

    public PotMultiBlockPrimaryTileEntity() {
        super(ModTiles.POT_MULTIBLOCK_PRIMARY);
        // TODO check for valid structure at an interval, if not, destroy ourselves
        coinCount = 0;
        ticksElapsed = 0;
    }

    public int addCoins(int count) {
        coinCount += count;
        markDirty();
        return count;
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

    /**
     * @param goldArconiumPos Position of hat in world
     * @return If the hat has a gold arconium tile entity under it, return that, otherwise return null
     */
    private GoldArconiumTileEntity getGoldArconiumInWorld(BlockPos goldArconiumPos) {
        TileEntity te = world.getTileEntity(goldArconiumPos);

        if (te == null || !(te instanceof GoldArconiumTileEntity)) {
            return null;
        }

        return (GoldArconiumTileEntity)te;
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }

        if (++ticksElapsed < MIN_TICK_INTERVAL) {
            return;
        }
        ticksElapsed = 0;
        tickHats();
    }

    /**
     * Collect coins from a gold arconium block under a hat. If this depletes the block, it will be turned into a regular arconium block.
     *
     * @param goldArconiumTE
     * @param goldArconiumPos
     */
    private void collectCoins(GoldArconiumTileEntity goldArconiumTE, BlockPos goldArconiumPos) {
        // TODO interval for this should probably be independent of the item sending logic
        int coinCount = goldArconiumTE.collectCoins();
        if (coinCount > 0) {
            addCoins(coinCount);
            ItemStack sent = new ItemStack(ModItems.goldCoin, Math.min(coinCount, 64));
            // TODO Have a dedicated packet OR rename this one
            ModPackets.sendToNearby(world, goldArconiumPos, new PotItemTransferPacket(pos.up(1).add(0.5, 0.5, 0.5), goldArconiumPos.up().add(0.5, 0.5, 0.5), sent));
        }

        if (goldArconiumTE.isDepleted()) {
            world.setBlockState(goldArconiumPos, ModBlocks.getArconiumBlock(goldArconiumTE.getTier()).getDefaultState(), 3);
            world.playSound(null, pos, SoundEvents.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 1, 1);
        }
    }

    /**
     * Loop over all hats in random order and collect coins and/or send (generate) resources to them
     */
    private void tickHats() {
        // For now, assume 1 coin = 1 resource, regardless of other parameters
        BlockPos particlePos = pos.up(2);
        Vector3d particleVec = new Vector3d(particlePos.getX(), particlePos.getY(), particlePos.getZ());
        particleVec.add(0.5, 1.5, 0.5);

        if (world == null || hatPositions.size() == 0) {
            world.addParticle(ParticleTypes.POOF, particleVec.x, particleVec.y, particleVec.z, 0, 0, 0);
            return;
        }

        int hatsSentTo = 0;
        List<BlockPos> hatsToRemove = new ArrayList<>(hatPositions.size());

        Collections.shuffle(hatPositions);

        for (BlockPos hatPos : hatPositions) {
            // TODO consider chunk loading if the hat is in a chunk that is not loaded?
            HatTileEntity hatEntity = getHatEntity(hatPos);
            if (hatEntity == null) {
                // Hat's gone. Don't try this hat again.
                hatsToRemove.add(hatPos);
                continue;
            }

            if (hatEntity.hasResourceGenerator()) {
                if (coinCount > 0 && sendResourceToHat(hatEntity, hatPos)) {
                    hatsSentTo++;
                }
            } else {
                BlockPos goldArconiumPos = hatPos.down();
                GoldArconiumTileEntity te = getGoldArconiumInWorld(goldArconiumPos);
                if (te != null) {
                    collectCoins(te, goldArconiumPos);
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

    /**
     * @param hatEntity
     * @return Whether resources were sent to the specified hat
     */
    private boolean sendResourceToHat(HatTileEntity hatEntity, BlockPos hatPos) {
        ItemStack sent = hatEntity.generateResource(world);
        if (!sent.isEmpty()) {
            coinCount--;
            markDirty();
            PotItemTransferPacket packet = new PotItemTransferPacket(hatPos.add(0.5, 0.5, 0.5), pos.up(1).add(0.5, 0.5, 0.5), sent);
            ModPackets.sendToNearby(world, pos, packet);
            return true;
        }
        return false;
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
        tag.putInt(TAG_COIN_COUNT, coinCount);
    }

    public void readPacketNBT(CompoundNBT tag) {
        long[] longPositions = tag.getLongArray(TAG_HAT_POSITIONS);
        hatPositions.clear();
        for (long longPos: longPositions) {
            hatPositions.add(BlockPos.fromLong(longPos));
        }
        this.coinCount = tag.getInt(TAG_COIN_COUNT);
    }

}
