package lonestarrr.arconia.common.world;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ModFeatures {
    public static final String CLOVER_PATCH_NAME = "clover_patch";

    public static final DeferredRegister<ConfiguredFeature<?,?>> CONFIGURED_FEATURES =
            DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, Arconia.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, Arconia.MOD_ID);

    /// Configured features
    private static final Map<RainbowColor, RegistryObject<ConfiguredFeature<?, ?>>> configuredTrees =
            new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, RegistryObject<PlacedFeature>> placedTrees = new HashMap<>(RainbowColor.values().length);
    // from minecraft's VegetationFeatures
    public static final RegistryObject<ConfiguredFeature<?,?>> CONFIGURED_CLOVER_PATCH = CONFIGURED_FEATURES.register(CLOVER_PATCH_NAME,
            () -> new ConfiguredFeature<>(Feature.RANDOM_PATCH, new RandomPatchConfiguration(3, 6, 3,
                    PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.clover.get()))))));

    public static final RegistryObject<PlacedFeature> PLACED_CLOVER_PATCH = PLACED_FEATURES.register(CLOVER_PATCH_NAME,
        () -> new PlacedFeature(CONFIGURED_CLOVER_PATCH.getHolder().get(), VegetationPlacements.worldSurfaceSquaredWithCount(1)));

    static {
        for (RainbowColor tier : RainbowColor.values()) {
            // from vanilla's TreeFeatures
            RegistryObject<ConfiguredFeature<?, ?>> treeConfigured = CONFIGURED_FEATURES.register("tree_" + tier.getTierName(),
                    () -> new ConfiguredFeature<>(Feature.TREE, (new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(Blocks.OAK_LOG), new StraightTrunkPlacer(5, 2, 0),
                            BlockStateProvider.simple(ModBlocks.getArconiumTreeLeaves(tier).get()), new BlobFoliagePlacer(
                            UniformInt.of(2, 3), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))).ignoreVines().build()));
            configuredTrees.put(tier, treeConfigured);

            // from vanilla VegetationPlacements
            RegistryObject<PlacedFeature> arconiumTreePlaced = PLACED_FEATURES.register("tree_" + tier.getTierName(),
                    () -> new PlacedFeature(treeConfigured.getHolder().get(), VegetationPlacements.treePlacement(PlacementUtils.countExtra(0, 0.05F, 1), ModBlocks.getArconiumTreeSapling(tier).get())));
            placedTrees.put(tier, arconiumTreePlaced);
        }
    }

    public static void register(IEventBus modBus) {
        CONFIGURED_FEATURES.register(modBus);
        PLACED_FEATURES.register(modBus);
    }

//    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
//        // This is not even using the event's registry. There is no separate forge registry for configured/placed features so we're piggybacking on this one
////        IForgeRegistry<Feature<?>> r = event.getRegistry();
//        Arconia.logger.info("********* Registering biome features");
//
//        // Registered configured features and their placements
//        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "clovers"), CLOVER_CONFIGURED);
//        Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "clovers"), PLACED_CLOVER);
//
//        // Using minecraft's built in tree feature for the arconium trees. Each tier has a unique config due to using tiered leaves
//        for (RainbowColor tier : RainbowColor.values()) {
//            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "arconium_tree_" + tier.getTierName()),
//                    configuredTrees.get(tier));
//            Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "arconium_tree_" + tier.getTierName()),
//                    placedTrees.get(tier));
//        }
//    }

    public static Holder<ConfiguredFeature<?, ?>> getArconiumTreeConfigured(RainbowColor tier) {
        return configuredTrees.get(tier).getHolder().get();
    }
}
