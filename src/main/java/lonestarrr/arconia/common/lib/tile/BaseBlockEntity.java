package lonestarrr.arconia.common.lib.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/** Base class for block entities that implement the standard data syncing
 */
public abstract class BaseBlockEntity extends BlockEntity {
    public BaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nonnull
    @Override
    public void saveAdditional(CompoundTag tag) {
        writePacketNBT(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readPacketNBT(tag);
    }

    public abstract void writePacketNBT(CompoundTag tag);

    public abstract void readPacketNBT(CompoundTag tag);

    /**
     * Updates client on block updates
     */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Will get tag from #getUpdateTag
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public final CompoundTag getUpdateTag() {
        CompoundTag result = new CompoundTag();
        saveAdditional(result);
        return result;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        // Called on client to read server data
        load(tag);
    }

    /**
     * Updates client side by publishing a block update
     */
    protected void updateClient() {
        if (level == null || level.isClientSide()) {
            return;
        }

        level.sendBlockUpdated(getBlockPos(), level.getBlockState(getBlockPos()), level.getBlockState(getBlockPos()), Block.UPDATE_CLIENTS);
    }
}