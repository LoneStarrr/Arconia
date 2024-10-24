package lonestarrr.arconia.common.network;

import lonestarrr.arconia.client.effects.PotItemTransfers;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;


/**
 * Packet containing data for visualisation of item transfer between pot of gold and hats
 */
public record PotItemTransferPacket(BlockPos startPos, BlockPos endPos, ItemStack itemStack) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(Arconia.MOD_ID, "pot_item_transfer");

    public PotItemTransferPacket(final FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readBlockPos(), buffer.readItem());
    }

    @Override
    public void write(final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(startPos());
        buffer.writeBlockPos(endPos());
        buffer.writeItem(itemStack());
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }

    // NeoForge example code has a global ClientHandler class that seems like it's intended to handle all client
    // packets, but I prefer to have that logic live with the actual packet code.
    public static class Handler {
        public static void handleClient(final PotItemTransferPacket packet, final PlayPayloadContext ctx) {
            // Run on main thread
            ctx.workHandler().submitAsync(() -> {
                PotItemTransfers.addItemTransfer(packet.startPos(), packet.endPos(), packet.itemStack());
            }).exceptionally(e -> {
                // Unhandled exceptions are silently swallowed
                // TODO actually add this language key
                ctx.packetHandler().disconnect(Component.translatable("arconia.networking.failed", e.getMessage()));
                return null;
            });
        }
    }
}
