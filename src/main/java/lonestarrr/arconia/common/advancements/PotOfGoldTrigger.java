package lonestarrr.arconia.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Advancement trigger for creating a pot of gold
 */
public class PotOfGoldTrigger extends SimpleCriterionTrigger<PotOfGoldTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation(Arconia.MOD_ID, "create_pot_of_gold");
    public static final PotOfGoldTrigger INSTANCE = new PotOfGoldTrigger();

    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return PotOfGoldTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ServerLevel world, BlockPos pos) {
        trigger(player, instance -> instance.matches(world, pos));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player, Optional<LocationPredicate> location
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<PotOfGoldTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                r -> r.group(
                                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PotOfGoldTrigger.TriggerInstance::player),
                                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "location")
                                        .forGetter(PotOfGoldTrigger.TriggerInstance::location)
                        )
                        .apply(r, PotOfGoldTrigger.TriggerInstance::new)
        );

        boolean matches(ServerLevel world, BlockPos pos) {
            return this.location.isPresent() && this.location.get().matches(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
