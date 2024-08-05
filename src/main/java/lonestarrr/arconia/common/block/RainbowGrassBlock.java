package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

public class RainbowGrassBlock extends GrassBlock implements BlockColor {
    private RainbowColor tier;

    public RainbowGrassBlock(RainbowColor color) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS));
        this.tier = color;
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }

}
