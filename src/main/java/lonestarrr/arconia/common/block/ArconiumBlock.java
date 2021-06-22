package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class ArconiumBlock extends Block {
    private final RainbowColor color;

    public ArconiumBlock(RainbowColor color) {
        super(Block.Properties.create(Material.IRON).setRequiresTool().hardnessAndResistance(5.0f, 6.0f).sound(SoundType.METAL));
        this.color = color;
    }
}
