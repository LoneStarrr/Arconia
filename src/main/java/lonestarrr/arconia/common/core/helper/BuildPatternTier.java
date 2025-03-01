package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.BuildPattern;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores build patterns for all rainbow color tiers.
 */
public class BuildPatternTier {
    private static final Map<RainbowColor, String> buildPatternResources;
    private static final Map<RainbowColor, BuildPattern> patterns = new HashMap<>(RainbowColor.values().length);
    private static final Map<RainbowColor, BuildPattern> patternsCompleted = new HashMap<>(RainbowColor.values().length);
    private static boolean patternsLoaded = false;

    static {
        Map<RainbowColor, String> tmpResources = new HashMap<>();
        tmpResources.put(RainbowColor.RED, "red.json");
        tmpResources.put(RainbowColor.ORANGE, "orange.json");
        tmpResources.put(RainbowColor.YELLOW, "yellow.json");
        tmpResources.put(RainbowColor.GREEN, "green.json");
        tmpResources.put(RainbowColor.BLUE, "blue.json");
        buildPatternResources = Collections.unmodifiableMap(tmpResources);
    }

    /**
     *
     * @param tier
     * @return The pattern to be built by the player
     */
    public static BuildPattern getPattern(RainbowColor tier) {
        return patterns.get(tier);
    }

    /**
     *
     * @param tier
     * @return The pattern to be placed in the world after the player completes the incomplete variant
     */
    public static BuildPattern getPatternCompleted(RainbowColor tier) {
        return patternsCompleted.get(tier);
    }
    /**
     * Load all patterns and do some pre-calculations.
     *
     * @throws IOException
     * @throws BlockPatternException
     */
    public static void loadPatterns() throws IOException, BlockPatternException {
        if (!patternsLoaded) {
            for (Map.Entry<RainbowColor, String> entry : buildPatternResources.entrySet()) {
                RainbowColor color = entry.getKey();
                String filename = entry.getValue();

                ResourceLocation res = ResourceLocation.fromNamespaceAndPath("arconia", "block_patterns/" + filename);
                try {
                    BuildPattern bp = BuildPattern.loadPattern(res);
                    patterns.put(color, bp);
                } catch (IOException | BlockPatternException e) {
                    Arconia.logger.error("Error loading incomplete build pattern: " + filename);
                    throw e;
                }

                ResourceLocation resCompleted = ResourceLocation.fromNamespaceAndPath("arconia", "block_patterns/completed/" + filename);
                try {
                    BuildPattern bp = BuildPattern.loadPattern(resCompleted);
                    patternsCompleted.put(color, bp);
                } catch (IOException | BlockPatternException e) {
                    Arconia.logger.error("Error loading completed build pattern: " + filename);
                    throw e;
                }
            }
            patternsLoaded = true;
        }
    }

}
