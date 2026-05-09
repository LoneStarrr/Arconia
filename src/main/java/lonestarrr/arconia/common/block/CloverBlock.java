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
    public static final MapCodec<CloverBlock> CODEC = simpleCodec(CloverBlock::new);

    public CloverBlock(Block.Properties props) {
        super(props.mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).noCollission().strength(0f).sound(SoundType.GRASS));
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<BushBlock> codec() {
        return (MapCodec<BushBlock>) (MapCodec<?>) CODEC;
    }
}
