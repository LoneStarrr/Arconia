package lonestarrr.arconia.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/** Advancement trigger for creating a pot of gold */
public class PotOfGoldTrigger extends SimpleCriterionTrigger<PotOfGoldTrigger.TriggerInstance> {
  @Override
  public @NotNull Codec<TriggerInstance> codec() {
    return PotOfGoldTrigger.TriggerInstance.CODEC;
  }

  public void trigger(ServerPlayer player) {
    trigger(player, TriggerInstance::matches);
  }

  public record TriggerInstance(Optional<ContextAwarePredicate> player)
      implements SimpleCriterionTrigger.SimpleInstance {
    public static final Codec<PotOfGoldTrigger.TriggerInstance> CODEC =
        RecordCodecBuilder.create(
            r ->
                r.group(
                        Codec.optionalField("player", EntityPredicate.ADVANCEMENT_CODEC, false)
                            .forGetter(PotOfGoldTrigger.TriggerInstance::player))
                    .apply(r, PotOfGoldTrigger.TriggerInstance::new));

    boolean matches() {
      return true;
    }
  }
}
