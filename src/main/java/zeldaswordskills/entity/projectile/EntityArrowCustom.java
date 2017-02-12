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

package zeldaswordskills.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.TargetUtils;

/**
 * 
 * Base custom Arrow class, operates exactly as EntityArrow except it provides an easy framework
 * from which to extend and manipulate, specifically by breaking up the onUpdate method into
 * multiple overridable steps
 *
 */
public class EntityArrowCustom extends EntityArrow implements IEntityAdditionalSpawnData
{
	/** Watchable object index for whether this arrow is a homing arrow */
	private static final int HOMING_DATAWATCHER_INDEX = 23;

	/** Watchable object index for target entity's id */
	private static final int TARGET_DATAWATCHER_INDEX = 24;

	/** Shooter's name, if shooter is a player - based on EntityThrowable's code */
	private String shooterName = null;

	// Private fields with no getters from EntityArrow; instead of repeating the fields here,
	// it would be better to use use Reflection to get/set the actual values in EntityArrow,
	// or use ASM to make them public
	protected Block inTile;
	protected int inData, xTile = -1, yTile = -1, zTile = -1;
	protected int ticksInGround = 0, ticksInAir = 0;
	protected boolean inGround = false;

	// Also private - has a setter, but no getter >.<
	private int knockbackStrength;

	/** The item to return when picked up */
	private Item arrowItem = Items.arrow;

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
		dataWatcher.addObject(TARGET_DATAWATCHER_INDEX, -1);
		dataWatcher.addObject(HOMING_DATAWATCHER_INDEX, Byte.valueOf((byte) 0));
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
	 * Returns the shootingEntity, or if null, tries to get the shooting player from the world based on shooterName, if available
	 */
	public Entity getShooter() {
		if (shootingEntity == null && shooterName != null) {
			shootingEntity = worldObj.getPlayerEntityByName(shooterName);
		}
		return shootingEntity;
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
		dataWatcher.updateObject(TARGET_DATAWATCHER_INDEX, target != null ? target.getEntityId() : -1);
	}

	/**
	 * Sets the arrow item that will be added to the player's inventory when picked up
	 */
	public EntityArrowCustom setArrowItem(Item item) {
		arrowItem = item;
		return this;
	}

	@Override
	public void onUpdate() {
		// This calls the Entity class' update method directly, circumventing EntityArrow
		super.onEntityUpdate();
		updateAngles();
		checkInGround();
		if (arrowShake > 0) {
			--arrowShake;
		}
		if (inGround) {
			updateInGround();
		} else {
			updateInAir();
		}
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
			if (canBePickedUp == 1 && !player.inventory.addItemStackToInventory(new ItemStack(arrowItem, 1, 0))) {
				flag = false;
			}

			if (flag) {
				playSound(Sounds.POP, 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				player.onItemPickup(this, 1);
				setDead();
			}
		}
	}

	/** Returns the damage source this arrow will use against the entity struck */ 
	protected DamageSource getDamageSource(Entity entity) {
		return new EntityDamageSourceIndirect("arrow", this, shootingEntity).setProjectile();
	}

	/** Returns whether this arrow can target the entity; used for Endermen */
	protected boolean canTargetEntity(Entity entity) {
		return (!(entity instanceof EntityEnderman));
	}

	/** Returns the amount of knockback the arrow applies when it hits a mob */
	public int getKnockbackStrength() {
		return knockbackStrength;
	}

