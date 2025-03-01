package lonestarrr.arconia.data.world;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.ModFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Register features and biome modifiers
 */
public class BiomeModifiers extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();
    public static final ResourceKey<BiomeModifier> ADD_TREES = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
            new ResourceLocation(Arconia.MOD_ID, "add_trees") // The registry name
    );
    public static final ResourceKey<BiomeModifier> ADD_CLOVERS = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS, // The registry this key is for
            new ResourceLocation(Arconia.MOD_ID, "add_clovers") // The registry name
    );

    static {
        // Configured features - these define how a feature is shaped
        BUILDER.add(Registries.CONFIGURED_FEATURE, bootstrap -> {
            for (RainbowColor tier : RainbowColor.values()) {
                bootstrap.register(ModFeatures.getArconiumTreeConfigured(tier),
                        new ConfiguredFeature<>(
                                Feature.TREE,
                                (new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(Blocks.OAK_LOG), new StraightTrunkPlacer(5, 2, 0),
                                        BlockStateProvider.simple(ModBlocks.getArconiumTreeLeaves(tier).get()), new BlobFoliagePlacer(
                                        UniformInt.of(2, 3), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))).ignoreVines().build()));
            }

            bootstrap.register(
                    ModFeatures.CLOVER_PATCH_CONFIGURED,
                    new ConfiguredFeature<>(
                            Feature.RANDOM_PATCH,
                            new RandomPatchConfiguration(3, 6, 3,
                                    PlacementUtils.onlyWhenEmpty(
                                            Feature.SIMPLE_BLOCK,
                                            new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.clover.get()))))));

        });

        // Placed features - these define constraints around placing them
        BUILDER.add(Registries.PLACED_FEATURE, bootstrap -> {
            for (RainbowColor tier : RainbowColor.values()) {
                bootstrap.register(
                        ModFeatures.getPlacedTrees(tier),
                        new PlacedFeature(
                                bootstrap.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModFeatures.getArconiumTreeConfigured(tier)),
                                VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.05F, 1), ModBlocks.getArconiumTreeSapling(tier).get())
                        ));
            }

            bootstrap.register(
                    ModFeatures.CLOVER_PATCH,
                    new PlacedFeature(
                            bootstrap.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModFeatures.CLOVER_PATCH_CONFIGURED),
                            VegetationPlacements.worldSurfaceSquaredWithCount(1)));
        });

        // Biome modifiers - these define where the placed features are to be placed
        BUILDER.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
            HolderGetter<Biome> biomes = bootstrap.lookup(Registries.BIOME);
            HolderGetter<PlacedFeature> placedFeatures = bootstrap.lookup(Registries.PLACED_FEATURE);
            HolderSet.Named<Biome> forestBiomes = biomes.getOrThrow(BiomeTags.IS_FOREST);

            // TODO: Should do this tag based, e.g. have an arconia:biome_clovers tag.
            // YES, because this either is a biome list, or a tag. And to be modder friendly, one needs to use a tag.
            // This is also why everything has 1000 tags nowadays, because if 10 mods want to allow something based on
            // a tag, an item/biome/.... will have 10 mod-specific tags. Unless there's a nice generic 'neoforge' tag
            // covering such a case of course.

            // Red arconium trees grow in forest biomes
            bootstrap.register(ADD_TREES,
                    new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                            forestBiomes,
                            HolderSet.direct(placedFeatures.getOrThrow(ModFeatures.getPlacedTrees(RainbowColor.RED))),
                            GenerationStep.Decoration.VEGETAL_DECORATION
                    )
            );

            // Clovers grow in many overworld biomes
            bootstrap.register(ADD_CLOVERS,
                    new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                            forestBiomes,
                            HolderSet.direct(placedFeatures.getOrThrow(ModFeatures.CLOVER_PATCH)),
                            GenerationStep.Decoration.VEGETAL_DECORATION
                    )
            );
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
