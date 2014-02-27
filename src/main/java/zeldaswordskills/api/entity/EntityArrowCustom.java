/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.api.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentThorns;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet70GameEvent;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Base custom Arrow class, operates exactly as EntityArrow except it provides an easy framework
 * from which to extend and manipulate, specifically by breaking up the onUpdate method into
 * multiple overridable steps
 *
 */
public class EntityArrowCustom extends EntityArrow implements IProjectile
{
	/** Watchable object index for thrower entity's id */
	protected static final int SHOOTER_DATAWATCHER_INDEX = 22;
	
	/** Watchable object index for whether this arrow is a homing arrow */
	private static final int HOMING_DATAWATCHER_INDEX = 23;
	
	/** Watchable object index for target entity's id */
	private static final int TARGET_DATAWATCHER_INDEX = 24;
	
	/** Private fields from EntityArrow are now protected instead */
	protected int xTile = -1, yTile = -1, zTile = -1, inTile, inData;
	protected int ticksInGround = 0, ticksInAir = 0;
	protected boolean inGround = false;

	/** damage and knockback have getters and setters, so can be private */
	private double damage = 2.0D;
	private int knockbackStrength;
	
	/** The item id to return when picked up */
	private int arrowItemId = Item.arrow.itemID;

	/** Basic constructor is necessary */
	public EntityArrowCustom(World world) {
		super(world);
	}

