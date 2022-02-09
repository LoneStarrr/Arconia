package lonestarrr.arconia.common.world;

import com.google.common.collect.ImmutableSet;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Features;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.blockplacers.SimpleBlockPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
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
            Biome.BiomeCategory.ICY,
            Biome.BiomeCategory.MUSHROOM
    );

    public static final class Configs {
        public static final RandomPatchConfiguration CLOVER_CONFIG = (new RandomPatchConfiguration.GrassConfigurationBuilder(
                new SimpleStateProvider(ModBlocks.clover.defaultBlockState()), SimpleBlockPlacer.INSTANCE)).tries(8).build();
    }

    /// Configured features
    private static final Map<RainbowColor, ConfiguredFeature<TreeConfiguration, ?>> configuredTrees =
            new HashMap<>(RainbowColor.values().length);
    public static final ConfiguredFeature<?, ?> CLOVER_CONFIGURED = Feature.FLOWER.configured(Configs.CLOVER_CONFIG).decorated(Features.Decorators.ADD_32).decorated(Features.Decorators.HEIGHTMAP_SQUARE).count(4);


    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        IForgeRegistry<Feature<?>> r = event.getRegistry();
        Arconia.logger.info("********* Registering biome features");

        // Features (before configuration) are registered separately from Configured Features, and differently as well! Somehow.
        // Don't have features atm, but if I add some, register them through the register() method.

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "clovers"), CLOVER_CONFIGURED);

        // Using minecraft's built in tree feature for the arconium trees. Each tier has a unique config due to using tiered leaves
        for (RainbowColor tier: RainbowColor.values()) {
            TreeConfiguration treeConfig = (new TreeConfiguration.TreeConfigurationBuilder(
                    new SimpleStateProvider(Blocks.OAK_LOG.defaultBlockState()),
                    new SimpleStateProvider(ModBlocks.getArconiumTreeLeaves(tier).defaultBlockState()),
                    new BlobFoliagePlacer(UniformInt.fixed(2), UniformInt.fixed(0), 3),
                    new StraightTrunkPlacer(5, 2, 0), new TwoLayersFeatureSize(1, 0, 1))).ignoreVines().build();
            ConfiguredFeature<TreeConfiguration, ?> treeConfigured = Feature.TREE.configured(treeConfig);
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "arconium_tree_" + tier.getTierName()), treeConfigured);
            configuredTrees.put(tier, treeConfigured);
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
            for (RainbowColor tier: RainbowColor.values()) {
                event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                        getArconiumTreeConfigured(tier));
            }
        }
    }

    public static void addClovers(BiomeLoadingEvent event) {
        Biome.BiomeCategory category = event.getCategory();

        if (!CLOVER_BIOME_BLACKLIST.contains(category)) {
            Arconia.logger.info("********* Adding clovers to biome " + event.getName());
            event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CLOVER_CONFIGURED);
        }
    }
}
