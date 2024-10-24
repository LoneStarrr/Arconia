package lonestarrr.arconia.common.world;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

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
        // Actual configuration happens in data generation as dynamic features are no longer supported since 1.19.3
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
