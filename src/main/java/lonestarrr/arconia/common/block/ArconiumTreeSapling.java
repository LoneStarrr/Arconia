package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.world.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Sapling that sprouts an arconium tree
 */
public class ArconiumTreeSapling extends SaplingBlock {
    private final RainbowColor tier;
    private static final Map<RainbowColor, TreeGrower> trees = new HashMap<>(RainbowColor.values().length);

    static {
        for (RainbowColor tier: RainbowColor.values()) {
            trees.put(tier, new TreeGrower(
                    tier.getTierName() + "_arconium_tree",
                    Optional.empty(),
                    Optional.of(ModFeatures.getArconiumTreeConfigured(tier)),
                    Optional.empty()
            ));
        }
    }

    public ArconiumTreeSapling(@Nonnull RainbowColor tier) {
        super(trees.get(tier),
                BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).ignitedByLava().pushReaction(PushReaction.DESTROY).noCollission().randomTicks().strength(0F).sound(SoundType.GRASS));
        this.tier = tier;
    }
}
