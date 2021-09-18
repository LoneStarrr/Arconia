package lonestarrr.arconia.common.world;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliageplacer.BlobFoliagePlacer;
import net.minecraft.world.gen.trunkplacer.StraightTrunkPlacer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static lonestarrr.arconia.common.block.ModBlocks.register;

@Mod.EventBusSubscriber(modid = Arconia.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModFeatures {
    public static final String MONEY_FEATURE_POSTFIX = "_money_tree_feature";
    public static final Set<Biome.Category> CLOVER_BIOME_BLACKLIST = ImmutableSet.of(
            Biome.Category.NETHER,
            Biome.Category.THEEND,
            Biome.Category.ICY,
            Biome.Category.MUSHROOM
    );

    public static final class Configs {
        public static final BlockClusterFeatureConfig CLOVER_CONFIG = (new BlockClusterFeatureConfig.Builder(
                new SimpleBlockStateProvider(ModBlocks.clover.getDefaultState()), SimpleBlockPlacer.PLACER)).tries(8).build();
    }

    /// Configured features
    private static final Map<RainbowColor, ConfiguredFeature<BaseTreeFeatureConfig, ?>> configuredTrees =
            new HashMap<>(RainbowColor.values().length);
    public static final ConfiguredFeature<?, ?> CLOVER_CONFIGURED = Feature.FLOWER.withConfiguration(Configs.CLOVER_CONFIG).withPlacement(Features.Placements.VEGETATION_PLACEMENT).withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT).func_242731_b(4);


    @SubscribeEvent
    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        IForgeRegistry<Feature<?>> r = event.getRegistry();
        Arconia.logger.info("********* Registering biome features");

        // Features (before configuration) are registered separately from Configured Features, and differently as well! Somehow.
        // Don't have features atm, but if I add some, register them through the register() method.

        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "clovers"), CLOVER_CONFIGURED);

        // Using minecraft's built in tree feature for the money trees. Each tier has a unique config due to using tiered leaves
        for (RainbowColor tier: RainbowColor.values()) {
            BaseTreeFeatureConfig treeConfig = (new BaseTreeFeatureConfig.Builder(
                    new SimpleBlockStateProvider(Blocks.OAK_LOG.getDefaultState()),
                    new SimpleBlockStateProvider(ModBlocks.getMoneyTreeLeaves(tier).getDefaultState()),
                    new BlobFoliagePlacer(FeatureSpread.func_242252_a(2), FeatureSpread.func_242252_a(0), 3),
                    new StraightTrunkPlacer(5, 2, 0), new TwoLayerFeature(1, 0, 1))).setIgnoreVines().build();
            ConfiguredFeature<BaseTreeFeatureConfig, ?> treeConfigured = Feature.TREE.withConfiguration(treeConfig);
            Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "resource_tree_" + tier.getTierName()), treeConfigured);
            configuredTrees.put(tier, treeConfigured);
        }
    }

    public static ConfiguredFeature<BaseTreeFeatureConfig, ?> getResourceTreeConfigured(RainbowColor tier) {
        return configuredTrees.get(tier);
    }

    /**
     * Mod's main entrypoint for adding world generation
     */
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        addClovers(event);
        addResourceTrees(event);
    }

    public static void addResourceTrees(BiomeLoadingEvent event) {
        Biome.Category category = event.getCategory();

        if (category == Biome.Category.FOREST) {
            for (RainbowColor tier: RainbowColor.values()) {
                event.getGeneration().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
                        getResourceTreeConfigured(tier));
            }
        }
    }

    public static void addClovers(BiomeLoadingEvent event) {
        Biome.Category category = event.getCategory();

        if (!CLOVER_BIOME_BLACKLIST.contains(category)) {
            Arconia.logger.info("********* Adding clovers to biome " + event.getName());
            event.getGeneration().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, CLOVER_CONFIGURED);
        }
    }
}
