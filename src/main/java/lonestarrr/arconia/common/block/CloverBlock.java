package lonestarrr.arconia.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * A clover weed that grows in the overworld
 */
public class CloverBlock extends BushBlock {
    public CloverBlock() {
        super(Block.Properties.of().mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).noCollission().strength(0f).sound(SoundType.GRASS));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return null;
    }
}
