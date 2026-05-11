package lonestarrr.arconia.client.item;

import com.mojang.serialization.MapCodec;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.item.MagicInABottle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes {@link MagicInABottle#getFilledPercentage} (a 0..1 ratio of ticks elapsed vs. ticks
 * needed for the next loot roll) to the data-driven item-model system, so the bottle's ClientItem
 * JSON can pick a sub-model per filled threshold.
 */
public record MagicInABottleFilledProperty() implements RangeSelectItemModelProperty {
  public static final Identifier ID = Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "filled");
  public static final MapCodec<MagicInABottleFilledProperty> MAP_CODEC =
      MapCodec.unit(MagicInABottleFilledProperty::new);

  @Override
  public float get(
      ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
    return MagicInABottle.getFilledPercentage(stack, level, owner, seed);
  }

  @Override
  public MapCodec<? extends RangeSelectItemModelProperty> type() {
    return MAP_CODEC;
  }
}
