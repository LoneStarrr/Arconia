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
        public final Map<Integer, ModConfigSpec.IntValue> potGenerationInterval = new HashMap<>(RainbowColor.values().length + 1);
        public final Map<Integer, ModConfigSpec.IntValue> potGenerationCount = new HashMap<>(RainbowColor.values().length + 1);
        public final ModConfigSpec.IntValue bonusPerExtraTree;

        public Common(ModConfigSpec.Builder builder) {
            final int[] counts = {1, 1, 2, 2, 4, 4, 8}; // Counts per tier (rainbow color)
            final int[] intervalSeconds = {16, 8, 8, 4, 4, 2, 2}; // interval between item draws per tier
            builder.push("potOfGold");

            // Each color of the rainbow represents a tier
            for (RainbowColor color: RainbowColor.values()) {
                int tier = color.getTier(); // 1..7
                String colorLabel = "tier" + tier;
                int currentGenerationCount = counts[tier - 1];
                int currentGenerationInterval = intervalSeconds[tier - 1] * 20;
                ModConfigSpec.IntValue generationInterval = builder
                        .comment("Time between item generation attempts, in game ticks")
                        .defineInRange(colorLabel + "GenerationInterval", currentGenerationInterval, 5, 1200);
                potGenerationInterval.put(tier, generationInterval);

                ModConfigSpec.IntValue generationCount = builder
                        .comment("Maximum number of items to generate per attempt")
                        .defineInRange(colorLabel + "GenerationCount", currentGenerationCount, 1, 64);
                potGenerationCount.put(tier, generationCount);
            }

            bonusPerExtraTree = builder
                    .comment("Extra item draw bonus chance percentage for each unique tree color of a previous tier planted nearby")
                            .defineInRange("BonusTreePercentage", 33, 0, 200);

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



