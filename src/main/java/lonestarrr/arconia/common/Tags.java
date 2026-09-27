package lonestarrr.arconia.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class Tags {
  public static class Blocks {
    /** Blocks that clovers may generate on during worldgen */
    public static final TagKey<Block> CLOVER_PATCH_CAN_PLACE_ON = tag("clover_patch_can_place_on");

    private static TagKey<Block> tag(String name) {
      return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Arconia.MOD_ID, name));
    }
  }

  public static class Biomes {
    public static final TagKey<Biome> HAS_CLOVER = tag("has_clover");
    public static final TagKey<Biome> HAS_ARCONIUM_TREES = tag("has_arconium_trees");

    private static TagKey<Biome> tag(String name) {
      return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(Arconia.MOD_ID, name));
    }
  }
}
