/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * 
 * A collection of methods related to target acquisition
 *
 */
public class TargetUtils
{
	/** Maximum range within which to search for targets */
	private static final int MAX_DISTANCE = 256;
	/** Max distance squared, used for comparing target distances (avoids having to call sqrt) */
	private static final double MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;

	// TODO write general MovingObjectPosition method, then have specific methods return blockHit or entityHit from that
	// TODO methods for acquiring multiple targets (beam, sphere, etc) with optional number of targets to acquire

	/**
	 * Returns the player's current reach distance based on game mode.
	 * The values were determined via actual in-game testing as the reach distances
	 * found in EntityRenderer#getMouseOver and PlayerControllerMP#getBlockReachDistance
	 * do not seem to accurately reflect the actual distance at which an attack will miss.
	 * Note that the only important distance check is handled server side in
	 * NetHandlerPlayServer#processUseEntity.
	 */
	public static double getReachDistanceSq(EntityPlayer player) {
		return player.capabilities.isCreativeMode ? 36.0D : 12.0D;
	}

	/**
	 * Returns true if current target is within the player's reach distance, used mainly
	 * for predicting misses from the client side; does not use the mouse over object.
	 */
	public static boolean canReachTarget(EntityPlayer player, Entity target) {
		return (player.canEntityBeSeen(target) && player.getDistanceSqToEntity(target) < getReachDistanceSq(player));
	}

