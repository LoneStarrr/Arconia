package lonestarrr.arconia.common.network;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class ModPackets {

    public static void registerPackets(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(Arconia.MOD_ID);
        registrar.play(
            PotItemTransferPacket.ID,
            PotItemTransferPacket::new, handler -> handler.client(PotItemTransferPacket.Handler::handleClient)
        );
    }

    /**
     * Send a network packet to any players that have loaded the chunk the given position is at
     * @param level
     * @param pos
     * @param toSend Packet to send
     */
    public static void sendToNearby(Level level, BlockPos pos, CustomPacketPayload toSend) {
        if (!level.isClientSide()) {
            PacketDistributor.TRACKING_CHUNK.with(level.getChunkAt(pos)).send(toSend);
        }
    }
}
