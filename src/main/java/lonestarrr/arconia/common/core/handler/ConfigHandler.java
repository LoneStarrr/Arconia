package lonestarrr.arconia.common.core.handler;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraftforge.common.ForgeConfigSpec;
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
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> goldArconiumCoinCounts = new HashMap<>(RainbowColor.values().length);
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> goldArconiumCoinInterval = new HashMap<>(RainbowColor.values().length);
        public final ForgeConfigSpec.IntValue potOfGoldMaxHats;
        public final ForgeConfigSpec.IntValue potOfGoldTicksPerInterval;
        public final ForgeConfigSpec.IntValue potOfGoldMaxHatDistance;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("potOfGold");

            potOfGoldMaxHats = builder
                    .comment("Maximum number of hats that can be linked to a single pot of gold")
                    .defineInRange("maxHats", 32, 2, 64);
            potOfGoldTicksPerInterval = builder
                    .comment("Number of ticks per interval, which defines the speed at which the pot works")
                    .defineInRange("ticksPerInterval", 5, 5, 256);
            potOfGoldMaxHatDistance = builder
                    .comment("Maximum distance at which hats can be linked to a pot of gold")
                    .defineInRange("maxHatDistance", 16, 4, 64);

            builder.push("goldArconiumBlock");
            int defaultCoinCount = (int) Math.pow(2, 8);
            int defaultCoinInterval = 10;

            for (RainbowColor color : RainbowColor.values()) {
                ForgeConfigSpec.IntValue coinAmount = builder
                        .comment("Number of gold coins produced before the block transforms into pure arconium")
                        .defineInRange(color.getTierName() + "CoinCount", defaultCoinCount, 1, Integer.MAX_VALUE);
                goldArconiumCoinCounts.put(color, coinAmount);
                defaultCoinCount *= 2;

                ForgeConfigSpec.IntValue coinInterval = builder
                        .comment("Coin generation interval. Interval tick length is determined by pot of gold")
                        .defineInRange(color.getTierName() + "CoinInterval", defaultCoinInterval, 1, 256);
                goldArconiumCoinInterval.put(color, coinInterval);
                defaultCoinInterval -= 1;
            }
            builder.pop();


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



