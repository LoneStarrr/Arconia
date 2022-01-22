package lonestarrr.arconia.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * A clover weed that grows in the overworld
 */
public class CloverBlock extends BushBlock {
    public CloverBlock() {
        super(Block.Properties.of(Material.PLANT).noCollission().strength(0f).sound(SoundType.GRASS));
    }
}
