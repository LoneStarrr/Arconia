package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class ArconiumBlock extends Block {
  private final RainbowColor tier;

  public ArconiumBlock(Block.Properties props, RainbowColor tier) {
    super(
        props
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(5.0f, 6.0f)
            .sound(SoundType.METAL));
    this.tier = tier;
    // Harvest level & tool are set by adding the block to specific tags - see datagen
  }
}
