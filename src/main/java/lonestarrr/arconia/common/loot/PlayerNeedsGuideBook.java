package lonestarrr.arconia.common.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.helper.PatchouliHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;
import java.util.Set;

/**
 * Returns true if looter is a real player, who hasn't found the guide book yet
 * Should really break this up into 2 conditions, is a real player, and has found guidebook
 */
public class PlayerNeedsGuideBook implements ILootCondition {
    @Override
    public boolean test(LootContext lootContext) {
        Entity looter = lootContext.get(LootParameters.THIS_ENTITY);
        if (!(looter instanceof ServerPlayerEntity) || looter instanceof FakePlayer) {
            return false;
        }

        ServerPlayerEntity player = (ServerPlayerEntity)looter;
        ServerWorld world = player.getServerWorld();
        // Only drop a book if the player does not have the advancement yet, AND there is no nearby guide book entity (you could mine a whole bunch of dirt
        // without picking up the book!). I suppose you could game this with e.g. a hopper if you really, really wanted to have a large collection of useless
        // guide books!
        Advancement guideBookAdvancement = world.getServer().getAdvancementManager().getAdvancement(new ResourceLocation(Arconia.MOD_ID, "main/root"));
        if (guideBookAdvancement == null) {
            // Should not happen, but if it does, better be safe than sorry!
            Arconia.logger.error("Missing guide book advancement");
            return false;
        }
        boolean hasGuideBookAdvancement = player.getAdvancements().getProgress(guideBookAdvancement).isDone();
        if (hasGuideBookAdvancement) {
            return false;
        }

        // Check if the player has recently mined a block that already did drop the book by checking for the entity in the near vicinity
        List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, player.getBoundingBox().grow(32));

        for (ItemEntity itemEntity : items) {
            ItemStack itemStack = itemEntity.getItem();
            if (PatchouliHelper.isGuideBook(itemStack)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<LootParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootParameters.THIS_ENTITY);
    }

    @Override
    public LootConditionType func_230419_b_() {
        return ModLootModifiers.PLAYER_NEEDS_GUIDEBOOK;
    }

    public static class Serializer implements ILootSerializer<PlayerNeedsGuideBook> {
        @Override
        public void serialize(JsonObject json, PlayerNeedsGuideBook condition, JsonSerializationContext ctx) {}

        @Override
        public PlayerNeedsGuideBook deserialize(JsonObject json, JsonDeserializationContext ctx) {
            return new PlayerNeedsGuideBook();
        }
    }
}
