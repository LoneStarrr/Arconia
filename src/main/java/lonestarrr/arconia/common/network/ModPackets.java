package lonestarrr.arconia.common.network;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPackets {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Arconia.MOD_ID, "chan"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void init() {
        int id = 0;
        HANDLER.registerMessage(id++, PotItemTransferPacket.class, PotItemTransferPacket::encode,
                PotItemTransferPacket::decode, PotItemTransferPacket.Handler::handle);
    }

    /**
     * Send a network packet to anyone within a radius of 64 meters of the given position
     * @param world
     * @param pos
     * @param toSend Packet to send (why does this not bother to implement at least an interface?)
     */
    public static void sendToNearby(Level world, BlockPos pos, Object toSend) {
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;

            serverWorld.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)
                    .stream().filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64)
                    .forEach(p -> HANDLER.send(PacketDistributor.PLAYER.with(() -> p), toSend));
        }
    }
}
