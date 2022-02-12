package lonestarrr.arconia.common.block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This tile entity keeps an inventory of gold coins, as well as ticks to convert the gold coins into rainbow coins.
 * Inspiration: https://wiki.mcjty.eu/modding/index.php?title=TileEntity_Data-1.12
 */
public class RainbowCropBlockEntity extends BlockEntity {
    //@ObjectHolder(Arconia.MOD_ID + ":" + BlockNames.RAINBOW_CROP)
    public static BlockEntityType<RainbowCropBlockEntity> TYPE;

    private short coinCount = 0;

    public RainbowCropBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
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
    public void load(CompoundTag tag) {
        super.load(tag);
        this.coinCount = tag.getShort("coinCount");
    }

}
