package lonestarrr.arconia.compat.theoneprobe;

import lonestarrr.arconia.common.Arconia;
import mcjty.theoneprobe.api.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TheOneProbe {
    private static boolean registered;

    public static void init() {
        // Figuring out The One Probe integration was quite a challenge - ended up following the implementation in xnet/mcjtylib
        if (ModList.get().isLoaded("theoneprobe")) {
            register();
        }
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", GetTheOneProbe::new);
    }

    // see mcjtylib's TOPCompatibility
    public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {
        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            Arconia.logger.info("Registered The One Probe integration");
            // API: https://github.com/McJtyMods/TheOneProbe/tree/1.16/src/main/java/mcjty/theoneprobe/api
            // It seems McJTY registered a provider, even though the API code says it's not necessary and there are three default categories that should
            // suffice. Instead, one can implement IProbeInfoAccessor to display default info when a block is being viewed. But, the actual implementation
            // in e.g. xnet puts all of the extra probe info into a single file instead of having the probe info in the actual block/tile entity
            // classes. The abstractions confused me greatly!
            theOneProbe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return Arconia.MOD_ID + ":default";
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
                    /* McJTY's implementation
                    if (blockState.getBlock() instanceof TOPInfoProvider) {
                        TOPInfoProvider provider = (TOPInfoProvider) blockState.getBlock();
                        TOPDriver driver = provider.getProbeDriver();
                        if (driver != null) {
                            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                        }
                    }
                     */
                    // I, instead, will just have blocks implement the driver directly. If I want a bunch of specific TOPDriver subclasses, I will add
                    // them here in this package and have the blocks directly implement those. Why would I want that extra getProbeDriver() step?
                    if (blockState.getBlock() instanceof TOPDriver) {
                        TOPDriver driver = (TOPDriver)blockState.getBlock();
                        driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                    }
                }
            });

            return null;
        }
    }
}
