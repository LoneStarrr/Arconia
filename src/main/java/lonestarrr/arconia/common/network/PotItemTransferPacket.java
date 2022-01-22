package lonestarrr.arconia.common.network;

import lonestarrr.arconia.client.effects.OrbLasers;
import lonestarrr.arconia.client.effects.PotItemTransfers;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    public static PotItemTransferPacket decode(PacketBuffer buf) {
        return new PotItemTransferPacket(buf.readBlockPos(), buf.readBlockPos(), buf.readItem());
    }

    public static void encode(PotItemTransferPacket msg, PacketBuffer buf) {
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
                    World world = mc.level;
                    PotItemTransfers.addItemTransfer(msg.hatPos, msg.potPos, msg.itemStack);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
