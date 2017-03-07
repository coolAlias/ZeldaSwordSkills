/**
    Copyright (C) <2015> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import zeldaswordskills.ref.ModInfo;

/**
 * Base class for particles. Provides custom options, such as fade-in/out, glow
 * and animation.
 * 
 * @author Hunternif
 */
public class ModParticle extends EntityFX {
	/** This sprite sheet must be strictly 16 by 16 icons, any resolution. */
	public static final ResourceLocation modParticles = new ResourceLocation(ModInfo.ID, "textures/particles.png");
	public static final ResourceLocation minecraftParticles = new ResourceLocation("textures/particle/particles.png");

	/** Total number of frames in the sequence. */
	protected int iconStages;
	/** Index of the first frame of the sequence. */
	protected int iconStartIndex;
	/** Index of the actual first frame, if selected at random. */ 
	private int initialIconIndex;
	/** The number of frames between initialIconIndex and the last frame index. */
	private int remainingIconStages;
	/** Whether to animate through stages. */
	private boolean animated = false;

	private float fadeInTime = 0;
	private float baseAlpha = 1;
	private float fadeOutTime = 0.8f;
	private boolean glowing = false;

	public ModParticle(World world, double x, double y, double z) {
		this(world, x, y, z, 0, 0, 0);
	}

	public ModParticle(World world, double x, double y, double z,
			double velX, double velY, double velZ) {
		super(world, x, y, z, velX, velY, velZ);
		this.motionX = velX;
		this.motionY = velY;
		this.motionZ = velZ;
	}

	@Override
	public void setAlphaF(float value) {
		setBaseAlpha(value);
	}
	protected void setBaseAlpha(float value) {
		baseAlpha = value;
		this.particleAlpha = value;
	}

	protected void setRandomScale(float min, float max) {
		particleScale = rand.nextFloat()*(max-min) + min;
	}

	protected void setRandomMaxAge(int min, int max) {
		particleMaxAge = MathHelper.floor_float(rand.nextFloat()*(float)(max-min)) + min;
	}

	protected void randomizeVelocity(double maximum) {
		this.motionX += (rand.nextDouble()*2-1) * maximum;
		this.motionY += (rand.nextDouble()*2-1) * maximum;
		this.motionZ += (rand.nextDouble()*2-1) * maximum;
	}

	protected void setTexturePositions(int x, int y) {
		setTexturePositions(x, y, 1, false);
	}
	/**
	 * When the particle's icon has multiple stages (frames), it will "play"
	 * the sequence until particle's death at a steady frame rate. Use the
	 * method setAnimated to enable animation sequence (default is false).
	 * @param x				horizontal coordinate on the texture.
	 * @param y				vertical coordinate on the texture.
	 * @param stages		total number of frames.
	 * @param randomStart	if true, the initial frame will be selected at
	 * 		random. The remaining frames will be displayed for equal durations.
	 */
	protected void setTexturePositions(int x, int y, int stages, boolean randomStart) {
		remainingIconStages = iconStages = stages;
		initialIconIndex = iconStartIndex = y * 16 + x;
		if (randomStart) {
			int firstStage = rand.nextInt(stages-1);
			initialIconIndex += firstStage;
			remainingIconStages -= firstStage;
		}
		setParticleTextureIndex(initialIconIndex);
	}

	protected void setAnimated(boolean value) {
		this.animated = value;
	}

	protected void setFade(float fadeInTime, float fadeOutTime) {
		this.fadeInTime = fadeInTime;
		this.fadeOutTime = fadeOutTime;
	}

	protected void setGlowing() {
		glowing = true;
	}

	@Override
	public void renderParticle(WorldRenderer renderer, Entity entity, float partialTick, float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {
		/*
		renderer.finishDrawing();
		Minecraft.getMinecraft().renderEngine.bindTexture(modParticles);
		renderer.startDrawingQuads();
		renderer.setBrightness(getBrightnessForRender(partialTick));
		super.renderParticle(renderer, entity, partialTick, rotX, rotXZ, rotZ, rotYZ, rotXY);
		renderer.finishDrawing();
		renderer.startDrawingQuads();
		*/
		Minecraft.getMinecraft().renderEngine.bindTexture(minecraftParticles);
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		float ageFraq = ((float)this.particleAge) / (float)this.particleMaxAge;

		this.particleAlpha = alphaAtAge(ageFraq);

		particleScale = scaleAtAge(ageFraq);

		// Animation:
		if (animated && remainingIconStages > 1) {
			int stage = MathHelper.floor_float(ageFraq * (float) remainingIconStages);
			setParticleTextureIndex(initialIconIndex + stage);
		}

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setDead();
		}

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getBrightnessForRender(float partialTick) {
		return glowing ? 0xf000f0 : super.getBrightnessForRender(partialTick);
	}

	/** Particle alpha as function of particle age (from 0 to 1). */
	protected float alphaAtAge(float ageFraq) {
		if (ageFraq <= fadeInTime) {
			return baseAlpha * ageFraq / fadeInTime;
		} else if (ageFraq >= fadeOutTime) {
			return baseAlpha * (1f - (ageFraq - fadeOutTime) / (1f-fadeOutTime) );
		} else {
			return baseAlpha;
		}
	}

	/** Particle scale as function of particle age (from 0 to 1). */
	protected float scaleAtAge(float ageFraq) {
		return particleScale;
	}

	protected void setColor(int color) {
		float r = (float)((color >> 16) & 0xff) / (float)0xff;
		float g = (float)((color >> 8) & 0xff) / (float)0xff;
		float b = (float)(color & 0xff) / (float)0xff;
		setRBGColorF(r, g, b);
	}
}
