package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.block.GoldArconiumBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.PotItemTransferPacket;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.stream.Collectors;

public class PotMultiBlockPrimaryTileEntity extends BaseTileEntity implements ITickableTileEntity {
    private static final int MAX_HATS = 50;
    private static final float MAX_HAT_DISTANCE = 16; // Max straight-line distance
    private static final float MAX_GOLD_ARCONIUM_DISTANCE = 16; // Max straight-line distance
    private static final String TAG_HAT_POSITIONS = "hat_positions";
    private static final String TAG_COIN_COUNT = "coin_count";
    private static final String TAG_GOLD_ARCONIUM_POS = "gold_arconium_pos";

    private int coinCount;
    public static final int TICK_INTERVAL = 5;
    private int ticksElapsed;

    private final List<BlockPos> hatPositions = new ArrayList<>();;
    private BlockPos goldArconiumPos;

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

    public boolean linkGoldArconiumBlock(BlockPos goldArconiumPos) {
        if (world.isRemote) {
            return false;
        }

        if (goldArconiumPos.distanceSq(pos) > MAX_GOLD_ARCONIUM_DISTANCE * MAX_GOLD_ARCONIUM_DISTANCE) {
            return false;
        }
        RainbowColor tier = getGoldArconiumTierInWorld(goldArconiumPos);
        if (tier == null) {
            return false;
        }
        this.goldArconiumPos = goldArconiumPos;
        markDirty();
        return true;
    }

    private RainbowColor getGoldArconiumTierInWorld(BlockPos worldPos) {
        BlockState bs = world.getBlockState(worldPos);
        if (!(bs.getBlock() instanceof GoldArconiumBlock)) {
            return null;
        }
        GoldArconiumBlock block = (GoldArconiumBlock)bs.getBlock();
        return block.getTier();
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

        collectCoins();
        generateResource();
    }

    private void unlinkHat(int hatIndex) {
        hatPositions.remove(hatIndex);
        markDirty();
    }

    private void unlinkGoldArconiumBlock() {
        this.goldArconiumPos = null;
        markDirty();
    }

    private void collectCoins() {
        // TODO interval for this should probably be independent of the item sending logic
        if (goldArconiumPos == null) {
            return;
        }

        RainbowColor tier = getGoldArconiumTierInWorld(goldArconiumPos);
        if (tier == null) {
            unlinkGoldArconiumBlock();
            return;
        }

        int coinCount = (int)Math.pow(2, tier.getTier());
        addCoins(coinCount);
        // TODO add logic to 'deplete' the gold Arconium source, and to transform it into a transconium block
        ItemStack sent = new ItemStack(ModItems.goldCoin, Math.min(coinCount, 64));
        // TODO Have a dedicated packet OR rename this one
        ModPackets.sendToNearby(world, goldArconiumPos, new PotItemTransferPacket(pos.up(2), goldArconiumPos, sent));
    }

    private void generateResource() {
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

        if (coinCount > 0) {
            Collections.shuffle(hatPositions);
            int hatsToSendTo = 4;

            for (BlockPos hatPos : hatPositions) {
                if (coinCount <= 0) {
                    break;
                }

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
                        coinCount--;
                        markDirty();
                        PotItemTransferPacket packet = new PotItemTransferPacket(hatPos, pos.up(2), sent);
                        ModPackets.sendToNearby(world, pos, packet);
                        hatsSentTo++;
                    }
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
        // TODO store coin generator
        if (goldArconiumPos != null) {
            tag.putLong(TAG_GOLD_ARCONIUM_POS, goldArconiumPos.toLong());
        }
        tag.putInt(TAG_COIN_COUNT, coinCount);
    }

    public void readPacketNBT(CompoundNBT tag) {
        long[] longPositions = tag.getLongArray(TAG_HAT_POSITIONS);
        hatPositions.clear();
        for (long longPos: longPositions) {
            hatPositions.add(BlockPos.fromLong(longPos));
        }
        this.coinCount = tag.getInt(TAG_COIN_COUNT);
        this.goldArconiumPos = tag.contains(TAG_GOLD_ARCONIUM_POS) ? BlockPos.fromLong(tag.getLong(TAG_GOLD_ARCONIUM_POS)) : null;
    }

}
