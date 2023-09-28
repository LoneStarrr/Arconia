package lonestarrr.arconia.common.loot;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Global loot modifiers
 */
public class ModLootModifiers {
    // Custom Conditions
//    public static final LootItemConditionType PLAYER_NEEDS_GUIDEBOOK = new LootItemConditionType(new PlayerNeedsGuideBook.Serializer());

    // Global modifiers
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, Arconia.MOD_ID);
    public static final RegistryObject<GlobalLootModifierSerializer<?>> DIRT_MODIFIER = LOOT_MODIFIERS.register("dirt", () -> new DirtLootModifier.Serializer());
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registry.LOOT_ITEM_REGISTRY, Arconia.MOD_ID);
    public static final RegistryObject<LootItemConditionType> NEEDS_GUIDEBOOK = LOOT_CONDITION_TYPES.register("player_needs_guidebook", () -> new LootItemConditionType(new PlayerNeedsGuideBook.Serializer()));

    public static void register(IEventBus modBus) {
        LOOT_MODIFIERS.register(modBus);
        LOOT_CONDITION_TYPES.register(modBus);
    }
}
