package lonestarrr.arconia.common.advancements;

import com.google.gson.JsonObject;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.advancements.criterion.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;

/**
 * Advancement trigger for creating a pot of gold
 */
public class PotOfGoldTrigger extends AbstractCriterionTrigger<PotOfGoldTrigger.Instance> {
    public static final ResourceLocation ID = new ResourceLocation(Arconia.MOD_ID, "create_pot_of_gold");
    public static final PotOfGoldTrigger INSTANCE = new PotOfGoldTrigger();

    private PotOfGoldTrigger() {}

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Nonnull
    @Override
    public Instance deserializeTrigger(@Nonnull JsonObject json, EntityPredicate.AndPredicate playerPred, ConditionArrayParser conditions) {
        // This allows mod pack authors to limit where the pot can be constructed through a datapack
        return new Instance(playerPred, LocationPredicate.deserialize(json.get("location")));
    }

    public void trigger(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        triggerListeners(player, instance -> instance.test(world, pos));
    }

    static class Instance extends CriterionInstance {
        private final LocationPredicate pos;

        Instance(EntityPredicate.AndPredicate playerPred, LocationPredicate pos) {
            super(ID, playerPred);
            this.pos = pos;
        }

        @Nonnull
        @Override
        public ResourceLocation getId() {
            return ID;
        }

        boolean test(ServerWorld world, BlockPos pos) {
            return this.pos.test(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
