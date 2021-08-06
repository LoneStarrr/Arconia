package lonestarrr.arconia.common.block;

import lonestarrr.arconia.client.core.handler.ColorHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Leaves of the resource tree
 */
public class ResourceTreeLeaves extends LeavesBlock implements IBlockColor {
    private RainbowColor tier;

    public ResourceTreeLeaves(RainbowColor tier) {
        super(Block.Properties.create(Material.LEAVES).hardnessAndResistance(0.2F).tickRandomly().sound(SoundType.PLANT).notSolid());
        this.tier = tier;
    }

    @Nonnull
    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}
