package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.GoldArconiumTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class InfiniteGoldArconiumBlock extends Block implements BlockColor {
    private final RainbowColor tier;

    public InfiniteGoldArconiumBlock(RainbowColor tier) {
        super(Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL));
        this.tier = tier;
    }

    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new GoldArconiumTileEntity(tier, true);
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}