package lonestarrr.arconia.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lonestarrr.arconia.common.core.helper.PatchouliHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * When a player harvests a dirt block, add this mod's guide to the loot table under the condition that they haven't received one through this method
 * before. To be used in conjunction with this mod's loot condition that checks whether this player has already received/mined the book.
 */
public class DirtLootModifier extends LootModifier {
    public static final MapCodec<DirtLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, DirtLootModifier::new));
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
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.DIRT_MODIFIER.get();
    }
}
