package lonestarrr.arconia.common.world;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ModFeatures {
    public static final ResourceKey<PlacedFeature> CLOVER_PATCH = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "clover_patch"));
    public static final ResourceKey<ConfiguredFeature<?,?>> CLOVER_PATCH_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "clover_patch"));
    /// Configured features
    private static final Map<RainbowColor, ResourceKey<ConfiguredFeature<?, ?>>> configuredTrees =
            new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, ResourceKey<PlacedFeature>> placedTrees = new HashMap<>(RainbowColor.values().length);

    static {
        // Actual configuration happens in data generation as dynamic features are no longer supported sinced 1.19.3
        for (RainbowColor tier : RainbowColor.values()) {
            configuredTrees.put(tier, ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "tree_" + tier.getTierName())));
            placedTrees.put(tier, ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Arconia.MOD_ID, "tree_" + tier.getTierName())));
        }
    }

    public static @NotNull ResourceKey<PlacedFeature> getPlacedTrees(RainbowColor tier) {
        return placedTrees.get(tier);
    }

    public static @NotNull ResourceKey<ConfiguredFeature<?, ?>> getArconiumTreeConfigured(RainbowColor tier) {
        return configuredTrees.get(tier);
    }
}
