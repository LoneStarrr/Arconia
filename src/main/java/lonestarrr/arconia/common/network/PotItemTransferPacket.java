package lonestarrr.arconia.common.network;

import lonestarrr.arconia.client.effects.OrbLasers;
import lonestarrr.arconia.client.effects.PotItemTransfers;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet containing data for Orb lasers to visualize on the client side.
 */
public class PotItemTransferPacket {
    private final BlockPos hatPos;
    private final BlockPos potPos;
    private final ItemStack itemStack;

    public PotItemTransferPacket(BlockPos hatPos, BlockPos potPos, ItemStack itemStack) {
        this.hatPos = hatPos;
        this.potPos = potPos;
        this.itemStack = itemStack.copy();
    }

    public static PotItemTransferPacket decode(FriendlyByteBuf buf) {
        return new PotItemTransferPacket(buf.readBlockPos(), buf.readBlockPos(), buf.readItem());
    }

    public static void encode(PotItemTransferPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.hatPos);
        buf.writeBlockPos(msg.potPos);
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
                    PotItemTransfers.addItemTransfer(msg.hatPos, msg.potPos, msg.itemStack);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
