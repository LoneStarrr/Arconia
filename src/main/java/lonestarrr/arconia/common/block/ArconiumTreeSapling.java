package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import lonestarrr.arconia.common.world.trees.ArconiumTree;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeTagHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

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

    @Override
    public void advanceTree(ServerLevel world, BlockPos pos, BlockState state, Random rand) {
        super.advanceTree(world, pos, state, rand);
        // Sadly, advanceTree does not return a boolean if a tree has actually sprouted. So, we'll just check if `pos' still contains a sapling.
        if (!(world.getBlockState(pos).getBlock() instanceof ArconiumTreeSapling)) {
            onTreeFormed(world, pos, rand);
        }
    }

    private static void onTreeFormed(ServerLevel world, BlockPos saplingPos, Random rand) {
        // Spawn some clovers at random around the sapling's position if the mod is configured to be in a skyblock setting
        if (!ConfigHandler.COMMON.skyBlock.get()) {
            return;
        }

        final int radius = 2;
        final int diameter = 2 * radius + 1;
        final float cloversPerTree = 2f;
        final float chancePerPos = cloversPerTree / (diameter * diameter - 1);
        BlockPos c1 = saplingPos.north(radius).west(radius).below();
        BlockPos c2 = saplingPos.south(radius).east(radius).below();
        BlockPos.betweenClosedStream(new AABB(c1, c2)).forEach(pos -> {
                if (!pos.equals(saplingPos)) {
                    if (BlockTags.DIRT.contains(world.getBlockState(pos).getBlock())) {
                        // This will help spread grass easily!
                        world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (chancePerPos >= rand.nextFloat()
                            && ModBlocks.clover.canSustainPlant(world.getBlockState(pos), world, pos, Direction.UP, ModBlocks.clover)
                            && world.getBlockState(pos.above()).isAir()
                    ) {
                        world.setBlock(pos.above(), ModBlocks.clover.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
        });
    }
}
