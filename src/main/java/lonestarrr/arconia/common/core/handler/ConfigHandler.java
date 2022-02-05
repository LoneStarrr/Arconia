package lonestarrr.arconia.common.core.handler;

import lonestarrr.arconia.common.Arconia;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod configuration
 */
public final class ConfigHandler {
    public static class Client {
        public Client(ForgeConfigSpec.Builder builder) {
        }
    }

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Common {
        public Common(ForgeConfigSpec.Builder builder) {
        }
    }

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static void onConfigLoad() {
        Arconia.configLoaded = true;
    }
}



