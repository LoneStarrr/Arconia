package lonestarrr.arconia.common.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.trees.Tree;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.trees.MoneyTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.regex.Pattern;

/**
 * Sapling that sprouts a money tree
 */
public class ResourceTreeSapling extends SaplingBlock implements IBlockColor {
    private final RainbowColor tier;

    public ResourceTreeSapling(@Nonnull RainbowColor tier) {
        super(new MoneyTree(tier),
                AbstractBlock.Properties.create(Material.PLANTS).doesNotBlockMovement().tickRandomly().hardnessAndResistance(0F).sound(SoundType.PLANT));
        this.tier = tier;
    }

    @Override
    protected boolean isValidGround(BlockState state, IBlockReader world, BlockPos pos) {
        /*
        !!!!!!!!!!!!!!!!!
        Trees have their OWN method to check for valid ground to see if they can grow. This is currently defined in TreeFeature.isDirtOrFarmlandAt(),
        and there's a forge add-on that checks for the FORGE dirt tag. So, I added all the tree root blocks to the forge dirt tag so that one will work
        as well. Kind of dirt...y.
         */
        Block block = state.getBlock();
        // Trees can spawn in the overworld - they don't do anything special if not placed on our custom tree root block - and the player will need the
        // saplings and other resources
        if (super.isValidGround(state, world, pos)) {
            return true;
        }

        if (!(block instanceof ResourceTreeRootBlock)) {
            return false;
        }

        ResourceTreeRootBlock rootBlock = (ResourceTreeRootBlock) block;
        return rootBlock.getTier().ordinal() >= this.tier.ordinal();
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return RainbowColor.getColorRGB(tier);
    }
}
