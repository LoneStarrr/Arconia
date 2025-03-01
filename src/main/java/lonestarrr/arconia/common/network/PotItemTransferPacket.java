package lonestarrr.arconia.common.network;

import lonestarrr.arconia.client.effects.PotItemTransfers;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;


/**
 * Packet containing data for visualisation of item transfer between pot of gold and hats
 */
public record PotItemTransferPacket(BlockPos startPos, BlockPos endPos, ItemStack itemStack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PotItemTransferPacket> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(Arconia.MOD_ID, "pot_item_transfer"));
    // composite: convenient instance method to build a StreamCodec with pairs of stream codecs and getters + constructor
    public static final StreamCodec<RegistryFriendlyByteBuf, PotItemTransferPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PotItemTransferPacket::startPos,
            BlockPos.STREAM_CODEC, PotItemTransferPacket::endPos,
            ItemStack.OPTIONAL_STREAM_CODEC, PotItemTransferPacket::itemStack,
            PotItemTransferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // NeoForge example code has a global ClientHandler class that seems like it's intended to handle all client
    // packets, but I prefer to have that logic live with the actual packet code.
    public static class Handler {
        public static void handleClient(final PotItemTransferPacket packet, final IPayloadContext ctx) {
            // Run on main thread
            ctx.enqueueWork(() -> {
                PotItemTransfers.addItemTransfer(packet.startPos(), packet.endPos(), packet.itemStack());
            }).exceptionally(e -> {
                // Unhandled exceptions are silently swallowed
                ctx.disconnect(Component.translatable("arconia.networking.potitemtransfer_failed", e.getMessage()));
                return null;
            });
        }
    }
}
