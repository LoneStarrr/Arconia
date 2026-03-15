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
        public final Map<RainbowColor, ModConfigSpec.IntValue> leavesItemCredits = new HashMap<>(RainbowColor.values().length);
        public final ModConfigSpec.IntValue bonusPerExtraTree;

        public Common(ModConfigSpec.Builder builder) {
            final int[] counts = {1, 1, 2, 2, 4, 4, 8}; // Counts per tier (rainbow color)
            final int[] intervalSeconds = {16, 8, 8, 4, 4, 2, 2}; // interval between item draws per tier
            final int[] itemCreditsFromLeaves = {1, 2, 4, 8, 16, 32, 64}; // #items produced per consumed leaves block
            builder.push("potOfGold");

            // Each color of the rainbow represents a tier
            for (RainbowColor color: RainbowColor.values()) {
                int tier = color.getTier(); // 1..7
                String colorLabel = color.getTierName().toLowerCase();
                int currentGenerationCount = counts[tier - 1];
                int currentGenerationInterval = intervalSeconds[tier - 1] * 20;
                ModConfigSpec.IntValue generationInterval = builder
                        .comment("Time between item generation attempts, in game ticks")
                        .defineInRange(colorLabel + "GenerationInterval", currentGenerationInterval, 5, 1200);
                potGenerationInterval.put(color, generationInterval);

                ModConfigSpec.IntValue generationCount = builder
                        .comment("Maximum number of items to generate per attempt")
                        .defineInRange(colorLabel + "GenerationCount", currentGenerationCount, 1, 64);
                potGenerationCount.put(color, generationCount);

                ModConfigSpec.IntValue itemCredits = builder
                        .comment("Number of items extracted from the pot for each leaves block consumed")
                        .defineInRange(colorLabel + "ItemCreditsFromLeaves", itemCreditsFromLeaves[tier -1], 1, 256);
                leavesItemCredits.put(color, itemCredits);
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



