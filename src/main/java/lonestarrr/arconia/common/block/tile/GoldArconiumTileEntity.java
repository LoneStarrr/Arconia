package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Gold Arconium blocks are used as a gold source for the pot of gold multiblock. This entity tracks the available gold before it is depleted, and manages
 * gold coin collection interval / counts.
 */
public class GoldArconiumTileEntity extends TileEntity {
    private RainbowColor tier;

    public GoldArconiumTileEntity(RainbowColor tier) {
        super(ModTiles.getGoldArconiumTileEntityType(tier));
        this.tier = tier;
    }

    public final RainbowColor getTier() {
        return this.tier;
    }

    /**
     * @return Whether the internal gold store has been depleted
     */
    public boolean isDepleted() {
        // TODO implement me
        return false;
    }

    /**
     * @return How many coins' worth the gold store has left to collect
     */
    public int coinsLeft() {
        // TODO implement me - consider enchants (unbreaking, mending)
        return 1;
    }

    /**
     * @return The number of coins collected. This depletes an internal 'gold store', and if the returned value is 0, it means either the store is exhausted,
     * or the minimum time interval allowed between coin collections has not been reached
     */
    public int collectCoins() {
        // TODO implement me from an actual store
        return (int)Math.pow(2, tier.getTier());
    }
}
