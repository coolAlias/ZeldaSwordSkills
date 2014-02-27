package zeldaswordskills.client.particle;

import net.minecraft.world.World;

/**
 * Particle for Deku Leaf gust of wind effect.
 * @author Hunternif
 */
public class ParticleCyclone extends ModParticle {
	public ParticleCyclone(World world, double x, double y, double z, double velX, double velY, double velZ) {
		super(world, x, y, z, velX, velY, velZ);
		setTexturePositions(0, 0);
		this.particleMaxAge = 20;
		this.particleScale = 2;
		setFade(0, 0.5f);
	}
	
	@Override
	protected float scaleAtAge(float ageFraq) {
		return ageFraq < 0.5f ? 2f : (2f + 2f * (ageFraq - 0.5f));
	}
}
