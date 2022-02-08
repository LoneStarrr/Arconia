package lonestarrr.arconia.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Leaves of the resource tree
 */
public class ArconiumTreeLeaves extends LeavesBlock implements BlockColor {
    private RainbowColor tier;

    public ArconiumTreeLeaves(RainbowColor tier) {
        super(Block.Properties.of(Material.LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion());
        this.tier = tier;
    }

    @Nonnull
    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}
