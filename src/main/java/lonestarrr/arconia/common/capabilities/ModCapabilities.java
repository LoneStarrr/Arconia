package lonestarrr.arconia.common.capabilities;

import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CENTER_PEDESTAL.get(), (o, direction) -> o.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.PEDESTAL.get(), (o, direction) -> o.getItemHandler());
    }
}
