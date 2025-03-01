package lonestarrr.arconia.common.core.handler;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Mod configuration
 */
public final class ConfigHandler {
    public static class Client {
        public Client(ModConfigSpec.Builder builder) {
        }
    }

    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Common {
        public final Map<RainbowColor, ModConfigSpec.IntValue> potGenerationInterval = new HashMap<>(RainbowColor.values().length);
        public final Map<RainbowColor, ModConfigSpec.IntValue> potGenerationCount = new HashMap<>(RainbowColor.values().length);

        public Common(ModConfigSpec.Builder builder) {
            builder.push("potOfGold");

            for (RainbowColor color : RainbowColor.values()) {
                int currentGenerationInterval = (color.getColorValue() * 2 + 1);
                int currentGenerationCount = 75 - 10 * color.getColorValue();
                ModConfigSpec.IntValue generationInterval = builder
                        .comment("Time between item generation attempts, in game ticks")
                        .defineInRange(color.getTierName() + "GenerationInterval", currentGenerationInterval, 5, 1200);
                potGenerationInterval.put(color, generationInterval);

                ModConfigSpec.IntValue generationCount = builder
                        .comment("Maximum number of items to generate per attempt")
                        .defineInRange(color.getTierName() + "GenerationCount", currentGenerationCount, 1, 64);
                potGenerationCount.put(color, generationCount);
            }

            builder.pop(); // potOfGold
        }
    }

    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static void onConfigLoad(ModConfigEvent.Loading evt) {
        Arconia.configLoaded = true;
    }

    public static void onConfigReload(ModConfigEvent.Reloading evt) {
        Arconia.configLoaded = true;
    }

}