	/** Constructs an arrow at a position, but with no heading or velocity */
	public EntityArrowCustom(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	/** Constructs an arrow with heading based on shooter and velocity, modified by the arrow's velocityFactor */
	public EntityArrowCustom(World world, EntityLivingBase shooter, float velocity) {
		super(world);
		renderDistanceWeight = 10.0D;
		shootingEntity = shooter;
		if (shooter instanceof EntityPlayer) { canBePickedUp = 1; }
		setSize(0.5F, 0.5F);
		setLocationAndAngles(shooter.posX, shooter.posY + (double) shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
		posX -= (double)(MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		posY -= 0.10000000149011612D;
		posZ -= (double)(MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		setPosition(posX, posY, posZ);
		yOffset = 0.0F;
		motionX = (double)(-MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
		motionZ = (double)(MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
		motionY = (double)(-MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI));
		setThrowableHeading(motionX, motionY, motionZ, velocity * getVelocityFactor(), 1.0F);
	}

	/**
	 * Constructs an arrow heading towards target's initial position with given velocity, but abnormal Y trajectory;
	 * @param wobble amount of deviation from base trajectory, used by Skeletons and the like; set to 0.0F for no x/z deviation
	 */
	public EntityArrowCustom(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		setTarget(target);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(SHOOTER_DATAWATCHER_INDEX, "");
		dataWatcher.addObject(TARGET_DATAWATCHER_INDEX, -1);
		dataWatcher.addObject(HOMING_DATAWATCHER_INDEX, Byte.valueOf((byte) 0));
	}
	
	/**
	 * Returns the shooter of this arrow or null if none was available
	 */
	public Entity getShooter() {
		String name = dataWatcher.getWatchableObjectString(SHOOTER_DATAWATCHER_INDEX);
		return (name.equals("") ? shootingEntity : worldObj.getPlayerEntityByName(name));
	}
	
	/**
	 * Used to update the datawatcher shooting entity object
	 */
	public EntityArrowCustom setShooter(EntityPlayer player) {
		dataWatcher.updateObject(SHOOTER_DATAWATCHER_INDEX, player != null ? player.username : "");
		return this;
	}

	/**
	 * Returns true if this arrow is a homing arrow
	 */
	protected boolean isHomingArrow() {
		return (dataWatcher.getWatchableObjectByte(HOMING_DATAWATCHER_INDEX) & 1) != 0;
	}
	
	/**
	 * Sets whether this arrow is a homing arrow or not
	 */
	public void setHomingArrow(boolean isHoming) {
		dataWatcher.updateObject(HOMING_DATAWATCHER_INDEX, Byte.valueOf((byte)(isHoming ? 1 : 0)));
	}

	/**
	 * Returns this arrow's current target, if any (for homing arrows only)
	 */
	protected EntityLivingBase getTarget() {
		int id = dataWatcher.getWatchableObjectInt(TARGET_DATAWATCHER_INDEX);
		return (id > 0 ? (EntityLivingBase) worldObj.getEntityByID(id) : null);
	}
	
	/**
	 * Sets this arrow's current target (for homing arrows only)
	 */
	public void setTarget(EntityLivingBase target) {
		dataWatcher.updateObject(TARGET_DATAWATCHER_INDEX, target != null ? target.entityId : -1);
	}
	
	/**
	 * Sets the arrow item that will be added to the player's inventory when picked up
	 */
	public EntityArrowCustom setArrowItem(int itemID) {
		arrowItemId = itemID;
		return this;
	}

	@Override
	public void onUpdate() {
		// This calls the Entity class' update method directly, circumventing EntityArrow
		super.onEntityUpdate();
		updateAngles();
		checkInGround();
		if (arrowShake > 0) { --arrowShake; }
		if (inGround) { updateInGround(); }
		else { updateInAir(); }
	}

	/**
	 * Sets the velocity to the args. Args: x, y, z
	 */
	@Override
	@SideOnly(Side.CLIENT)    
	public void setVelocity(double x, double y, double z) {
		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
			ticksInGround = 0;
		}
		super.setVelocity(x, y, z);
	}

	/**
	 * Called by a player entity when they collide with an entity
	 */
	@Override
	public void onCollideWithPlayer(EntityPlayer player) {
		if (!worldObj.isRemote && inGround && arrowShake <= 0) {
			boolean flag = canBePickedUp == 1 || canBePickedUp == 2 && player.capabilities.isCreativeMode;
			if (canBePickedUp == 1 && !player.inventory.addItemStackToInventory(new ItemStack(arrowItemId, 1, 0))) {
				flag = false;
			}

			if (flag) {
				playSound("random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				player.onItemPickup(this, 1);
				setDead();
			}
		}
	}

	/** Sets the amount of damage the arrow will inflict when it hits a mob */
	public void setDamage(double value) { damage = value; }

	/** Returns the amount of damage the arrow will inflict when it hits a mob */
	public double getDamage() { return damage; }
	
	/** Returns the damage source this arrow will use */ 
	protected DamageSource getDamageSource() {
		return new EntityDamageSourceIndirect("arrow", this, getShooter()).setProjectile();
	}
	
	/** Returns whether this arrow can target the entity; used for Endermen */
	protected boolean canTargetEntity(Entity entity) {
		return (!(entity instanceof EntityEnderman));
	}

	/** Sets the amount of knockback the arrow applies when it hits a mob. */
	public void setKnockbackStrength(int value) { knockbackStrength = value; }

	/** Returns the amount of knockback the arrow applies when it hits a mob */
	public int getKnockbackStrength() { return knockbackStrength; }

	/**
	 * Updates yaw and pitch based on current motion
	 */
	protected void updateAngles() {
		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
			prevRotationYaw = rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
			prevRotationPitch = rotationPitch = (float)(Math.atan2(motionY, (double) f) * 180.0D / Math.PI);
		}
	}

	/**
	 * Updates the arrow's position and angles
	 */
	protected void updatePosition() {
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / Math.PI);

		for (rotationPitch = (float)(Math.atan2(motionY, (double) f) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
		{ ; }

		while (rotationPitch - prevRotationPitch >= 180.0F)
		{ prevRotationPitch += 360.0F; }

		while (rotationYaw - prevRotationYaw < -180.0F)
		{ prevRotationYaw -= 360.0F; }

		while (rotationYaw - prevRotationYaw >= 180.0F)
		{ prevRotationYaw += 360.0F; }

		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
		float motionFactor = 0.99F;

		if (isInWater()) {
			for (int i = 0; i < 4; ++i) {
				float f3 = 0.25F;
				worldObj.spawnParticle("bubble", posX - motionX * (double) f3, posY - motionY * (double) f3, posZ - motionZ * (double) f3, motionX, motionY, motionZ);
			}

			motionFactor = 0.8F;
		}

		updateMotion(motionFactor, getGravityVelocity());
		setPosition(posX, posY, posZ);
	}

	/**
	 * Adjusts arrow's motion: multiplies each by factor, subtracts adjustY from motionY
	 */
	protected void updateMotion(float factor, float adjustY) {
		EntityLivingBase target = getTarget(); 
		if (isHomingArrow() && target != null) {
			double d0 = target.posX - this.posX;
			double d1 = target.boundingBox.minY + (double)(target.height) - this.posY;
			double d2 = target.posZ - this.posZ;
			setThrowableHeading(d0, d1, d2, getVelocityFactor() * 2.0F, 1.0F);
		} else {
			motionX *= (double) factor;
			motionY *= (double) factor;
			motionZ *= (double) factor;
			motionY -= (double) adjustY;
		}
	}

	/**
	 * Checks if entity is colliding with a block and if so, sets inGround to true
	 */
	protected void checkInGround() {
		int i = worldObj.getBlockId(xTile, yTile, zTile);
		if (i > 0) {
			Block.blocksList[i].setBlockBoundsBasedOnState(worldObj, xTile, yTile, zTile);
			AxisAlignedBB axisalignedbb = Block.blocksList[i].getCollisionBoundingBoxFromPool(worldObj, xTile, yTile, zTile);
			if (axisalignedbb != null && axisalignedbb.isVecInside(worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ))) {
				inGround = true;
			}
		}
	}

	/**
	 * If entity is in ground, updates ticks in ground or adjusts position if block no longer in world
	 */
	protected void updateInGround() {
		int j = worldObj.getBlockId(xTile, yTile, zTile);
		int k = worldObj.getBlockMetadata(xTile, yTile, zTile);

		if (j == inTile && k == inData) {
			++ticksInGround;
			if (ticksInGround == 1200) {
				setDead();
			}
		} else {
			inGround = false;
			motionX *= (double)(rand.nextFloat() * 0.2F);
			motionY *= (double)(rand.nextFloat() * 0.2F);
			motionZ *= (double)(rand.nextFloat() * 0.2F);
			ticksInGround = 0;
			ticksInAir = 0;
		}
	}

	/**
	 * Checks for impacts, spawns trailing particles and updates entity position
	 */
	protected void updateInAir() {
		++ticksInAir;
		MovingObjectPosition mop = checkForImpact();
		if (mop != null) { onImpact(mop); }
		spawnTrailingParticles();
		updatePosition();
		doBlockCollisions();
	}
	
	/** Returns the arrow's velocity factor */
	protected float getVelocityFactor() { return 1.5F; }
	
	/** Default gravity adjustment for arrows seems to be 0.05F */
	protected float getGravityVelocity() { return 0.05F; }
	
	/** The name of the particle to spawn for trailing particle effects */
	protected String getParticleName() { return "crit"; }
	
	/**
	 * Returns whether trailing particles should spawn (vanilla returns isCritical())
	 */
	protected boolean shouldSpawnParticles() {
		return (getIsCritical() && getParticleName().length() > 0);
	}

	/**
	 * Spawns trailing particles, if any
	 */
	protected void spawnTrailingParticles() {
		if (shouldSpawnParticles()) {
			for (int i = 0; i < 4; ++i) {
				worldObj.spawnParticle(getParticleName(), posX + motionX * (double) i / 4.0D, posY + motionY * (double) i / 4.0D,
						posZ + motionZ * (double) i / 4.0D, -motionX, -motionY + 0.2D, -motionZ);
			}
		}
	}

	/**
	 * Returns MovingObjectPosition of Entity or Block impacted, or null if nothing was struck
	 */
	protected MovingObjectPosition checkForImpact() {
		Vec3 vec3 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		Vec3 vec31 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);
		MovingObjectPosition mop = worldObj.rayTraceBlocks_do_do(vec3, vec31, false, true);
		vec3 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		vec31 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);

		if (mop != null) {
			vec31 = worldObj.getWorldVec3Pool().getVecFromPool(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
		}

		Entity entity = null;
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
		double d0 = 0.0D;
		double hitBox = 0.3D;
		shootingEntity = getShooter();

		for (int i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity) list.get(i);
			if (entity1.canBeCollidedWith() && (entity1 != shootingEntity || ticksInAir >= 5)) {
				AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(hitBox, hitBox, hitBox);
				MovingObjectPosition mop1 = axisalignedbb.calculateIntercept(vec3, vec31);
				
				if (mop1 != null) {
					double d1 = vec3.distanceTo(mop1.hitVec);
					if (d1 < d0 || d0 == 0.0D) {
						entity = entity1;
						d0 = d1;
					}
				}
			}
		}

		if (entity != null) { mop = new MovingObjectPosition(entity); }

		if (mop != null && mop.entityHit instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) mop.entityHit;
			if (player.capabilities.disableDamage || (shootingEntity instanceof EntityPlayer
					&& !((EntityPlayer) shootingEntity).canAttackPlayer(player)))
			{
				mop = null;
			}
		}

		return mop;
	}

	/**
	 * Called when custom arrow impacts an entity or block
	 */
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.entityHit != null) { onImpactEntity(mop); }
		else { onImpactBlock(mop); }
	}

	/**
	 * Called when custom arrow impacts another entity
	 */
	protected void onImpactEntity(MovingObjectPosition mop) {
		if (mop.entityHit != null) {
			int dmg = calculateDamage(mop.entityHit);
			if (isBurning() && canTargetEntity(mop.entityHit)) {
				mop.entityHit.setFire(5);
			}

			if (mop.entityHit.attackEntityFrom(getDamageSource(), (float) dmg)) {
				if (mop.entityHit instanceof EntityLivingBase) {
					handlePostDamageEffects((EntityLivingBase) mop.entityHit);

					shootingEntity = getShooter();
					if (shootingEntity instanceof EntityPlayerMP && mop.entityHit != shootingEntity && mop.entityHit instanceof EntityPlayer) {
						((EntityPlayerMP) shootingEntity).playerNetServerHandler.sendPacketToPlayer(new Packet70GameEvent(6, 0));
					}
				}

				playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
				if (canTargetEntity(mop.entityHit)) {
					setDead();
				}
			} else {
				motionX *= -0.10000000149011612D;
				motionY *= -0.10000000149011612D;
				motionZ *= -0.10000000149011612D;
				rotationYaw += 180.0F;
				prevRotationYaw += 180.0F;
				ticksInAir = 0;
			}
		}
	}

	/**
	 * Called when custom arrow impacts a block
	 */
	protected void onImpactBlock(MovingObjectPosition mop) {
		xTile = mop.blockX;
		yTile = mop.blockY;
		zTile = mop.blockZ;
		inTile = worldObj.getBlockId(xTile, yTile, zTile);
		inData = worldObj.getBlockMetadata(xTile, yTile, zTile);
		motionX = (double)((float)(mop.hitVec.xCoord - posX));
		motionY = (double)((float)(mop.hitVec.yCoord - posY));
		motionZ = (double)((float)(mop.hitVec.zCoord - posZ));
		float f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
		posX -= motionX / (double) f2 * 0.05000000074505806D;
		posY -= motionY / (double) f2 * 0.05000000074505806D;
		posZ -= motionZ / (double) f2 * 0.05000000074505806D;
		playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
		inGround = true;
		arrowShake = 7;
		setIsCritical(false);

		if (inTile != 0) {
			Block.blocksList[inTile].onEntityCollidedWithBlock(worldObj, xTile, yTile, zTile, this);
		}
	}

	/**
	 * Returns amount of damage arrow will inflict to entity impacted
	 */
	protected int calculateDamage(Entity entityHit) {
		float velocity = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
		int dmg = MathHelper.ceiling_double_int((double) velocity * damage);
		if (getIsCritical()) { dmg += rand.nextInt(dmg / 2 + 2); }
		return dmg;
	}

	/**
	 * Handles all secondary effects if entity hit was damaged, such as knockback, thorns, etc.
	 */
	protected void handlePostDamageEffects(EntityLivingBase entityHit) {
		if (!worldObj.isRemote) {
			entityHit.setArrowCountInEntity(entityHit.getArrowCountInEntity() + 1);
		}

		if (knockbackStrength > 0) {
			float f3 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);

			if (f3 > 0.0F) {
				double knockback = (double) knockbackStrength * 0.6000000238418579D / (double) f3;
				entityHit.addVelocity(motionX * knockback, 0.1D, motionZ * knockback);
			}
		}

		if (shootingEntity != null) {
			EnchantmentThorns.func_92096_a(shootingEntity, entityHit, rand);
		}
	}

	/**
	 * Super must be called in order for arrow's flight path to be correct,
	 * despite the otherwise wasteful duplication of labor 
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setShort("xTile", (short) xTile);
		compound.setShort("yTile", (short) yTile);
		compound.setShort("zTile", (short) zTile);
		compound.setByte("inTile", (byte) inTile);
		compound.setByte("inData", (byte) inData);
		compound.setByte("shake", (byte) arrowShake);
		compound.setByte("inGround", (byte)(inGround ? 1 : 0));
		compound.setByte("pickup", (byte) canBePickedUp);
		compound.setDouble("damage", damage);
		compound.setInteger("arrowId", arrowItemId);
		compound.setString("shooter", getShooter() instanceof EntityPlayer ? ((EntityPlayer) getShooter()).username : "");
		compound.setInteger("target", getTarget() != null ? getTarget().entityId : -1);
	}

	/**
	 * Super must be called in order for arrow's flight path to be correct,
	 * despite the otherwise wasteful duplication of labor 
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		xTile = compound.getShort("xTile");
		yTile = compound.getShort("yTile");
		zTile = compound.getShort("zTile");
		inTile = compound.getByte("inTile") & 255;
		inData = compound.getByte("inData") & 255;
		arrowShake = compound.getByte("shake") & 255;
		inGround = compound.getByte("inGround") == 1;
		if (compound.hasKey("damage")) { damage = compound.getDouble("damage"); }
		if (compound.hasKey("pickup")) { canBePickedUp = compound.getByte("pickup"); }
		else if (compound.hasKey("player")) { canBePickedUp = compound.getBoolean("player") ? 1 : 0; }
		arrowItemId = (compound.hasKey("arrowId") ? compound.getInteger("arrowId") : Item.arrow.itemID);
		dataWatcher.updateObject(SHOOTER_DATAWATCHER_INDEX, compound.hasKey("shooter") ? compound.getString("shooter") : "");
		dataWatcher.updateObject(TARGET_DATAWATCHER_INDEX, compound.hasKey("target") ? compound.getInteger("target") : -1);
	}
}
