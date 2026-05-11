package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class RainbowGrassBlock extends GrassBlock {
  private RainbowColor tier;

  public RainbowGrassBlock(BlockBehaviour.Properties props, RainbowColor color) {
    super(props.mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS));
    this.tier = color;
  }

  public RainbowColor getTier() {
    return tier;
  }
}
