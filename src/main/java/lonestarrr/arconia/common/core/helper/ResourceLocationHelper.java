package lonestarrr.arconia.common.core.helper;

import net.minecraft.resources.ResourceLocation;
import lonestarrr.arconia.common.Arconia;

public class ResourceLocationHelper {
    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(Arconia.MOD_ID, path);
    }
}
