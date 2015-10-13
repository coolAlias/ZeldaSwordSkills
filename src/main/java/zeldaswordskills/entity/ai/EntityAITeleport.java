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

package zeldaswordskills.entity.ai;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import zeldaswordskills.entity.player.ZSSPlayerSkills;

/**
 * 
 * AI for entities that teleport, whether randomly or upon some certain condition.
 * 
 * Also provides static teleportation methods to use for custom conditions, using
 * Forge events to allow the AI delay timer to be set even when not teleporting
 * directly through the AI, thus preventing crazy Ender-like teleportation (hopefully).
 * 
 * Uses MutexBit 8.
 *
 */
public class EntityAITeleport extends EntityAIBase
{
	/** The task owner, i.e. the teleporting entity */
	public final EntityCreature entity;

	/** Maximum teleportation range; y range is half this value */
	public final double range;

	/** Range squared */
	public final double rangeSq;

	/** Minimum delay before next teleport attempt */
	public final int minTeleportDelay;

	/** The number of ticks before next teleport attempt will be made */
	protected int teleportDelay;

	/** Incrementing timer; entity will teleport randomly when timer reaches teleportDelay */
	protected int delayTimer;

	/** Additional timer for triggered teleports, set externally */
	protected int triggerTimer;

	/** True to require entity to land upon solid ground when teleporting */
	public final boolean isGrounded;

	/** True to allow entity to teleport randomly */
	public final boolean randomTele;

	/** True to teleport towards current target if target is beyond current range */
	public final boolean approachTele;

	/** True to teleport away from attack target when distance is too close */
	public final boolean fleeTele;

	/** True to teleport away shortly after taking damage */
	public final boolean hurtTele;

	/** Optional bounding box defining limits of teleportation - entity will not teleport outside of the defined boundary */
	protected AxisAlignedBB teleBounds;

	/** True when the AI is already in the process of teleporting */
	protected boolean isTeleporting;

	/**
	 * 
	 * @param entity	The task owner, i.e. the teleporting entity
	 * @param teleRange	Maximum teleportation range; y range is half this value
	 * @param delay		The minimum time before the entity will next attempt to teleport randomly or to the current target
	 * @param grounded	True to require entity to land upon solid ground when teleporting
	 * @param random	True to allow entity to teleport randomly
	 * @param approach	True to teleport to current target when target is out of range
	 * @param flee		True to teleport away from attack target when distance is too close
	 * @param hurt		True to teleport away shortly after taking damage
	 */
	public EntityAITeleport(EntityCreature entity, double teleRange, int delay, boolean grounded, boolean random, boolean approach, boolean flee, boolean hurt) {
		this.entity = entity;
		this.range = teleRange;
		this.rangeSq = teleRange * teleRange;
		this.minTeleportDelay = delay;
		this.isGrounded = grounded;
		this.randomTele = random;
		this.approachTele = approach;
		this.fleeTele = flee;
		this.hurtTele = hurt;
		this.setMutexBits(8); // compatible with all vanilla AI tasks, but not EntityAIUseMagic
	}

	/**
	 * Returns the boundary within which the entity is allowed to teleport
	 */
	public AxisAlignedBB getTeleBounds() {
		return teleBounds;
	}

	/**
	 * Sets (or removes) the boundary within which the entity is allowed to teleport.
	 * 
	 * It is important for an entity with restricted bounds to frequently check
	 * {@link #invalidateBounds}, otherwise it is possible for the entity to be
	 * completely unable to teleport when no longer with range of its bounds.
	 * 
	 * @param newBounds null is allowed
	 */
	public void setTeleBounds(AxisAlignedBB newBounds) {
		this.teleBounds = newBounds;
	}

	/**
	 * Schedules a teleport to occur after a certain number of ticks, though
	 * the entity may teleport sooner if other conditions are met first.
	 */
	public void scheduleNextTeleport(int ticks) {
		triggerTimer = (triggerTimer > 0 ? Math.min(triggerTimer, Math.max(0, ticks)) : Math.max(0, ticks));
	}

	/**
	 * Checks if the entity is too far outside of the teleportation bounding box,
	 * in which case the bounds will be set to NULL and the entity will no longer
	 * be restricted in its teleportation (aka it is in an 'unbound' state).
	 * 
	 * Can happen if the entity uses mundane means of travel, such as walking.
	 * 
	 * When bounds are invalidated, either accept the unbound state, set up new
	 * bounds, or remove the teleportation AI task.
	 * 
	 * @param rangeSq	Distance squared at which the bounds will become invalid
	 * @return			True if the teleportation bounds are still valid or if there were no bounds to begin with
	 */
	public boolean invalidateBounds(double rangeSq) {
		if (teleBounds != null) {
			double x = teleBounds.minX + ((teleBounds.maxX - teleBounds.minX) / 2.0D);
			double y = teleBounds.minY + ((teleBounds.maxY - teleBounds.minY) / 2.0D);
			double z = teleBounds.minZ + ((teleBounds.maxZ - teleBounds.minZ) / 2.0D);
			if (entity.getDistanceSq(x, y, z) > rangeSq) {
				setTeleBounds(null);
				return false;
			}
		}
		return true;
	}

