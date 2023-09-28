package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;

public class ArconiumBlock extends Block implements BlockColor {
    private final RainbowColor tier;

    public ArconiumBlock(RainbowColor tier) {
        super(Block.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL));
        this.tier = tier;
        // Harvest level & tool are set by adding the block to specific tags - see datagen
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }

}
