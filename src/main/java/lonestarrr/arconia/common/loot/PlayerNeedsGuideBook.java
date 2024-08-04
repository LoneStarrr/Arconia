package lonestarrr.arconia.common.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.helper.PatchouliHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Returns true if looter is a real player, who hasn't found the guide book yet
 * Should really break this up into 2 conditions, is a real player, and has found guidebook
 */
public class PlayerNeedsGuideBook implements LootItemCondition {
    @Override
    public boolean test(LootContext lootContext) {
        Entity looter = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(looter instanceof ServerPlayer) || looter instanceof FakePlayer) {
            return false;
        }

        ServerPlayer player = (ServerPlayer)looter;
        ServerLevel world = player.serverLevel();
        // Only drop a book if the player does not have the advancement yet, AND there is no nearby guide book entity (you could mine a whole bunch of dirt
        // without picking up the book!). I suppose you could game this with e.g. a hopper if you really, really wanted to have a large collection of useless
        // guide books!
        Advancement guideBookAdvancement = world.getServer().getAdvancements().getAdvancement(new ResourceLocation(Arconia.MOD_ID, "main/root"));
        if (guideBookAdvancement == null) {
            // Should not happen, but if it does, better be safe than sorry!
            Arconia.logger.error("Missing guide book advancement");
            return false;
        }
        boolean hasGuideBookAdvancement = player.getAdvancements().getOrStartProgress(guideBookAdvancement).isDone();
        if (hasGuideBookAdvancement) {
            return false;
        }

        // Check if the player has recently mined a block that already did drop the book by checking for the entity in the near vicinity
        List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32));

        for (ItemEntity itemEntity : items) {
            ItemStack itemStack = itemEntity.getItem();
            if (PatchouliHelper.isGuideBook(itemStack)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public LootItemConditionType getType() {
        return ModLootModifiers.NEEDS_GUIDEBOOK.get();
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<PlayerNeedsGuideBook> {
        @Override
        public void serialize(@Nonnull JsonObject json, @Nonnull PlayerNeedsGuideBook value, @Nonnull JsonSerializationContext context) {}

        @Override
        public PlayerNeedsGuideBook deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
            return new PlayerNeedsGuideBook();
        }
    }
}
