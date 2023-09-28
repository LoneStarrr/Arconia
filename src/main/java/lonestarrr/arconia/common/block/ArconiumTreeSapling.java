package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.trees.ArconiumTree;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Sapling that sprouts an arconium tree
 */
public class ArconiumTreeSapling extends SaplingBlock implements BlockColor {
    private final RainbowColor tier;

    public ArconiumTreeSapling(@Nonnull RainbowColor tier) {
        super(new ArconiumTree(tier),
                BlockBehaviour.Properties.of(Material.PLANT).noCollission().randomTicks().strength(0F).sound(SoundType.GRASS));
        this.tier = tier;
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}
