package lonestarrr.arconia.client.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class RainbowParticles extends TextureSheetParticle {

    protected RainbowParticles(
            ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd,
            double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        // Reduces speed if <1
        this.friction = 1f;
        // velocity
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        // size
        this.quadSize *= 1.0f;
        this.lifetime = 35;
        this.alpha = 0.7f;
        this.setSpriteFromAge(spriteSet);

        // color
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();
        fadeOut();
    }

    private void fadeOut() {
        // LERP: progress, start value, end value
        this.alpha = Mth.lerp(0.7f * (1f - (float)age/lifetime), 0f, 0.7f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(
                SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz) {
            return new RainbowParticles(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
