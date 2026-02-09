package lonestarrr.arconia.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class Tags {
    public static class Biomes {
        public static final TagKey<Biome> HAS_CLOVER = tag("has_clover");
        public static final TagKey<Biome> HAS_ARCONIUM_TREES = tag("has_arconium_trees");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Arconia.MOD_ID, name));
        }

    }
}
