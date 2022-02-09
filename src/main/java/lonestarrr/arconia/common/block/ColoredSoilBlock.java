package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.level.block.Block;

// TODO split out RainbowColor property logic into base block class like BlockColored
public class ColoredSoilBlock extends Block {
    public final RainbowColor color;


    public ColoredSoilBlock(Block.Properties builder, RainbowColor color) {
        super(builder);
        this.color = color;
    }
}
