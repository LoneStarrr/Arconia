package lonestarrr.arconia.common.loot;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.loot.LootConditionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Global loot modifiers
 */
public class ModLootModifiers {
    // Custom Conditions
    public static final LootConditionType PLAYER_NEEDS_GUIDEBOOK = new LootConditionType(new PlayerNeedsGuideBook.Serializer());

    // Global modifiers
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, Arconia.MOD_ID);
    public static final RegistryObject<GlobalLootModifierSerializer<?>> DIRT_MODIFIER = LOOT_MODIFIERS.register("dirt", () -> new DirtLootModifier.Serializer());

    public static void init() {
        Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(Arconia.MOD_ID, "player_needs_guidebook"), PLAYER_NEEDS_GUIDEBOOK);
    }
}
