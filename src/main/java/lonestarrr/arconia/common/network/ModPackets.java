package lonestarrr.arconia.common.network;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import lonestarrr.arconia.common.Arconia;

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
        HANDLER.registerMessage(id++, RainbowCratePacket.class, RainbowCratePacket::encode,
                RainbowCratePacket::decode, RainbowCratePacket.Handler::handle);
        HANDLER.registerMessage(id++, OrbLaserPacket.class, OrbLaserPacket::encode,
                OrbLaserPacket::decode, OrbLaserPacket.Handler::handle);
        HANDLER.registerMessage(id++, PotItemTransferPacket.class, PotItemTransferPacket::encode,
                PotItemTransferPacket::decode, PotItemTransferPacket.Handler::handle);
    }

    /**
     * Send a network packet to anyone within a radius of 64 meters of the given position
     * @param world
     * @param pos
     * @param toSend Packet to send (why does this not bother to implement at least an interface?)
     */
    public static void sendToNearby(World world, BlockPos pos, Object toSend) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;

            serverWorld.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false)
                    .filter(p -> p.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64)
                    .forEach(p -> HANDLER.send(PacketDistributor.PLAYER.with(() -> p), toSend));
        }
    }
}
