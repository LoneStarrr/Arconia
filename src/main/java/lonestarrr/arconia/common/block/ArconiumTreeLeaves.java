package lonestarrr.arconia.common.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nonnull;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;

/** Leaves of the resource tree */
public class ArconiumTreeLeaves extends LeavesBlock {
  private final RainbowColor tier;

  public ArconiumTreeLeaves(Block.Properties props, RainbowColor tier) {
    super(
        0.01F,
        props
            .mapColor(RainbowColor.getMapColor(tier))
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion());
    this.tier = tier;
  }

  @Nonnull
  public RainbowColor getTier() {
    return tier;
  }

  @Override
  public MapCodec<? extends LeavesBlock> codec() {
    throw new UnsupportedOperationException(
        "ArconiumTreeLeaves does not support codec serialization");
  }

  @Override
  protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
    // No-op — Arconium leaves don't drop falling-leaf particles.
  }
}
