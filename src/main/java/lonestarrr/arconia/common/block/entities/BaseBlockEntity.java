package lonestarrr.arconia.common.block.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

/** Base class for block entities that implement the standard data syncing */
public abstract class BaseBlockEntity extends BlockEntity {
  public BaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Override
  protected void saveAdditional(@NotNull ValueOutput output) {
    super.saveAdditional(output);
    writePacketNBT(output);
  }

  @Override
  protected void loadAdditional(@NotNull ValueInput input) {
    super.loadAdditional(input);
    readPacketNBT(input);
  }

  public abstract void writePacketNBT(@NotNull ValueOutput output);

  public abstract void readPacketNBT(@NotNull ValueInput input);

  /** Updates client on block updates */
  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    // Will get tag from #getUpdateTag
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
    return this.saveWithoutMetadata(registries);
  }

  /** Updates client side by publishing a block update */
  protected void updateClient() {
    if (level == null || level.isClientSide()) {
      return;
    }

    level.sendBlockUpdated(
        getBlockPos(),
        level.getBlockState(getBlockPos()),
        level.getBlockState(getBlockPos()),
        Block.UPDATE_CLIENTS);
  }
}
