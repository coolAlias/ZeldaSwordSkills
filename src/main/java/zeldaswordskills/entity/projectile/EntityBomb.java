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

package zeldaswordskills.entity.projectile;

import java.util.List;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.entity.IEntityBombEater;
import zeldaswordskills.api.entity.IEntityBombIngestible;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;

public class EntityBomb extends EntityMobThrowable implements IEntityBombIngestible
{
	/** Time until explosion */
	private int fuseTime = 24;

	/** Uses ItemBomb's radius if this value is zero */
	private float radius = 0.0F;

	/** Whether this bomb is capable of destroying blocks */
	private boolean canGrief = true;

	/** Factor by which affected entity's motion will be multiplied */
	protected float motionFactor = 1.0F;

	/** Factor by which radius of block destruction is multiplied */
	protected float destructionFactor = 1.0F;

	/** Watchable object index for bomb's type */
	private static final int BOMBTYPE_DATAWATCHER_INDEX = 22;

	public EntityBomb(World world) {
		super(world);
		setGravityVelocity(0.075F);
		setSize(0.5F, 0.5F);
	}

	public EntityBomb(World world, EntityLivingBase entity) {
		super(world, entity);
		setGravityVelocity(0.075F);
		setSize(0.5F, 0.5F);
	}

	public EntityBomb(World world, double x, double y, double z) {
		super(world, x, y, z);
		setGravityVelocity(0.075F);
		setSize(0.5F, 0.5F);
	}

	public EntityBomb(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		setGravityVelocity(0.075F);
		setSize(0.5F, 0.5F);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		setDamage(0.0F);
		dataWatcher.addObject(BOMBTYPE_DATAWATCHER_INDEX, BombType.BOMB_STANDARD.ordinal());
	}

	@Override
	public float getExplosionDamage(Entity entity) {
		return getDamage();
	}

	@Override
	public EntityBomb setExplosionDamage(float damage) {
		return (EntityBomb) setDamage(damage);
	}

	@Override
	public float getExplosionRadius(Entity entity) {
		return getRadius();
	}

	@Override
	public EntityBomb setExplosionRadius(float radius) {
		this.radius = radius;
		return this;
	}

	@Override
	public int getFuseTime(Entity entity) {
		return fuseTime;
	}

	@Override
	public EntityBomb setFuseTime(int time) {
		fuseTime = Math.max(time, 1);
		return this;
	}

	/**
	 * Adds time to bomb's fuse
	 */
	public EntityBomb addTime(int time) {
		fuseTime = Math.max(fuseTime + time, fuseTime);
		return this;
	}

	public float getRadius() {
		return radius > 0 ? radius : ItemBomb.getRadius(getType());
	}

	@Override
	public float getMotionFactor() {
		return motionFactor;
	}

	/**
	 * Sets amount by which entity's motion will be multiplied
	 */
	public EntityBomb setMotionFactor(float amount) {
		motionFactor = amount;
		return this;
	}

	@Override
	public float getDestructionFactor() {
		return destructionFactor;
	}

	/**
	 * Sets the amount by which block destruction radius will be multiplied
	 */
	public EntityBomb setDestructionFactor(float factor) {
		this.destructionFactor = factor;
		return this;
	}

	@Override
	public EntityPlayer getBombThrower() {
		EntityLivingBase thrower = this.getThrower();
		return (thrower instanceof EntityPlayer ? (EntityPlayer) thrower : null);
	}

	/**
	 * Sets this bomb to not destroy blocks, but still cause damage 
	 */
	public EntityBomb setNoGrief() {
		canGrief = false;
		return this;
	}

	@Override
	public BombType getType() {
		return BombType.values()[dataWatcher.getWatchableObjectInt(BOMBTYPE_DATAWATCHER_INDEX)];
	}

	/**
	 * Set this bomb's {@link BombType}
	 */
	public EntityBomb setType(BombType type) {
		dataWatcher.updateObject(BOMBTYPE_DATAWATCHER_INDEX, type.ordinal());
		return this;
	}

	@Override
	public boolean hasPostExplosionEffect() {
		return getType() != BombType.BOMB_FLOWER || getThrower() == null;
	}

	@Override
	protected float func_70182_d() {
		return 0.5F;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return !isDead;
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return boundingBox;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity) {
		return entity.boundingBox;
	}

