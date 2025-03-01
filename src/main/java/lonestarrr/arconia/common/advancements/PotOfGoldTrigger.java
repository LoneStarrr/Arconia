package lonestarrr.arconia.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Advancement trigger for creating a pot of gold
 */
public class PotOfGoldTrigger extends SimpleCriterionTrigger<PotOfGoldTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return PotOfGoldTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, TriggerInstance::matches);
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<PotOfGoldTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                r -> r.group(
                                Codec.optionalField("player", EntityPredicate.ADVANCEMENT_CODEC, false).forGetter(PotOfGoldTrigger.TriggerInstance::player)
                        )
                        .apply(r, PotOfGoldTrigger.TriggerInstance::new)
        );

        boolean matches() {
            return true;
        }
    }
}
