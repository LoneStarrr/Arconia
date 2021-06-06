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
    public CompoundNBT write(CompoundNBT tag) {
        CompoundNBT ret = super.write(tag);
        writePacketNBT(ret);
        return ret;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        readPacketNBT(tag);
    }

    public abstract void writePacketNBT(CompoundNBT tag);

    public abstract void readPacketNBT(CompoundNBT tag);


    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tag = getUpdateTag();
        writePacketNBT(tag);
        final int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(pos, tileEntityType, tag);

    }

    @Override
    public void onDataPacket(NetworkManager manager, SUpdateTileEntityPacket packet) {
        super.onDataPacket(manager, packet);
        readPacketNBT(packet.getNbtCompound());
    }

    @Override
    public final CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        // Called on client to read server data
        read(state, tag);
    }

    /**
     * Updates client side by publishing a block update
     */
    protected void updateClient() {
        if (world == null || world.isRemote()) {
            return;
        }

        world.notifyBlockUpdate(getPos(), world.getBlockState(getPos()), world.getBlockState(getPos()), Constants.BlockFlags.BLOCK_UPDATE);
    }
}