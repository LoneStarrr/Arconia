package lonestarrr.arconia.common.block;

import net.minecraft.block.Block;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.BlockPatternException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO split out RainbowColor property logic into base block class like BlockColored
public class ColoredSoilBlock extends Block {
    public final RainbowColor color;


    public ColoredSoilBlock(Block.Properties builder, RainbowColor color) {
        super(builder);
        this.color = color;
    }
}
