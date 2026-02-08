package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Leaves of the resource tree
 */
public class ArconiumTreeLeaves extends LeavesBlock implements BlockColor {
    private final RainbowColor tier;

    public ArconiumTreeLeaves(RainbowColor tier) {
        super(Block.Properties.of().mapColor(MapColor.PLANT).ignitedByLava().pushReaction(PushReaction.DESTROY).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion());
        this.tier = tier;
    }

    @Nonnull
    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public int getColor(
            @NotNull BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }

    @Override
    public @NotNull MapColor getMapColor(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull MapColor defaultColor) {
        return switch (this.tier) {
            case RainbowColor.RED -> MapColor.COLOR_RED;
            case RainbowColor.ORANGE -> MapColor.COLOR_ORANGE;
            case RainbowColor.YELLOW -> MapColor.COLOR_YELLOW;
            case RainbowColor.GREEN -> MapColor.COLOR_GREEN;
            case RainbowColor.BLUE -> MapColor.COLOR_BLUE;
            case RainbowColor.LIGHT_BLUE -> MapColor.COLOR_LIGHT_BLUE;
            case RainbowColor.PURPLE -> MapColor.COLOR_PURPLE;
        };
    }
}
