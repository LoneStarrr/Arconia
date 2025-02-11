package lonestarrr.arconia.common.advancements;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom advancement triggers
 */
public class ModCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Arconia.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, PotOfGoldTrigger> CREATE_POT_OF_GOLD_TRIGGER = CRITERIA_TRIGGERS.register("create_pot_of_gold", PotOfGoldTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, TouchGrassTrigger> TOUCH_GRASS_TRIGGER = CRITERIA_TRIGGERS.register("touch_grass", TouchGrassTrigger::new);
}