	/**
	 * Returns MovingObjectPosition of Entity or Block impacted, or null if nothing was struck
	 * @param entity	The entity checking for impact, e.g. an arrow
	 * @param shooter	An entity not to be collided with, generally the shooter
	 * @param hitBox	The amount by which to expand the collided entities' bounding boxes when checking for impact (may be negative)
	 * @param flag		Optional flag to allow collision with shooter, e.g. (ticksInAir >= 5)
	 */
	public static MovingObjectPosition checkForImpact(World world, Entity entity, Entity shooter, double hitBox, boolean flag) {
		double posY = entity.posY + (entity.height / 2); // fix for Dash
		Vec3 vec3 = new Vec3(entity.posX, posY, entity.posZ);
		Vec3 vec31 = new Vec3(entity.posX + entity.motionX, posY + entity.motionY, entity.posZ + entity.motionZ);
		MovingObjectPosition mop = world.rayTraceBlocks(vec3, vec31, false, true, false);
		vec3 = new Vec3(entity.posX, posY, entity.posZ);
		vec31 = new Vec3(entity.posX + entity.motionX, posY + entity.motionY, entity.posZ + entity.motionZ);
		if (mop != null) {
			vec31 = new Vec3(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
		}
		Entity target = null;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox().addCoord(entity.motionX, entity.motionY, entity.motionZ).expand(1.0D, 1.0D, 1.0D));
		double d0 = 0.0D;
		for (int i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity) list.get(i);
			if (entity1.canBeCollidedWith() && (entity1 != shooter || flag)) {
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(hitBox, hitBox, hitBox);
				MovingObjectPosition mop1 = axisalignedbb.calculateIntercept(vec3, vec31);
				if (mop1 != null) {
					double d1 = vec3.distanceTo(mop1.hitVec);
					if (d1 < d0 || d0 == 0.0D) {
						target = entity1;
						d0 = d1;
					}
				}
			}
		}
		if (target != null) {
			mop = new MovingObjectPosition(target);
		}
		if (mop != null && mop.entityHit instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) mop.entityHit;
			if (player.capabilities.disableDamage || (shooter instanceof EntityPlayer
					&& !((EntityPlayer) shooter).canAttackPlayer(player)))
			{
				mop = null;
			}
		}
		return mop;
	}

	/**
	 * Returns true if the entity is directly in the crosshairs
	 */
	@SideOnly(Side.CLIENT)
	public static boolean isMouseOverEntity(Entity entity) {
		MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
		return (mop != null && mop.entityHit == entity);
	}

	/**
	 * Returns the Entity that the mouse is currently over, or null
	 */
	@SideOnly(Side.CLIENT)
	public static Entity getMouseOverEntity() {
		MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
		return (mop == null ? null : mop.entityHit);
	}

	/** Returns the EntityLivingBase closest to the point at which the seeker is looking and within the distance and radius specified */
	public static final EntityLivingBase acquireLookTarget(EntityLivingBase seeker, int distance, double radius) {
		return acquireLookTarget(seeker, distance, radius, false);
	}

	/**
	 * Calls {@link TargetUtils#acquireLookTarget(EntityLivingBase, int, double, boolean, Class)}
	 * with no specific class to prioritize.
	 */
	public static final EntityLivingBase acquireLookTarget(EntityLivingBase seeker, int distance, double radius, boolean closestToSeeker) {
		return acquireLookTarget(seeker, distance, radius, closestToSeeker, null);
	}
	/**
	 * Returns the EntityLivingBase closest to the point at which the entity is looking and within the distance and radius specified
	 * @param distance max distance to check for target, in blocks; negative value will check to MAX_DISTANCE
	 * @param radius max distance, in blocks, to search on either side of the vector's path
	 * @param closestToEntity if true, the target closest to the seeker and still within the line of sight search radius is returned
	 * @param targetClass Optional preferred entity class (or interface) to prioritize above all others
	 * @return the entity the seeker is looking at or null if no entity within sight search range
	 */
	public static final EntityLivingBase acquireLookTarget(EntityLivingBase seeker, int distance, double radius, boolean closestToSeeker, Class<?> targetClass) {
		if (distance < 0 || distance > MAX_DISTANCE) {
			distance = MAX_DISTANCE;
		}
		EntityLivingBase currentTarget = null;
		double currentDistance = MAX_DISTANCE_SQ;
		Vec3 vec3 = seeker.getLookVec();
		double targetX = seeker.posX;
		double targetY = seeker.posY + seeker.getEyeHeight() - 0.10000000149011612D;
		double targetZ = seeker.posZ;
		double distanceTraveled = 0;
		while ((int) distanceTraveled < distance) {
			targetX += vec3.xCoord;
			targetY += vec3.yCoord;
			targetZ += vec3.zCoord;
			distanceTraveled += vec3.lengthVector();
			boolean foundClass = (targetClass == null);
			AxisAlignedBB bb = new AxisAlignedBB(targetX-radius, targetY-radius, targetZ-radius, targetX+radius, targetY+radius, targetZ+radius);
			List<EntityLivingBase> list = seeker.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bb);
			for (EntityLivingBase target : list) {
				if (target != seeker && target.canBeCollidedWith() && isTargetInSight(vec3, seeker, target)) {
					double newDistance = (closestToSeeker ? target.getDistanceSqToEntity(seeker) : target.getDistanceSq(targetX, targetY, targetZ));
					boolean closer = (newDistance < currentDistance);
					if (closer || (!foundClass && targetClass != null && targetClass.isAssignableFrom(target.getClass()))) {
						currentTarget = target;
						currentDistance = newDistance;
						if (!closer) {
							foundClass = true;
						}
					}
				}
			}
		}
		return currentTarget;
	}

	/**
	 * Similar to the single entity version, but this method returns a List of all EntityLivingBase entities
	 * that are within the entity's field of vision, up to a certain range and distance away
	 */
	public static final List<EntityLivingBase> acquireAllLookTargets(EntityLivingBase seeker, int distance, double radius) {
		if (distance < 0 || distance > MAX_DISTANCE) {
			distance = MAX_DISTANCE;
		}
		List<EntityLivingBase> targets = new ArrayList<EntityLivingBase>();
		Vec3 vec3 = seeker.getLookVec();
		double targetX = seeker.posX;
		double targetY = seeker.posY + seeker.getEyeHeight() - 0.10000000149011612D;
		double targetZ = seeker.posZ;
		double distanceTraveled = 0;
		while ((int) distanceTraveled < distance) {
			targetX += vec3.xCoord;
			targetY += vec3.yCoord;
			targetZ += vec3.zCoord;
			distanceTraveled += vec3.lengthVector();
			AxisAlignedBB bb = new AxisAlignedBB(targetX-radius, targetY-radius, targetZ-radius, targetX+radius, targetY+radius, targetZ+radius);
			List<EntityLivingBase> list = seeker.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bb);
			for (EntityLivingBase target : list) {
				if (target != seeker && target.canBeCollidedWith() && isTargetInSight(vec3, seeker, target)) {
					if (!targets.contains(target)) {
						targets.add(target);
					}
				}
			}
		}
		return targets;
	}

	/**
	 * Returns whether the target is in the seeker's field of view based on relative position
	 * @param fov seeker's field of view; a wider angle returns true more often
	 */
	public static final boolean isTargetInFrontOf(Entity seeker, Entity target, float fov) {
		// thanks again to Battlegear2 for the following code snippet
		double dx = target.posX - seeker.posX;
		double dz;
		for (dz = target.posZ - seeker.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random()) * 0.01D) {
			dx = (Math.random() - Math.random()) * 0.01D;
		}
		while (seeker.rotationYaw > 360) { seeker.rotationYaw -= 360; }
		while (seeker.rotationYaw < -360) { seeker.rotationYaw += 360; }
		float yaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - seeker.rotationYaw;
		yaw = yaw - 90;
		while (yaw < -180) { yaw += 360; }
		while (yaw >= 180) { yaw -= 360; }
		return yaw < fov && yaw > -fov;
	}

	/**
	 * Returns true if the target's position is within the area that the seeker is facing and the target can be seen
	 */
	public static final boolean isTargetInSight(EntityLivingBase seeker, Entity target) {
		return isTargetInSight(seeker.getLookVec(), seeker, target);
	}

	/**
	 * Returns true if the target's position is within the area that the seeker is facing and the target can be seen
	 */
	private static final boolean isTargetInSight(Vec3 vec3, EntityLivingBase seeker, Entity target) {
		return seeker.canEntityBeSeen(target) && isTargetInFrontOf(seeker, target, 60);
	}

	/**
	 * Applies all vanilla modifiers to passed in arrow (e.g. enchantment bonuses, critical, etc)
	 * @param charge should be a value between 0.0F and 1.0F, inclusive
	 */
	public static final void applyArrowSettings(EntityArrow arrow, ItemStack bow, float charge) {
		if (charge < 0.0F) { charge = 0.0F; }
		if (charge > 1.0F) { charge = 1.0F; }

		if (charge == 1.0F) { arrow.setIsCritical(true); }

		int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);

		if (k > 0) { arrow.setDamage(arrow.getDamage() + (double) k * 0.5D + 0.5D); }

		int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);

		if (l > 0) { arrow.setKnockbackStrength(l); }

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
			arrow.setFire(100);
		}
	}

	/**
	 * Sets an entity's motion along the given vector at the given velocity, with wobble being
	 * an amount of variation applied to the course.
	 * @param wobble set to 0.0F for a true heading
	 * @param backwards if true, will set the entity's rotation to the opposite direction
	 */
	public static void setEntityHeading(Entity entity, double vecX, double vecY, double vecZ, float velocity, float wobble, boolean backwards) {
		float vectorLength = MathHelper.sqrt_double(vecX * vecX + vecY * vecY + vecZ * vecZ);
		vecX /= vectorLength;
		vecY /= vectorLength;
		vecZ /= vectorLength;
		vecX += entity.worldObj.rand.nextGaussian() * (entity.worldObj.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * wobble;
		vecY += entity.worldObj.rand.nextGaussian() * (entity.worldObj.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * wobble;
		vecZ += entity.worldObj.rand.nextGaussian() * (entity.worldObj.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * wobble;
		vecX *= velocity;
		vecY *= velocity;
		vecZ *= velocity;
		entity.motionX = vecX;
		entity.motionY = vecY;
		entity.motionZ = vecZ;
		float f = MathHelper.sqrt_double(vecX * vecX + vecZ * vecZ);
		entity.prevRotationYaw = entity.rotationYaw = (backwards ? -1 : 1) * (float)(Math.atan2(vecX, vecZ) * 180.0D / Math.PI);
		entity.prevRotationPitch = entity.rotationPitch = (backwards ? -1 : 1) * (float)(Math.atan2(vecY, f) * 180.0D / Math.PI);
	}

	/**
	 * Returns true if the entity is considered friendly to the player (or IS the player)
	 */
	public static boolean isOnTeam(EntityPlayer player, EntityLivingBase entity) {
		if (entity == player) {
			return true;
		} else if (player.isOnSameTeam(entity)) {
			return true;
		} else if (entity instanceof IEntityOwnable) {
			return ((IEntityOwnable) entity).getOwner() == player;
		} else {
			return false;
		}
	}

	/**
	 * Returns true if the entity has an unimpeded view of the sky
	 */
	public static boolean canEntitySeeSky(World world, Entity entity) {
		BlockPos pos = new BlockPos(entity);
		while (pos.getY() < world.getActualHeight()) {
			if (!world.isAirBlock(pos)) {
				return false;
			}
			pos = pos.up();
		}
		return true;
	}

	/**
	 * Whether the entity is currently standing in any liquid
	 */
	public static boolean isInLiquid(Entity entity) {
		IBlockState state = entity.worldObj.getBlockState(new BlockPos(entity));
		return state.getBlock().getMaterial().isLiquid();
	}

	/**
	 * Knocks the pushed entity back slightly as though struck by the pushing entity
	 */
	public static final void knockTargetBack(EntityLivingBase pushedEntity, EntityLivingBase pushingEntity) {
		if (pushedEntity.canBePushed()) {
			double dx = pushedEntity.posX - pushingEntity.posX;
			double dz;
			for (dz = pushedEntity.posZ - pushingEntity.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random()) * 0.01D){
				dx = (Math.random() - Math.random()) * 0.01D;
			}
			pushedEntity.knockBack(pushingEntity, 0, -dx, -dz);
		}
	}
}