	@Override
	public void resetTask() {
		delayTimer = 0;
		triggerTimer = 0;
		isTeleporting = false;
	}

	@Override
	public boolean shouldExecute() {
		if (isTeleporting) {
			return false;
		} else if (randomTele) {
			return entity.isEntityAlive();
		}
		return (entity.getAttackTarget() != null);
	}

	@Override
	public void updateTask() {
		boolean flag = false;
		EntityLivingBase target = entity.getAttackTarget();
		++delayTimer;
		if (triggerTimer > 0 && --triggerTimer == 0) {
			flag = true;
		} else if (randomTele && delayTimer > teleportDelay) {
			flag = true;
		} else if (fleeTele && delayTimer > (teleportDelay / 2) && target != null && entity.getDistanceSqToEntity(target) < range) {
			flag = true;
		} else if (hurtTele && entity.hurtResistantTime > 10 && delayTimer > entity.hurtResistantTime) {
			flag = true; // hurt time > 10 should restrict teleports to once per time hit
		} else if (approachTele && target != null && entity.getDistanceSqToEntity(target) > rangeSq) {
			if (teleBounds == null || teleBounds.isVecInside(new Vec3(target.posX, target.posY, target.posZ))) {
				if (!entity.worldObj.isRemote) {
					isTeleporting = true;
					for (int i = 0; i < 64; ++i) {
						if (teleportToEntity(entity.worldObj, entity, target)) {
							break;
						}
					}
				}
			}
		}
		if (flag && !entity.worldObj.isRemote) {
			teleportRandomly();
		}
	}

