package lonestarrr.arconia.common.lib.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

/** Base class for tile entities that implements the standard data syncing
 */
public abstract class BaseTileEntity extends BlockEntity {
    public BaseTileEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Nonnull
    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag ret = super.save(tag);
        writePacketNBT(ret);
        return ret;
    }

    @Override
    public void load(BlockState state, CompoundTag tag) {
        super.load(state, tag);
        readPacketNBT(tag);
    }

    public abstract void writePacketNBT(CompoundTag tag);

    public abstract void readPacketNBT(CompoundTag tag);


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag tag = getUpdateTag();
        writePacketNBT(tag);
        final int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new ClientboundBlockEntityDataPacket(worldPosition, tileEntityType, tag);

    }

    @Override
    public void onDataPacket(Connection manager, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(manager, packet);
        readPacketNBT(packet.getTag());
    }

    @Override
    public final CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        // Called on client to read server data
        load(state, tag);
    }

    /**
     * Updates client side by publishing a block update
     */
    protected void updateClient() {
        if (level == null || level.isClientSide()) {
            return;
        }

        level.sendBlockUpdated(getBlockPos(), level.getBlockState(getBlockPos()), level.getBlockState(getBlockPos()), Constants.BlockFlags.BLOCK_UPDATE);
    }
}