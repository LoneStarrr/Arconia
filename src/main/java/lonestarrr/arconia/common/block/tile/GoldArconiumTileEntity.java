package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Gold Arconium blocks are used as a gold source for the pot of gold multiblock. This entity tracks the available gold before it is depleted, and manages
 * gold coin collection interval / counts.
 */
public class GoldArconiumTileEntity extends BaseTileEntity {
    public static final String TAG_COINS = "coins"; //nbt tag
    public static final String TAG_INFINITE = "infinite"; //nbt tag

    private RainbowColor tier;
    private long coins; // number of coins left to produce
    private boolean infinite; // If set, internal coin store is never depleted and unlimited coins are generated

    public GoldArconiumTileEntity(RainbowColor tier) {
        super(ModTiles.getGoldArconiumTileEntityType(tier));
        this.tier = tier;
        setInitialCoinCount();
        infinite = false;
    }

    public final RainbowColor getTier() {
        return this.tier;
    }

    public void setInfinite() {
        this.infinite = true;
    }

    private void setInitialCoinCount() {
        // TODO configurable
        this.coins = (long)Math.pow(2, 7 + this.tier.getTier());
        markDirty();
    }

    /**
     * @return Whether the internal gold store has been depleted
     */
    public boolean isDepleted() {
        return !this.infinite && this.coins == 0;
    }

    /**
     * @return How many coins' worth the gold store has left to collect
     */
    public long coinsLeft() {
        return coins;
    }

    /**
     * @return How frequently coins can be collected - interval length determined by collector (pot of gold)
     */
    public int getCoinGenerationInterval() {
        int tierNum = tier.getTier();
        return Math.max(1, 10 - tierNum);
    }

    /**
     * @return The number of coins collected. This depletes an internal 'gold store', and if the returned value is 0, it means either the store is exhausted,
     * or the minimum time interval allowed between coin collections has not been reached
     */
    public int collectCoins() {
        long coinsToSend =  (long)Math.pow(2, tier.getTier());
        if (!infinite) {
            coinsToSend = coinsToSend > coins ? coins : coinsToSend;
            this.coins -= coinsToSend;
            markDirty();
        }
        return (int)coinsToSend;
    }

    @Override
    public void writePacketNBT(CompoundNBT tag) {
        tag.putLong(TAG_COINS, this.coins);
        tag.putBoolean(TAG_INFINITE, this.infinite);
    }

    @Override
    public void readPacketNBT(CompoundNBT tag) {
        this.coins = tag.getLong(TAG_COINS);
        this.infinite = tag.getBoolean(TAG_INFINITE);
    }
}