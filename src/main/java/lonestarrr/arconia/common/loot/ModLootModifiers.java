package lonestarrr.arconia.common.loot;

import com.mojang.serialization.Codec;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Global loot modifiers
 */
public class ModLootModifiers {
    // Custom Conditions
//    public static final LootItemConditionType PLAYER_NEEDS_GUIDEBOOK = new LootItemConditionType(new PlayerNeedsGuideBook.Serializer());

    // Registries
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Arconia.MOD_ID);
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE, Arconia.MOD_ID);

    public static final Supplier<Codec<DirtLootModifier>> DIRT_MODIFIER = CODECS.register("dirt", () -> DirtLootModifier.createCodec());
    public static final Supplier<LootItemConditionType> NEEDS_GUIDEBOOK = LOOT_CONDITION_TYPES.register("player_needs_guidebook", () -> new LootItemConditionType(new PlayerNeedsGuideBook.Serializer()));
}
