/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This is an invisible particle that spawns a ring of Cyclone particles, which
 * move along an axis while rotating around it.
 * 
 * @author Hunternif
 */
@SideOnly(Side.CLIENT)
public class FXCycloneRing extends EntityFX {

	/** The factory giving access to the {@code EntityDiggingFX} constructor for this ring*/
	public static final EntityDiggingFX.Factory factory = new EntityDiggingFX.Factory();
	
	/** How fast the ring rises */
	public static final float ascendVelocity = 0.2f;
	
	/** The number of cyclone particles per ring. Digging particles are extraneous and not counted */
	public static final int puffsPerRing = 8;
	
	/** The maximum height that this effect can rise */
	public static final float maxRingHeight = 4;
	
	/** How quickly the ring rotates per update */
	public static final float baseAngleVelocity = 0.4f;

	
	/** The list of particles in this ring */
	private EntityFX[] puffs;
	
	/**
	 * 
	 * The angle of rotation of the current ring. This is determined by equally distributing the number of particles
	 * around the center of this ring (i.e. 8 particles would set the dAngle to PI/4 (45 degrees))
	 * 
	 */
	private float dAngle;

	private float yaw;
	private float pitch;
	private Vec3 axis;
	private float ringHeight;
	
	/** The current angle of the working ring. The ring rotates by the rate of the dAngle, and a new particle is created at this position on the vector*/
	private float baseAngle;

	public FXCycloneRing(World world, double x, double y, double z, double velX, double velY, double velZ,
			float yaw, float pitch, float alpha, EffectRenderer renderer) {
		super(world, x, y, z);
		// Because the EntityFX constructor with velocities screws them up:
		motionX = velX;
		motionY = velY;
		motionZ = velZ;

		axis = new Vec3(0, 1, 0);
		particleGravity = 0;
		this.yaw = yaw;
		this.pitch = pitch;
		axis = axis.rotatePitch(pitch);
		axis = axis.rotateYaw(yaw);
		ringHeight = ascendVelocity; // starts just a bit off the ground
		baseAngle = 0;
		this.particleAlpha = alpha;

		puffs = new EntityFX[puffsPerRing];
		for (int i = 0; i < puffsPerRing; i++) {
			if (Math.random() < 0.07) {
				// Add *dust* of the block below instead of the smoke puff
				int xInt = MathHelper.floor_double(x);
				int yInt = MathHelper.floor_double(y) - 1;// Minus 1 to get the block beneath this entity
				int zInt = MathHelper.floor_double(z);
				IBlockState state = world.getBlockState(new BlockPos(xInt, yInt, zInt));
				if (state.getBlock() != Blocks.air) {
					// The "y + 0.1" below is a workaround for the bug that digging
					// particles stayed on the ground and didn't fly up for some reason.
					puffs[i] = factory.getEntityFX(EnumParticleTypes.BLOCK_CRACK.getParticleID(), world, x, y + 0.1, z, velX, velY, velZ, Block.getStateId(state)).multipleParticleScaleBy(0.5f);
					renderer.addEffect(puffs[i]);
					continue;
				}
			}
			puffs[i] = new ParticleCyclone(world, x, y, z, velX, velY, velZ);
			puffs[i].setAlphaF(particleAlpha);
			renderer.addEffect(puffs[i]);
		}
		dAngle = 2 * (float)Math.PI / ((float) puffs.length);
	}

	@Override
	public void setAlphaF(float alpha) {
		super.setAlphaF(alpha);
		for (int i = 0; i < puffs.length; i++) {
			puffs[i].setAlphaF(alpha);
		}
	}

	@Override
	public void renderParticle(WorldRenderer renderer, Entity entity, float partialTick, float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.posX += motionX;
		this.posY += motionY;
		this.posZ += motionZ;
		float ringWidth = getWidthVSHeight(ringHeight);
		for (int i = 0; i < puffs.length; i++) {
			Vec3 vec = new Vec3(ringWidth, 0, 0);
			vec = vec.rotateYaw(baseAngle + ((float)i) * dAngle);
			vec = vec.rotatePitch(pitch);
			vec = vec.rotateYaw(yaw);
			puffs[i].motionX = posX + motionX + vec.xCoord - puffs[i].posX;
			puffs[i].motionY = posY + motionY + vec.yCoord - puffs[i].posY;
			puffs[i].motionZ = posZ + motionZ + vec.zCoord - puffs[i].posZ;
		}
		if (axis != null) {
			if (ringHeight < maxRingHeight) {
				posX += axis.xCoord * ascendVelocity;
				posY += axis.yCoord * ascendVelocity;
				posZ += axis.zCoord * ascendVelocity;
				ringHeight += ascendVelocity;
			} else {
				for (int i = 0; i < puffsPerRing; i++) {
					puffs[i].setDead();
				}
				setDead();
			}
		}
		baseAngle += baseAngleVelocity;
		if (baseAngle > 2 * Math.PI) {
			baseAngle -= 2 * Math.PI;
		}
	}

	public static float getWidthVSHeight(float height) {
		return height * 0.2f + 0.15f;
	}
}
