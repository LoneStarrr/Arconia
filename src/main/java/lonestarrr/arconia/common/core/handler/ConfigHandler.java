package lonestarrr.arconia.common.core.handler;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

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
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> potGenerationInterval = new HashMap<>(RainbowColor.values().length);
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> potGenerationCount = new HashMap<>(RainbowColor.values().length);

        public final ForgeConfigSpec.IntValue potOfGoldMaxHats;
        public final ForgeConfigSpec.IntValue potOfGoldMaxHatDistance;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("potOfGold");
            potOfGoldMaxHats = builder
                    .comment("Maximum number of hats that can be linked to a single pot of gold")
                    .defineInRange("maxHats", 64, 2, 128);
            potOfGoldMaxHatDistance = builder
                    .comment("Maximum distance at which hats can be linked to a pot of gold")
                    .defineInRange("maxHatDistance", 16, 4, 64);

            int currentGenerationInterval = 100;
            int currentGenerationCount = 2;

            for (RainbowColor color : RainbowColor.values()) {
                ForgeConfigSpec.IntValue generationInterval = builder
                        .comment("Time between item generation attempts, in game ticks")
                        .defineInRange(color.getTierName() + "GenerationInterval", currentGenerationInterval, PotMultiBlockPrimaryBlockEntity.MIN_TICK_INTERVAL, 1200);
                potGenerationInterval.put(color, generationInterval);
                currentGenerationInterval -= 15;

                ForgeConfigSpec.IntValue generationCount = builder
                        .comment("Maximum number of items to generate per attempt")
                        .defineInRange(color.getTierName() + "GenerationCount", currentGenerationCount, 1, 256);
                potGenerationCount.put(color, generationCount);
                currentGenerationCount *= 2;
            }

            builder.pop(); // potOfGold
        }
    }

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
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



