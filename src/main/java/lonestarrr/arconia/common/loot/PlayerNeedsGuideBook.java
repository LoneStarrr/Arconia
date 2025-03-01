package lonestarrr.arconia.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.helper.PatchouliHelper;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Returns true if looter is a real player, who hasn't found the guide book yet
 * Should really break this up into 2 conditions, is a real player, and has found guidebook
 */
public class PlayerNeedsGuideBook implements LootItemCondition {
    public static final PlayerNeedsGuideBook INSTANCE = new PlayerNeedsGuideBook();
    public static MapCodec<PlayerNeedsGuideBook> CODEC = MapCodec.of(Encoder.empty(), Decoder.unit(PlayerNeedsGuideBook.INSTANCE));
    public static final LootItemConditionType NEEDS_GUIDEBOOK = new LootItemConditionType(CODEC);

    private PlayerNeedsGuideBook() {}

    @Override
    public boolean test(LootContext lootContext) {
        Entity looter = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(looter instanceof ServerPlayer) || looter instanceof FakePlayer) {
            return false;
        }

        ServerPlayer player = (ServerPlayer)looter;
        ServerLevel level = player.serverLevel();
        // Only drop a book if the player does not have the advancement yet, AND there is no nearby guide book entity (you could mine a whole bunch of dirt
        // without picking up the book!). I suppose you could game this with e.g. a hopper if you really, really wanted to have a large collection of useless
        // guide books!
        AdvancementHolder guideBookAdvancement = level.getServer().getAdvancements().get(ResourceLocationHelper.prefix("main/root"));
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
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32));

        for (ItemEntity itemEntity : items) {
            ItemStack itemStack = itemEntity.getItem();
            if (PatchouliHelper.isGuideBook(itemStack)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return NEEDS_GUIDEBOOK;
    }
}
