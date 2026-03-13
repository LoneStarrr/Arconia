package lonestarrr.arconia.client.particle;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Arconia.MOD_ID);

    // rainbow with all colors
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES = PARTICLE_TYPES.register("rainbow_particles", () -> new SimpleParticleType(true));
    // individual colors
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES_RED = PARTICLE_TYPES.register("rainbow_particles_red", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES_ORANGE = PARTICLE_TYPES.register("rainbow_particles_orange", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES_YELLOW = PARTICLE_TYPES.register("rainbow_particles_yellow", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES_GREEN = PARTICLE_TYPES.register("rainbow_particles_green", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES_LIGHT_BLUE = PARTICLE_TYPES.register("rainbow_particles_light_blue", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES_BLUE = PARTICLE_TYPES.register("rainbow_particles_blue", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> RAINBOW_PARTICLES_PURPLE = PARTICLE_TYPES.register("rainbow_particles_purple", () -> new SimpleParticleType(true));
}
