package lonestarrr.arconia.common.loot;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Global loot modifiers
 */
public class ModLootModifiers {
    // Custom Conditions
    public static final LootItemConditionType PLAYER_NEEDS_GUIDEBOOK = new LootItemConditionType(new PlayerNeedsGuideBook.Serializer());

    // Global modifiers
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, Arconia.MOD_ID);
    public static final RegistryObject<GlobalLootModifierSerializer<?>> DIRT_MODIFIER = LOOT_MODIFIERS.register("dirt", () -> new DirtLootModifier.Serializer());

    public static void init() {
        Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(Arconia.MOD_ID, "player_needs_guidebook"), PLAYER_NEEDS_GUIDEBOOK);
    }
}
