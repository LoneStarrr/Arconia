package lonestarrr.arconia.common.network;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {

    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Arconia.MOD_ID);
        registrar.playToClient(
            PotItemTransferPacket.TYPE,
            PotItemTransferPacket.STREAM_CODEC,
            PotItemTransferPacket.Handler::handleClient
        );
    }

    /**
     * Send a network packet to any players that have loaded the chunk the given position is at
     * @param level
     * @param pos
     * @param toSend Packet to send
     */
    public static void sendToNearby(ServerLevel level, BlockPos pos, CustomPacketPayload toSend) {
        if (!level.isClientSide()) {
            PacketDistributor.sendToPlayersNear(level, null, pos.getX(), pos.getY(), pos.getZ(), 64, toSend);
        }
    }
}
