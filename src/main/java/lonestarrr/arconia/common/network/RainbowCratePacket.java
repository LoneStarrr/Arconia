package lonestarrr.arconia.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent;
import lonestarrr.arconia.common.block.tile.RainbowCrateTileEntity;

import java.util.function.Supplier;

/**
 * Synchronize a rainbow crate's internal inventory to clients so they can see data on it in the chest GUI
 */
public class RainbowCratePacket {
    private final BlockPos pos;
    private final CompoundTag inventory;

    public RainbowCratePacket(final BlockPos pos, final CompoundTag inventory) {
        // The client needs to be able to see a rainbow crate's internal inventory. The regular chest inventory
        // syncing appears to be dealt with by vanilla code already. So, just item counts? Which should be all
        // that the client cares about.
        this.pos = pos;
        this.inventory = inventory;
    }

    public static RainbowCratePacket decode(FriendlyByteBuf buf) {
        return new RainbowCratePacket(buf.readBlockPos(), buf.readNbt());
    }

    public static void encode(RainbowCratePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeNbt(msg.inventory);
    }

    public static class Handler {
        public static void handle(final RainbowCratePacket message, final Supplier<NetworkEvent.Context> ctx) {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                ctx.get().setPacketHandled(true);
                return;
            }

            ctx.get().enqueueWork(new Runnable() {
                // Use anon - lambda causes classloading issues
                @Override
                public void run() {
                    Minecraft mc = Minecraft.getInstance();
                    Level world = mc.level;
                    BlockEntity te = world.getBlockEntity(message.pos);
                    if (te instanceof RainbowCrateTileEntity) {
                        RainbowCrateTileEntity rcte = (RainbowCrateTileEntity)te;
                        //rcte.receiveServerSideInventoryData(message.itemCounts);
                        rcte.load(world.getBlockState(message.pos), message.inventory);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
