package lonestarrr.arconia.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

/**
 * A clover weed that grows in the overworld
 */
public class CloverBlock extends BushBlock {
    public CloverBlock() {
        super(Block.Properties.of(Material.PLANT).noCollission().strength(0f).sound(SoundType.GRASS));
    }
}
