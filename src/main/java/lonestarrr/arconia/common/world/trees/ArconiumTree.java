package lonestarrr.arconia.common.world.trees;

import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

import javax.annotation.Nullable;
import java.util.Random;

public class ArconiumTree extends AbstractTreeGrower {
    private final RainbowColor tier;

    public ArconiumTree(RainbowColor tier) {
        this.tier = tier;
    }

    /**
     * Get a {@link net.minecraft.world.gen.feature.ConfiguredFeature} of tree
     */
    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random randomIn, boolean largeHive) {
        return ModFeatures.getArconiumTreeConfigured(this.tier);
    }

    @Override
    public boolean growTree(
            ServerLevel world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random rand) {
        boolean didGrow = super.growTree(world, chunkGenerator, pos, state, rand);

        if (didGrow) {
            // Is this arconium tree placed on a pattern? Then attempt to complete it.
            attemptPatternCompletion(world, pos, this.tier);
        }

        return didGrow;
    }

    private static void attemptPatternCompletion(Level world, BlockPos saplingPos, RainbowColor tier) {
        // TODO Do I still need this? I think I care not - in which case this whole custom class can probably go
    }
}
