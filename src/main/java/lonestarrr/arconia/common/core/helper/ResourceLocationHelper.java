package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.resources.Identifier;

public class ResourceLocationHelper {
    public static Identifier prefix(String path) {
        return Identifier.fromNamespaceAndPath(Arconia.MOD_ID, path);
    }
}
