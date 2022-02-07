package lonestarrr.arconia.common.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.trees.ArconiumTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Sapling that sprouts an arconium tree
 */
public class ArconiumTreeSapling extends SaplingBlock implements IBlockColor {
    private final RainbowColor tier;

    public ArconiumTreeSapling(@Nonnull RainbowColor tier) {
        super(new ArconiumTree(tier),
                AbstractBlock.Properties.of(Material.PLANT).noCollission().randomTicks().strength(0F).sound(SoundType.GRASS));
        this.tier = tier;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, IBlockReader world, BlockPos pos) {
        /*
        !!!!!!!!!!!!!!!!!
        Trees have their OWN method to check for valid ground to see if they can grow. This is currently defined in TreeFeature.isDirtOrFarmlandAt(),
        and there's a forge add-on that checks for the FORGE dirt tag. So, I added all the tree root blocks to the forge dirt tag so that one will work
        as well. Kind of dirt...y.
         */
        Block block = state.getBlock();
        // Trees can spawn in the overworld - they don't do anything special if not placed on our custom tree root block - and the player will need the
        // saplings and other resources
        if (super.mayPlaceOn(state, world, pos)) {
            return true;
        }

        if (!(block instanceof ArconiumTreeRootBlock)) {
            return false;
        }

        ArconiumTreeRootBlock rootBlock = (ArconiumTreeRootBlock) block;
        return rootBlock.getTier().ordinal() >= this.tier.ordinal();
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}
