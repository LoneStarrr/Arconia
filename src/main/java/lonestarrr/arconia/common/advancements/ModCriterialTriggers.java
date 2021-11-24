package lonestarrr.arconia.common.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public class ModCriterialTriggers {
    public static void init() {
        CriteriaTriggers.register(PotOfGoldTrigger.INSTANCE);
    }
}
