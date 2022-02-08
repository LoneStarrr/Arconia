package lonestarrr.arconia.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent;
import lonestarrr.arconia.client.effects.OrbLasers;
import lonestarrr.arconia.common.block.tile.RainbowCrateTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;

import java.util.function.Supplier;

/**
 * Packet containing data for Orb lasers to visualize on the client side.
 */
public class OrbLaserPacket {
    private final BlockPos orbPos;
    private final BlockPos itemPos;
    private final ItemStack itemStack;

    public OrbLaserPacket(BlockPos orbPos, BlockPos itemPos, ItemStack itemStack) {
        this.orbPos = orbPos;
        this.itemPos = itemPos;
        this.itemStack = itemStack.copy();
    }

    public static OrbLaserPacket decode(FriendlyByteBuf buf) {
        return new OrbLaserPacket(buf.readBlockPos(), buf.readBlockPos(), buf.readItem());
    }

    public static void encode(OrbLaserPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.orbPos);
        buf.writeBlockPos(msg.itemPos);
        buf.writeItem(msg.itemStack);
    }

    public static class Handler {
        public static void handle(final OrbLaserPacket msg, final Supplier<NetworkEvent.Context> ctx) {
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
                    OrbLasers.addLaserBeam(msg.orbPos, msg.itemPos, msg.itemStack);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
