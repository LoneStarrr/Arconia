package lonestarrr.arconia.common.world;

import com.google.common.collect.ImmutableSet;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModFeatures {
    public static final Set<Biome.BiomeCategory> CLOVER_BIOME_BLACKLIST = ImmutableSet.of(
            Biome.BiomeCategory.NETHER,
            Biome.BiomeCategory.THEEND,
            Biome.BiomeCategory.DESERT,
            Biome.BiomeCategory.ICY,
            Biome.BiomeCategory.MUSHROOM,
            Biome.BiomeCategory.UNDERGROUND,
            Biome.BiomeCategory.BEACH
    );

    /// Configured features
    private static final Map<RainbowColor, ConfiguredFeature<TreeConfiguration, ?>> configuredTrees =
            new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, PlacedFeature> placedTrees = new HashMap<>(RainbowColor.values().length);
    // from minecraft's VegetionFeatures
    public static final ConfiguredFeature<RandomPatchConfiguration, ?> CLOVER_CONFIGURED = FeatureUtils.register("clover",
            Feature.FLOWER.configured(new RandomPatchConfiguration(8, 6, 2, () -> {
                return Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.clover))).onlyWhenEmpty();
            })));
    public static final PlacedFeature PLACED_CLOVER = PlacementUtils.register("placed_clover", CLOVER_CONFIGURED.placed(VegetationPlacements.worldSurfaceSquaredWithCount(2)));

    static {
        for (RainbowColor tier : RainbowColor.values()) {
            // from vanilla's TreeFeatures
            ConfiguredFeature<TreeConfiguration, ?> treeConfigured = FeatureUtils.register("arconium_tree_" + tier.getTierName(), Feature.TREE.configured(
                    (new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(Blocks.OAK_LOG), new StraightTrunkPlacer(5, 2, 0),
                            BlockStateProvider.simple(ModBlocks.getArconiumTreeLeaves(tier)), new BlobFoliagePlacer(
                            UniformInt.of(2, 3), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))).ignoreVines().build()));
            configuredTrees.put(tier, treeConfigured);

            // from vanilla VegetationPlacements
            PlacedFeature arconiumTreePlaced = PlacementUtils.register("arconium_tree_" + tier.getTierName(),
                    treeConfigured.placed(VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), ModBlocks.getArconiumTreeSapling(tier))));
            placedTrees.put(tier, arconiumTreePlaced);
        }
    }

    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        IForgeRegistry<Feature<?>> r = event.getRegistry();
        Arconia.logger.info("********* Registering biome features");

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "clovers"), CLOVER_CONFIGURED);

        // Using minecraft's built in tree feature for the arconium trees. Each tier has a unique config due to using tiered leaves
        for (RainbowColor tier : RainbowColor.values()) {
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "arconium_tree_" + tier.getTierName()),
                    configuredTrees.get(tier));
        }
    }

    public static ConfiguredFeature<TreeConfiguration, ?> getArconiumTreeConfigured(RainbowColor tier) {
        return configuredTrees.get(tier);
    }

    /**
     * Mod's main entrypoint for adding world generation
     */
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        addClovers(event);
        addArconiumTrees(event);
    }

    public static void addArconiumTrees(BiomeLoadingEvent event) {
        Biome.BiomeCategory category = event.getCategory();

        if (category == Biome.BiomeCategory.FOREST) {
            for (RainbowColor tier : RainbowColor.values()) {
                event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                        placedTrees.get(tier));
            }
        }
    }

    public static void addClovers(BiomeLoadingEvent event) {
        Biome.BiomeCategory category = event.getCategory();

        if (!CLOVER_BIOME_BLACKLIST.contains(category)) {
            Arconia.logger.info("********* Adding clovers to biome " + event.getName());
            event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PLACED_CLOVER);
        }
    }
}
