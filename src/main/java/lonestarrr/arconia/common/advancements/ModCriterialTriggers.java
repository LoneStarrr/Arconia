package lonestarrr.arconia.common.advancements;

import net.minecraft.advancements.CriteriaTriggers;

/**
 * Custom advancement triggers
 */
public class ModCriterialTriggers {
    public static void init() {
        CriteriaTriggers.register(PotOfGoldTrigger.INSTANCE);
    }
}
