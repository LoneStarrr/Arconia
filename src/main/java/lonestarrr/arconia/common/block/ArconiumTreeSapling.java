package lonestarrr.arconia.common.block;

import net.minecraft.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.trees.ArconiumTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

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
    protected boolean mayPlaceOn(BlockState state, BlockGetter world, BlockPos pos) {
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
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }
}
