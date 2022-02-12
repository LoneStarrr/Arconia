package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationHelper {
    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(Arconia.MOD_ID, path);
    }
}
