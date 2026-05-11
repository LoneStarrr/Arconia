package lonestarrr.arconia.client.item;

import com.mojang.serialization.MapCodec;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.MagicInABottle;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Per-stack tint source for the magic-in-a-bottle overlay layer — picks the colour from the
 * bottle's stored tier (the {@code MagicInABottleData} component) so the swirl matches whichever
 * rainbow tier the bottle is currently set to.
 */
public record MagicInABottleTintSource() implements ItemTintSource {
  public static final Identifier ID =
      Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "magic_in_a_bottle_tier");
  public static final MapCodec<MagicInABottleTintSource> MAP_CODEC =
      MapCodec.unit(MagicInABottleTintSource::new);

  @Override
  public int calculate(
      ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
    RainbowColor tier = MagicInABottle.getTier(stack);
    // Tints multiply against the texture; -1 (white) = no change, ARGB ints = colour multiply.
    return tier.getColorValue() | 0xFF000000;
  }

  @Override
  public MapCodec<? extends ItemTintSource> type() {
    return MAP_CODEC;
  }
}
