package lonestarrr.arconia.common.lib.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

/** Base class for tile entities that implements the standard data syncing
 */
public abstract class BaseTileEntity extends TileEntity {
    public BaseTileEntity(TileEntityType<?> type) {
        super(type);
    }

    @Nonnull
    @Override
    public CompoundNBT save(CompoundNBT tag) {
        CompoundNBT ret = super.save(tag);
        writePacketNBT(ret);
        return ret;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        readPacketNBT(tag);
    }

    public abstract void writePacketNBT(CompoundNBT tag);

    public abstract void readPacketNBT(CompoundNBT tag);


    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tag = getUpdateTag();
        writePacketNBT(tag);
        final int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(worldPosition, tileEntityType, tag);

    }

    @Override
    public void onDataPacket(NetworkManager manager, SUpdateTileEntityPacket packet) {
        super.onDataPacket(manager, packet);
        readPacketNBT(packet.getTag());
    }

    @Override
    public final CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
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