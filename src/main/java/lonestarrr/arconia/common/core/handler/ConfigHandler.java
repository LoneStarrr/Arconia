package lonestarrr.arconia.common.core.handler;

import lonestarrr.arconia.common.Arconia;
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
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> goldArconiumCoinCounts = new HashMap<>(RainbowColor.values().length);
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> goldArconiumCoinCapacity = new HashMap<>(RainbowColor.values().length);
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> goldArconiumCoinInterval = new HashMap<>(RainbowColor.values().length);
        public final Map<RainbowColor, ForgeConfigSpec.BooleanValue> goldArconiumIsInfinite = new HashMap<>(RainbowColor.values().length);

        public final ForgeConfigSpec.IntValue potOfGoldMaxHats;
        public final ForgeConfigSpec.IntValue potOfGoldTicksPerInterval;
        public final ForgeConfigSpec.IntValue potOfGoldMaxHatDistance;
        public final Map<RainbowColor, ForgeConfigSpec.IntValue> itemCost = new HashMap<>(RainbowColor.values().length);

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("potOfGold");
            potOfGoldMaxHats = builder
                    .comment("Maximum number of hats that can be linked to a single pot of gold")
                    .defineInRange("maxHats", 32, 2, 64);
            potOfGoldTicksPerInterval = builder
                    .comment("Number of game ticks per 'pot tick', which defines the base speed the pot operates at.")
                    .defineInRange("ticksPerInterval", 5, 5, 256);
            potOfGoldMaxHatDistance = builder
                    .comment("Maximum distance at which hats can be linked to a pot of gold")
                    .defineInRange("maxHatDistance", 16, 4, 64);

            int currentItemCost = 1;

            for (RainbowColor color : RainbowColor.values()) {
                ForgeConfigSpec.IntValue itemCostForTier = builder
                        .comment("Number of gold coins required to produce an item of this tier")
                        .defineInRange(color.getTierName() + "CoinInterval", currentItemCost, 1, Integer.MAX_VALUE);
                itemCost.put(color, itemCostForTier);
                currentItemCost *= 2;
            }

            builder.pop(); // potOfGold

            builder.push("goldArconiumBlock");
            int currentCoinCount = 2;
            int currentCoinInterval = 13;
            int currentCoinCapacity = 256;

            for (RainbowColor color : RainbowColor.values()) {
                ForgeConfigSpec.IntValue coinInterval = builder
                        .comment("How frequently are coins collected from this tier's gold arconium block by the pot. Expressed as the number of 'pot ticks'. The length of a single 'pot tick' is a configuration parameter of the pot.")
                        .defineInRange(color.getTierName() + "CoinInterval", currentCoinInterval, 1, 256);
                goldArconiumCoinInterval.put(color, coinInterval);
                currentCoinInterval -= 2;

                ForgeConfigSpec.IntValue coinAmount = builder
                        .comment("How many coins are collected by the pot whenever it ticks. This number should be at least equal to the item cost for the same tier to prevent them from being generated at all.")
                        .defineInRange(color.getTierName() + "CoinCount", currentCoinCount, 1, Integer.MAX_VALUE);
                goldArconiumCoinCounts.put(color, coinAmount);
                currentCoinCount *= 3;

                ForgeConfigSpec.BooleanValue isInfinite = builder
                        .comment("Whether this gold arconium block has infinite capacity, e.g. it never runs out of coins.")
                        .define(color.getTierName() + "IsInfinite", true);
                goldArconiumIsInfinite.put(color, isInfinite);

                ForgeConfigSpec.IntValue coinCapacity = builder
                        .comment("If this gold arconium block does not have an infinite supply, the total amount of gold it has from the start.")
                        .defineInRange(color.getTierName() + "CoinCapacity", currentCoinCapacity, 1, Integer.MAX_VALUE);
                goldArconiumCoinCapacity.put(color, coinCapacity);
                currentCoinCapacity *= 3;
            }
            builder.pop(); // goldArconiumBlock
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



