package lonestarrr.arconia.common.loot;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lonestarrr.arconia.common.core.helper.PatchouliHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * When a player harvests a dirt block, add this mod's guide to the loot table under the condition that they haven't received one through this method
 * before. To be used in conjunction with this mod's loot condition that checks whether this player has already received/mined the book.
 */
public class DirtLootModifier extends LootModifier {
    protected DirtLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ItemStack bookStack = PatchouliHelper.createGuideBook();
        generatedLoot.add(bookStack);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.DIRT_MODIFIER.get();
    }

    public static Codec<DirtLootModifier> createCodec() {
        return RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, DirtLootModifier::new));
    }

    // add Serializer to support data packs
    // TODO this whole thing can go, superseded by CODEC ?
//    public static class Serializer extends GlobalLootModifierSerializer<DirtLootModifier> {
//        @Override
//        public DirtLootModifier read(ResourceLocation name, JsonObject json, LootItemCondition[] conditions) {
//            return new DirtLootModifier(conditions);
//        }
//
//        @Override
//        public JsonObject write(DirtLootModifier modifier) {
//            JsonObject json = makeConditions(modifier.conditions);
//            return json;
//        }
//
//    }
}