	/** Sets the amount of knockback the arrow applies when it hits a mob. */
	public void setKnockbackStrength(int value) {
		knockbackStrength = value;
	}

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
				worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * (double) f3, posY - motionY * (double) f3, posZ - motionZ * (double) f3, motionX, motionY, motionZ);
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
			double d1 = target.getEntityBoundingBox().minY + (double)(target.height) - this.posY;
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
		BlockPos pos = new BlockPos(xTile, yTile, zTile);
		IBlockState state = worldObj.getBlockState(pos);
		Block block = state.getBlock();
		if (block.getMaterial() != Material.air) {
			block.setBlockBoundsBasedOnState(worldObj, pos);
			AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(worldObj, pos, state);
			if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3(posX, posY, posZ))) {
				inGround = true;
			}
		}
	}

	/**
	 * If entity is in ground, updates ticks in ground or adjusts position if block no longer in world
	 */
	protected void updateInGround() {
		BlockPos pos = new BlockPos(xTile, yTile, zTile);
		IBlockState state = worldObj.getBlockState(pos);
		Block block = state.getBlock();
		if (block == inTile && block.getMetaFromState(state) == inData) {
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
		MovingObjectPosition mop = TargetUtils.checkForImpact(worldObj, this, getShooter(), 0.3D, ticksInAir >= 5);
		if (mop != null) {
			onImpact(mop);
		}
		spawnTrailingParticles();
		updatePosition();
		doBlockCollisions();
	}

	/** Returns the arrow's velocity factor */
	protected float getVelocityFactor() {
		return 1.5F;
	}

	/** Default gravity adjustment for arrows seems to be 0.05F */
	protected float getGravityVelocity() {
		return 0.05F;
	}

	/** The name of the particle to spawn for trailing particle effects */
	protected EnumParticleTypes getParticle() {
		return EnumParticleTypes.CRIT;
	}

	/**
	 * Returns whether trailing particles should spawn (vanilla returns isCritical())
	 */
	protected boolean shouldSpawnParticles() {
		return (getIsCritical() && getParticle() != null);
	}

	/**
	 * Spawns trailing particles, if any
	 */
	protected void spawnTrailingParticles() {
		if (shouldSpawnParticles()) {
			for (int i = 0; i < 4; ++i) {
				worldObj.spawnParticle(getParticle(),
						posX + motionX * (double) i / 4.0D,
						posY + motionY * (double) i / 4.0D,
						posZ + motionZ * (double) i / 4.0D,
						-motionX, -motionY + 0.2D, -motionZ);
			}
		}
	}

	/**
	 * Called when custom arrow impacts an entity or block
	 */
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.entityHit != null) {
			onImpactEntity(mop);
		} else {
			onImpactBlock(mop);
		}
	}

	/**
	 * Called when custom arrow impacts another entity
	 */
	protected void onImpactEntity(MovingObjectPosition mop) {
		if (mop.entityHit != null) {
			// make sure shootingEntity is correct, e.g. if loaded from NBT
			shootingEntity = getShooter();
			float dmg = calculateDamage(mop.entityHit);
			if (dmg > 0) {
				if (isBurning() && canTargetEntity(mop.entityHit)) {
					mop.entityHit.setFire(5);
				}
				if (mop.entityHit.attackEntityFrom(getDamageSource(mop.entityHit), dmg)) {
					if (mop.entityHit instanceof EntityLivingBase) {
						handlePostDamageEffects((EntityLivingBase) mop.entityHit);
						if (shootingEntity instanceof EntityPlayerMP && mop.entityHit != shootingEntity && mop.entityHit instanceof EntityPlayer) {
							((EntityPlayerMP) shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
						}
					}
					playSound(Sounds.BOW_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
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
	}

	/**
	 * Called when custom arrow impacts a block
	 */
	protected void onImpactBlock(MovingObjectPosition mop) {
		BlockPos pos = mop.getBlockPos();
		xTile = pos.getX();
		yTile = pos.getY();
		zTile = pos.getZ();
		IBlockState state = worldObj.getBlockState(pos);
		inTile = state.getBlock();
		inData = inTile.getMetaFromState(state);
		motionX = (double)((float)(mop.hitVec.xCoord - posX));
		motionY = (double)((float)(mop.hitVec.yCoord - posY));
		motionZ = (double)((float)(mop.hitVec.zCoord - posZ));
		float f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
		posX -= motionX / (double) f2 * 0.05000000074505806D;
		posY -= motionY / (double) f2 * 0.05000000074505806D;
		posZ -= motionZ / (double) f2 * 0.05000000074505806D;
		playSound(Sounds.BOW_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
		inGround = true;
		arrowShake = 7;
		setIsCritical(false);
		if (inTile.getMaterial() != Material.air) {
			inTile.onEntityCollidedWithBlock(worldObj, pos, state, this);
		}
	}

	/**
	 * Returns amount of damage arrow will inflict to entity impacted
	 */
	protected float calculateDamage(Entity entityHit) {
		float velocity = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
		float dmg = (float)(velocity * getDamage());
		if (getIsCritical()) {
			dmg += rand.nextInt(MathHelper.ceiling_double_int(dmg) / 2 + 2);
		}
		return dmg;
	}

	/**
	 * Handles all secondary effects if entity hit was damaged, such as knockback, thorns, etc.
	 */
	protected void handlePostDamageEffects(EntityLivingBase entityHit) {
		if (!worldObj.isRemote) {
			entityHit.setArrowCountInEntity(entityHit.getArrowCountInEntity() + 1);
		}
		int k = getKnockbackStrength();
		if (k > 0) {
			float f3 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
			if (f3 > 0.0F) {
				double knockback = (double) k * 0.6000000238418579D / (double) f3;
				entityHit.addVelocity(motionX * knockback, 0.1D, motionZ * knockback);
			}
		}
		if (shootingEntity instanceof EntityLivingBase) {
			EnchantmentHelper.applyThornEnchantments((EntityLivingBase) entityHit, shootingEntity);
			EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) shootingEntity, entityHit);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		//super.writeEntityToNBT(compound); // writes the same data
		compound.setShort("xTile", (short) xTile);
		compound.setShort("yTile", (short) yTile);
		compound.setShort("zTile", (short) zTile);
		compound.setShort("life", (short) ticksInGround);
		// vanilla arrow uses Byte for the block; use Integer instead for modded blocks
		compound.setInteger("inTile", Block.getIdFromBlock(inTile));
		compound.setByte("inData", (byte) inData);
		compound.setByte("shake", (byte) arrowShake);
		compound.setByte("inGround", (byte)(inGround ? 1 : 0));
		compound.setByte("pickup", (byte) canBePickedUp);
		compound.setDouble("damage", getDamage());
		compound.setInteger("arrowId", Item.getIdFromItem(arrowItem));
		if ((shooterName == null || shooterName.length() == 0) && shootingEntity instanceof EntityPlayer) {
			shooterName = shootingEntity.getCommandSenderName();
		}
		compound.setString("shooter", shooterName == null ? "" : shooterName);
		compound.setInteger("target", getTarget() == null ? -1 : getTarget().getEntityId());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		//super.readEntityFromNBT(compound);
		xTile = compound.getShort("xTile");
		yTile = compound.getShort("yTile");
		zTile = compound.getShort("zTile");
		ticksInGround = compound.getShort("life");
		// vanilla arrow uses Byte for the block; use Integer instead for modded blocks
		// otherwise, could call super to have parent get correct values for private fields
		inTile = Block.getBlockById(compound.getInteger("inTile"));
		inData = compound.getByte("inData") & 255;
		arrowShake = compound.getByte("shake") & 255;
		inGround = compound.getByte("inGround") == 1;
		setDamage(compound.getDouble("damage"));
		canBePickedUp = compound.getByte("pickup");
		arrowItem = (compound.hasKey("arrowId") ? Item.getItemById(compound.getInteger("arrowId")) : Items.arrow);
		shooterName = compound.getString("shooter");
		if (shooterName != null && shooterName.length() == 0) {
			shooterName = null;
		}
		dataWatcher.updateObject(TARGET_DATAWATCHER_INDEX, compound.hasKey("target") ? compound.getInteger("target") : -1);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(shootingEntity != null ? shootingEntity.getEntityId() : -1);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		// Replicate EntityArrow's special spawn packet handling from NetHandlerPlayClient#handleSpawnObject:
		Entity shooter = worldObj.getEntityByID(buffer.readInt());
		if (shooter instanceof EntityLivingBase) { // why check for EntityLivingBase when shootingEntity can be an Entity?
			shootingEntity = (EntityLivingBase) shooter;
		}
	}
}
