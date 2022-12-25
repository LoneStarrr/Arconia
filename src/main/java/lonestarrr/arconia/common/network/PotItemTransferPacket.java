package lonestarrr.arconia.common.network;

import lonestarrr.arconia.client.effects.PotItemTransfers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet containing data for Orb lasers to visualize on the client side.
 */
public class PotItemTransferPacket {
    private final BlockPos startPos;
    private final BlockPos endPos;
    private final ItemStack itemStack;

    public PotItemTransferPacket(BlockPos startPos, BlockPos endPos, ItemStack itemStack) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.itemStack = itemStack.copy();
    }

    public static PotItemTransferPacket decode(FriendlyByteBuf buf) {
        return new PotItemTransferPacket(buf.readBlockPos(), buf.readBlockPos(), buf.readItem());
    }

    public static void encode(PotItemTransferPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.startPos);
        buf.writeBlockPos(msg.endPos);
        buf.writeItem(msg.itemStack);
    }

    public static class Handler {
        public static void handle(final PotItemTransferPacket msg, final Supplier<NetworkEvent.Context> ctx) {
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
                    PotItemTransfers.addItemTransfer(msg.startPos, msg.endPos, msg.itemStack);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
