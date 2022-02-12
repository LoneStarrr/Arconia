package lonestarrr.arconia.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class PotBlock extends Block {
    public PotBlock() {
        super(Block.Properties.of(Material.METAL, MaterialColor.STONE).strength(2.0F).noOcclusion());
    }
}
