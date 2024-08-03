package lonestarrr.arconia.data.world;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.ModFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Register features and biome modifiers
 */
public class BiomeModifiers extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();

    static {
        // Configured features
        BUILDER.add(Registries.CONFIGURED_FEATURE, context -> {
            for (RainbowColor tier : RainbowColor.values()) {
                context.register(ModFeatures.getArconiumTreeConfigured(tier),
                        new ConfiguredFeature<>(
                                Feature.TREE,
                                (new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(Blocks.OAK_LOG), new StraightTrunkPlacer(5, 2, 0),
                                        BlockStateProvider.simple(ModBlocks.getArconiumTreeLeaves(tier).get()), new BlobFoliagePlacer(
                                        UniformInt.of(2, 3), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))).ignoreVines().build()));
            }

            context.register(
                    ModFeatures.CLOVER_PATCH_CONFIGURED,
                    new ConfiguredFeature<>(
                            Feature.RANDOM_PATCH,
                            new RandomPatchConfiguration(3, 6, 3,
                                    PlacementUtils.onlyWhenEmpty(
                                            Feature.SIMPLE_BLOCK,
                                            new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.clover.get()))))));

        });

        // Placed features
        BUILDER.add(Registries.PLACED_FEATURE, context -> {
            for (RainbowColor tier : RainbowColor.values()) {
                context.register(
                        ModFeatures.getPlacedTrees(tier),
                        new PlacedFeature(
                                context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModFeatures.getArconiumTreeConfigured(tier)),
                                VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.05F, 1), ModBlocks.getArconiumTreeSapling(tier).get())
                        ));
            }

            context.register(
                    ModFeatures.CLOVER_PATCH,
                    new PlacedFeature(
                            context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModFeatures.CLOVER_PATCH_CONFIGURED),
                            VegetationPlacements.worldSurfaceSquaredWithCount(1)));
        });
    }

    public BiomeModifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Arconia.MOD_ID));

    }

    @Override
    public @NotNull String getName() {
        return "Biome Modifier Registries: " + Arconia.MOD_ID;
    }
}
