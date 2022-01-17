package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.GoldArconiumTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class InfiniteGoldArconiumBlock extends Block implements IBlockColor {
    private final RainbowColor tier;

    public InfiniteGoldArconiumBlock(RainbowColor tier) {
        super(Properties.create(Material.IRON).setRequiresTool().hardnessAndResistance(5.0f, 6.0f).sound(SoundType.METAL));
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
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new GoldArconiumTileEntity(tier, true);
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}