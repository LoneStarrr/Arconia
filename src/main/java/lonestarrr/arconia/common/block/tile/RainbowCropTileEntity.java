package lonestarrr.arconia.common.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * This tile entity keeps an inventory of gold coins, as well as ticks to convert the gold coins into rainbow coins.
 * Inspiration: https://wiki.mcjty.eu/modding/index.php?title=TileEntity_Data-1.12
 */
public class RainbowCropTileEntity extends TileEntity {
    //@ObjectHolder(Arconia.MOD_ID + ":" + BlockNames.RAINBOW_CROP)
    public static TileEntityType<RainbowCropTileEntity> TYPE;

    private short coinCount = 0;

    public RainbowCropTileEntity() {
        super(TYPE);
    }

    public void addCoins(short coinCount) {
        this.coinCount = (short) Math.min(this.coinCount + coinCount, Short.MAX_VALUE);
    }

    public void removeCoins(short coinCount) {
        this.coinCount = (short) Math.max(this.coinCount - coinCount, 0);
    }

    public short getCoinCount() {
        return this.coinCount;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.coinCount = compound.getShort("coinCount");
    }

}
