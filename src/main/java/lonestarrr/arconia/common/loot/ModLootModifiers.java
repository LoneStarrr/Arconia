package lonestarrr.arconia.common.loot;

import com.mojang.serialization.Codec;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Global loot modifiers
 */
public class ModLootModifiers {
    // Custom Conditions
//    public static final LootItemConditionType PLAYER_NEEDS_GUIDEBOOK = new LootItemConditionType(new PlayerNeedsGuideBook.Serializer());

    // Registries
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> CODECS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Arconia.MOD_ID);
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Arconia.MOD_ID);

    public static final RegistryObject<Codec<DirtLootModifier>> DIRT_MODIFIER = CODECS.register("dirt", () -> DirtLootModifier.createCodec());
    public static final RegistryObject<LootItemConditionType> NEEDS_GUIDEBOOK = LOOT_CONDITION_TYPES.register("player_needs_guidebook", () -> new LootItemConditionType(new PlayerNeedsGuideBook.Serializer()));
}
