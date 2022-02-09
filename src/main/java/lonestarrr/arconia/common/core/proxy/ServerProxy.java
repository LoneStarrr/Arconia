package lonestarrr.arconia.common.core.proxy;

import lonestarrr.arconia.common.Arconia;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Proxy code inspired by: http://jabelarminecraft.blogspot.com/p/minecraft-modding-organizing-your-proxy.html
 */
public class ServerProxy implements IProxy {
    @Override
    public void registerHandlers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::dedicatedServerSetup);
    }

    public void dedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        Arconia.logger.info("********************* server-side proxy init");
    }
}
