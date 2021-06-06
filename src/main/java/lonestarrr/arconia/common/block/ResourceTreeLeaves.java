package lonestarrr.arconia.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import lonestarrr.arconia.common.core.RainbowColor;

import javax.annotation.Nonnull;

/**
 * Leaves of the resource tree
 */
public class ResourceTreeLeaves extends LeavesBlock {
    private RainbowColor tier;

    public ResourceTreeLeaves(RainbowColor tier) {
        super(Block.Properties.create(Material.LEAVES).hardnessAndResistance(0.2F).tickRandomly().sound(SoundType.PLANT).notSolid());
        this.tier = tier;
    }

    @Nonnull
    public RainbowColor getTier() {
        return tier;
    }
}
