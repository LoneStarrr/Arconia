package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;

import javax.annotation.Nonnull;

/**
 * Leaves of the resource tree
 */
public class ArconiumTreeLeaves extends LeavesBlock {
    private final RainbowColor tier;

    public ArconiumTreeLeaves(RainbowColor tier) {
        super(Block.Properties.of().mapColor(RainbowColor.getMapColor(tier)).ignitedByLava().pushReaction(PushReaction.DESTROY).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion());
        this.tier = tier;
    }

    @Nonnull
    public RainbowColor getTier() {
        return tier;
    }
}
