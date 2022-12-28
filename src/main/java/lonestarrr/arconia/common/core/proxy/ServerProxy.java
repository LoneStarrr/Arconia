package lonestarrr.arconia.common.core.proxy;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.entities.WorldBuilderEntity;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;

/**
 * Proxy code inspired by: http://jabelarminecraft.blogspot.com/p/minecraft-modding-organizing-your-proxy.html
 */
public class ServerProxy implements IProxy {
    @Override
    public void registerHandlers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::dedicatedServerSetup);
    }

    public void dedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        // This is only run in headless server mode, i.e. the physical server, not the logical server which is present also in a single-player minecraft instance.
        Arconia.logger.info("********************* server-side proxy init");
    }
}