	@Override
	public float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		return 0.0F; // Never called anyway since bombs only cause damage via explosions
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		// func_145771_j is pushOutOfBlocks
		noClip = func_145771_j(posX, (boundingBox.minY + boundingBox.maxY) / 2.0D, posZ);
		moveEntity(motionX, motionY, motionZ);
		float f = 0.98F;
		if (onGround) {
			f = 0.58800006F;
			Block block = worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));
			if (block.getMaterial() != Material.air) {
				f = block.slipperiness * 0.98F;
			}
		}
		motionX *= (double) f;
		motionY *= 0.9800000190734863D;
		motionZ *= (double) f;
		if (onGround) {
			motionY *= -0.5D;
		}
		if (!worldObj.isRemote && ticksExisted > 5 && wasBombEaten()) {
			setDead(); // make sure it's dead
			return;
		}
		Material material = worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)).getMaterial();
		// func_147470_e is isBoundingBoxBurning
		boolean inFire = isBurning() || (material == Material.lava || material == Material.fire) || worldObj.func_147470_e(boundingBox);
		if (isDud(inFire)) {
			fuseTime += 10;
			disarm(worldObj);
		} else if (ticksExisted % 20 == 0) {
			playSound(Sounds.BOMB_FUSE, 1.0F, 2.0F + rand.nextFloat() * 0.4F);
		}
		if (!worldObj.isRemote && shouldExplode(inFire)) {
			CustomExplosion.createExplosion(this, worldObj, posX, posY, posZ, getRadius(), getDamage(), canGrief);
			setDead();
		}
	}

	/**
	 * Returns true if any nearby {@link IEntityBombEater} consumes this bomb
	 */
	private boolean wasBombEaten() {
		List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(0.5D, 0.5D, 0.5D));
		for (EntityLivingBase entity : entities) {
			if (!entity.isEntityAlive()) {
				continue;
			}
			Result result = (entity instanceof IEntityBombEater ? ((IEntityBombEater) entity).ingestBomb(this) : Result.DENY);
			if (result == Result.ALLOW) {
				return true;
			} else if (result == Result.DEFAULT && ZSSEntityInfo.get(entity).onBombIngested(this)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If the bomb is disarmed, replaces the entity with a new EntityItem
	 */
	public boolean disarm(World world) {
		if (!world.isRemote) {
			if (isEntityAlive() && fuseTime > 4) {
				setDead();
				EntityItem bomb = new EntityItem(world, posX, posY, posZ, new ItemStack(ZSSItems.bomb,1,getType().ordinal()));
				bomb.delayBeforeCanPickup = 40;
				bomb.motionX = motionX;
				bomb.motionY = motionY;
				bomb.motionZ = motionZ;
				world.spawnEntityInWorld(bomb);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onImpact(MovingObjectPosition movingobjectposition) {
		motionX *= 0.5F;
		motionY *= -0.5F;
		motionZ *= 0.5F;
	}

	/**
	 * Returns true if the bomb is a dud: in the water or a water bomb in the nether
	 * @param inFire whether this bomb is in fire, lava, or currently burning
	 */
	private boolean isDud(boolean inFire) {
		switch(getType()) {
		case BOMB_WATER: return inFire || worldObj.provider.isHellWorld;
		default: return (worldObj.getBlock((int) posX, (int) posY, (int) posZ).getMaterial() == Material.water);
		}
	}

	/**
	 * Returns whether this bomb should explode
	 * @param inFire whether this bomb is in fire, lava, or currently burning
	 */
	private boolean shouldExplode(boolean inFire) {
		if (!isEntityAlive()) {
			return false;
		} else if ((inFire || worldObj.provider.isHellWorld) && getType() != BombType.BOMB_FIRE) {
			return true;
		}
		return ticksExisted >= fuseTime;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("bombType", (byte) getType().ordinal());
		compound.setInteger("fuseTime", fuseTime);
		compound.setFloat("bombRadius", radius);
		compound.setFloat("motionFactor", motionFactor);
		compound.setFloat("destructionFactor", destructionFactor);
		compound.setBoolean("canGrief", canGrief);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setType(BombType.values()[compound.getByte("bombType") % BombType.values().length]);
		fuseTime = compound.getInteger("fuseTime");
		radius = compound.getFloat("bombRadius");
		motionFactor = compound.getFloat("motionFactor");
		destructionFactor = compound.getFloat("destructionFactor");
		canGrief = compound.getBoolean("canGrief");
	}
}