	/**
	 * Attempts to teleport randomly until successful, up to 64 times
	 */
	public boolean teleportRandomly() {
		isTeleporting = true;
		for (int i = 0; i < 64; ++i) {
			if (teleportRandomly(entity.worldObj, entity, range, teleBounds, isGrounded)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the entity has not teleported too recently. Should be used
	 * before calling any of the static methods such as {@link #teleportRandomly}
	 */
	public boolean canTeleport() {
		return !isTeleporting && delayTimer > teleportDelay;
	}

	/**
	 * Returns whether the AI is currently in the process of teleporting
	 */
	public boolean isTeleporting() {
		return isTeleporting;
	}

	/**
	 * Sets {@link #isTeleporting} to true; do this before calling any of the static teleportation methods
	 */
	public void setTeleporting() {
		isTeleporting = true;
	}

	/**
	 * Called when the parent entity posts {@link PostEnderTeleport} event
	 */
	public void onPostTeleport(double originX, double originY, double originZ) {
		teleportDelay = minTeleportDelay + entity.worldObj.rand.nextInt(minTeleportDelay * 2) - entity.worldObj.rand.nextInt((minTeleportDelay / 2) + 1);
		delayTimer = 0;
		triggerTimer = 0;
		isTeleporting = false;
	}

	/**
	 * Teleport the enderman to a random nearby position
	 * @param range	The maximum range of the teleport; y range is half this value
	 * @return True if the entity successfully teleported
	 */
	public static boolean teleportRandomly(World world, EntityLivingBase entity, double range) {
		return teleportRandomly(world, entity, range, null, true);
	}

	/**
	 * Teleport the enderman to a random nearby position
	 * @param range	The maximum range of the teleport; y range is half this value
	 * @param restriction	Optional bounding box defining teleportation borders - entity will not teleport outside these bounds
	 * @param grounded		True to require entity to land upon solid ground when teleporting
	 * @return True if the entity successfully teleported
	 */
	public static boolean teleportRandomly(World world, EntityLivingBase entity, double range, AxisAlignedBB restriction, boolean grounded) {
		int rangeY = (int) range;
		if (range < 1.0D || rangeY < 1) {
			return false;
		}
		double x = entity.posX + (world.rand.nextDouble() - 0.5D) * range;
		double y = entity.posY + (double)(world.rand.nextInt(rangeY) - (rangeY / 2));
		double z = entity.posZ + (world.rand.nextDouble() - 0.5D) * range;
		return teleportTo(world, entity, x, y, z, restriction, grounded, true);
	}

	/**
	 * Teleport the entity to somewhere near the target entity's position with no
	 * bounding box restriction and requiring entity to land upon the ground
	 * @return True if the entity successfully teleported
	 */
	public static boolean teleportToEntity(World world, EntityLivingBase entity, Entity target) {
		return teleportToEntity(world, entity, target, null, true);
	}

	/**
	 * Teleport the entity to somewhere near the target entity's position
	 * @param restriction	Optional bounding box defining teleportation borders - entity will not teleport outside these bounds
	 * @param grounded		True to require entity to land upon solid ground when teleporting
	 * @return True if the entity successfully teleported
	 */
	public static boolean teleportToEntity(World world, EntityLivingBase entity, Entity target, AxisAlignedBB restriction, boolean grounded) {
		Vec3 vec3 = new Vec3(entity.posX - target.posX, entity.getEntityBoundingBox().minY + (double)(entity.height / 2.0F) - target.posY + (double) target.getEyeHeight(), entity.posZ - target.posZ);
		vec3 = vec3.normalize();
		double d0 = 16.0D;
		double x = entity.posX + (world.rand.nextDouble() - 0.5D) * 8.0D - vec3.xCoord * d0;
		double y = entity.posY + (double)(world.rand.nextInt(16) - 8) - vec3.yCoord * d0;
		double z = entity.posZ + (world.rand.nextDouble() - 0.5D) * 8.0D - vec3.zCoord * d0;
		return teleportTo(world, entity, x, y, z, restriction, grounded, true);
	}

	/**
	 * Teleport the entity to the position specified, adjusting y coordinate as necessary
	 * @return True if the entity successfully teleported
	 */
	public static boolean teleportTo(World world, EntityLivingBase entity, double x, double y, double z) {
		return teleportTo(world, entity, x, y, z, null, true, true);
	}

	/**
	 * Teleport the entity to the position specified, adjusting y coordinate as necessary
	 * @param restriction	Optional bounding box defining teleportation borders - entity will not teleport outside these bounds'
	 * @param grounded		True to require entity to land upon solid ground when teleporting
	 * @param noLiquid		True to prevent entity from teleporting into liquids
	 * @return True if the entity successfully teleported
	 */
	public static boolean teleportTo(World world, EntityLivingBase entity, double x, double y, double z, AxisAlignedBB restriction, boolean grounded, boolean noLiquid) {
		EnderTeleportEvent event = new EnderTeleportEvent(entity, x, y, z, 0);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			return false;
		}
		double d3 = entity.posX;
		double d4 = entity.posY;
		double d5 = entity.posZ;
		entity.posX = event.targetX;
		entity.posY = event.targetY;
		entity.posZ = event.targetZ;
		boolean flag = false;
		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY);
		int k = MathHelper.floor_double(entity.posZ);
		boolean foundSolidBlock = !grounded;
		if (grounded) {
			while (!foundSolidBlock && j > 1) {
				Block block = entity.worldObj.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
				if (block.getMaterial().blocksMovement()) {
					foundSolidBlock = true;
				} else {
					--entity.posY;
					--j;
				}
			}
		}
		if (foundSolidBlock) {
			entity.setPosition(entity.posX, entity.posY, entity.posZ);
			if (restriction != null && !restriction.isVecInside(new Vec3(entity.posX, entity.posY, entity.posZ))) {
				flag = false;
			} else if (world.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && (!noLiquid || !world.isAnyLiquid(entity.getEntityBoundingBox()))) {
				flag = true;
			}
		}
		if (!flag) {
			entity.setPosition(d3, d4, d5);
			return false;
		} else {
			if (entity instanceof EntityPlayer) {
				entity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
			}
			for (int l = 0; l < 128; ++l) {
				double d6 = (double) l / 127.0D;
				float f = (world.rand.nextFloat() - 0.5F) * 0.2F;
				float f1 = (world.rand.nextFloat() - 0.5F) * 0.2F;
				float f2 = (world.rand.nextFloat() - 0.5F) * 0.2F;
				double d7 = d3 + (entity.posX - d3) * d6 + (world.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
				double d8 = d4 + (entity.posY - d4) * d6 + world.rand.nextDouble() * (double) entity.height;
				double d9 = d5 + (entity.posZ - d5) * d6 + (world.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
				entity.worldObj.spawnParticle(EnumParticleTypes.PORTAL, d7, d8, d9, (double) f, (double) f1, (double) f2);
			}
			entity.worldObj.playSoundEffect(d3, d4, d5, "mob.endermen.portal", 1.0F, 1.0F);
			entity.playSound("mob.endermen.portal", 1.0F, 1.0F);
			MinecraftForge.EVENT_BUS.post(new PostEnderTeleport(entity, d3, d4, d5, 0));
			return true;
		}
	}

	/**
	 * Call to disengage any ILockOnTargets tracking the entity
	 */
	public static void disruptTargeting(EntityLivingBase entity) {
		if (entity.worldObj instanceof WorldServer) {
			Set<EntityPlayer> players = ((WorldServer) entity.worldObj).getEntityTracker().getTrackingPlayers(entity);
			for (EntityPlayer player : players) {
				ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
				if (skills.getTargetingSkill() != null && skills.getTargetingSkill().getCurrentTarget() == entity) {
					skills.getTargetingSkill().setCurrentTarget(player, null);
				}
			}
		}
	}

	/**
	 * Event posted after a successful ender teleport; not cancelable and changing fields has no effect
	 */
	public static class PostEnderTeleport extends EnderTeleportEvent {
		/**
		 * Post teleport event with the original position of the entity; the entity has already been set to its new position.
		 */
		public PostEnderTeleport(EntityLivingBase entity, double originX, double originY, double originZ, float damage) {
			super(entity, originX, originY, originZ, damage);
		}
	}
}
