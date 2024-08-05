package lonestarrr.arconia.client.particle;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Arconia.MOD_ID);

    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES = PARTICLE_TYPES.register("rainbow_particles", () -> new SimpleParticleType(true));
}
