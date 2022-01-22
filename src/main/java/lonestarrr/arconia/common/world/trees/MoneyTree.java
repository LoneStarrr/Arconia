package lonestarrr.arconia.common.world.trees;

import net.minecraft.block.BlockState;
import net.minecraft.block.trees.Tree;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.ModFeatures;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * "Money does not grow on trees!" - some ignorant fool
 */
public class MoneyTree extends Tree {
    private final RainbowColor tier;

    public MoneyTree(RainbowColor tier) {
        this.tier = tier;
    }

    /**
     * Get a {@link net.minecraft.world.gen.feature.ConfiguredFeature} of tree
     */
    @Nullable
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random randomIn, boolean largeHive) {
        return ModFeatures.getResourceTreeConfigured(this.tier);
    }

    @Override
    public boolean growTree(
            ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random rand) {
        boolean didGrow = super.growTree(world, chunkGenerator, pos, state, rand);

        if (didGrow) {
            // Is this money tree placed on a pattern? Then attempt to complete it.
            attemptPatternCompletion(world, pos, this.tier);
        }

        return didGrow;
    }

    private static void attemptPatternCompletion(World world, BlockPos saplingPos, RainbowColor tier) {
        // TODO Do I still need this? I think I care not - in which case this whole custom class can probably go
    }
}
