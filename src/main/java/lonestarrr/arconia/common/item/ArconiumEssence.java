package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.item.Item;

public class ArconiumEssence extends Item {
  private RainbowColor tier;

  public ArconiumEssence(Item.Properties builder, RainbowColor tier) {
    super(builder);
    this.tier = tier;
  }
}
