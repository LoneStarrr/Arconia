package lonestarrr.arconia.client.particle;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Arconia.MOD_ID);

    public static final RegistryObject<SimpleParticleType> RAINBOW_PARTICLES = PARTICLE_TYPES.register("rainbow_particles", () -> new SimpleParticleType(true));
}
