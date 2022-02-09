package lonestarrr.arconia.common.advancements;

import com.google.gson.JsonObject;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

/**
 * Advancement trigger for creating a pot of gold
 */
public class PotOfGoldTrigger extends SimpleCriterionTrigger<PotOfGoldTrigger.Instance> {
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
    public Instance createInstance(@Nonnull JsonObject json, EntityPredicate.Composite playerPred, DeserializationContext conditions) {
        // This allows mod pack authors to limit where the pot can be constructed through a datapack
        return new Instance(playerPred, LocationPredicate.fromJson(json.get("location")));
    }

    public void trigger(ServerPlayer player, ServerLevel world, BlockPos pos) {
        trigger(player, instance -> instance.test(world, pos));
    }

    static class Instance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate pos;

        Instance(EntityPredicate.Composite playerPred, LocationPredicate pos) {
            super(ID, playerPred);
            this.pos = pos;
        }

        @Nonnull
        @Override
        public ResourceLocation getCriterion() {
            return ID;
        }

        boolean test(ServerLevel world, BlockPos pos) {
            return this.pos.matches(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
