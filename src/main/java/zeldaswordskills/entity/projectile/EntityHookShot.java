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

package zeldaswordskills.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceStunIndirect;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.HookshotType;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemHookShot;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.util.TargetUtils;

/**
 * 
 * The hookshot entity should travel up to 16 blocks, locking on to any impacted block if the
 * material is appropriate for the shot type.
 * 
 * The entity returned from getThrower() is the entity that will travel to the location struck
 * so long as it is holding and using an ItemHookShot of the appropriate type.
 *
 */
public class EntityHookShot extends EntityThrowable
{
	/** Watchable object index for thrower entity's id */
	protected static final int THROWER_DATA_WATCHER_INDEX = 22;

	/** Watchable object index for target entity's id */
	protected static final int TARGET_DATA_WATCHER_INDEX = 23;

	/** Watchable object index for hookshot's type */
	protected static final int SHOTTYPE_DATA_WATCHER_INDEX = 24;

	/** These are set to inside the struck block to prevent player's motion from freaking out */
	protected double hitX, hitY, hitZ;

	/** Stops reeling player in when true */
	protected boolean reachedHook = false;

	public EntityHookShot(World world) {
		super(world);
	}

	public EntityHookShot(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityHookShot(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		setSize(0.25F, 0.25F);
		dataWatcher.addObject(THROWER_DATA_WATCHER_INDEX, "");
		dataWatcher.addObject(TARGET_DATA_WATCHER_INDEX, -1);
		dataWatcher.addObject(SHOTTYPE_DATA_WATCHER_INDEX, HookshotType.WOOD_SHOT.ordinal());
	}

	/**
	 * Return's this entity's hookshot Type
	 */
	public HookshotType getType() {
		return HookshotType.values()[dataWatcher.getWatchableObjectInt(SHOTTYPE_DATA_WATCHER_INDEX)];
	}

	/**
	 * Sets the shot's type; returns itself for convenience
	 */
	public EntityHookShot setType(HookshotType type) {
		dataWatcher.updateObject(SHOTTYPE_DATA_WATCHER_INDEX, type.ordinal());
		return this;
	}

	public int getMaxDistance() {
		return Config.getHookshotRange() * (getType().ordinal() % 2 == 1 ? 2 : 1);
	}

	protected Entity getTarget() {
		int id = dataWatcher.getWatchableObjectInt(TARGET_DATA_WATCHER_INDEX);
		return (id == -1 ? null : worldObj.getEntityByID(id));
	}

	protected void setTarget(Entity entity) {
		dataWatcher.updateObject(TARGET_DATA_WATCHER_INDEX, entity != null ? entity.entityId : -1);
	}

	@Override
	public EntityLivingBase getThrower() {
		String name = dataWatcher.getWatchableObjectString(THROWER_DATA_WATCHER_INDEX);
		return (name.equals("") ? null : worldObj.getPlayerEntityByName(name));
	}

	public void setThrower(EntityPlayer player) {
		dataWatcher.updateObject(THROWER_DATA_WATCHER_INDEX, player != null ? player.username : "");
	}

	/** Returns a hookshot damage source */
	protected DamageSource getDamageSource() {
		return new DamageSourceStunIndirect("hookshot", this, getThrower(), 50, 1).setCanStunPlayers().setProjectile();
	}

	/**
	 * Returns true if the block at x/y/z can be grappled by this type of hookshot
	 */
	protected boolean canGrabMaterial(int x, int y, int z) {
		return canGrabMaterial(worldObj.getBlockMaterial(x, y, z));
	}

	/**
	 * Returns true if the block at x/y/z can be grappled by this type of hookshot
	 */
	protected boolean canGrabMaterial(Material material) {
		int baseType = getType().ordinal() / 2;
		if (baseType == (HookshotType.WOOD_SHOT.ordinal() / 2)) {
			return material == Material.wood;
		} else if (baseType == (HookshotType.STONE_SHOT.ordinal() / 2)) {
			return material == Material.rock;
		} else if (baseType == (HookshotType.MULTI_SHOT.ordinal() / 2)) {
			return material == Material.wood || material == Material.rock || material == Material.ground ||
					material == Material.grass || material == Material.clay;
		} else {
			return false;
		}
	}

	/**
	 * Returns true if the hookshot can destroy the block / material type
	 */
	protected boolean canDestroyBlock(Block block, Material m, int x, int y, int z) {
		Result result = Result.DEFAULT;
		if (block instanceof IHookable) {
			result = ((IHookable) block).canDestroyBlock(getType(), worldObj, x, y, z);
		}
		switch(result) {
		case DEFAULT:
			boolean isBreakable = block.getBlockHardness(worldObj, x, y, z) >= 0.0F;
			boolean canPlayerEdit = false;
			if (getThrower() instanceof EntityPlayer) {
				canPlayerEdit = ((EntityPlayer) getThrower()).capabilities.allowEdit && Config.canHookshotBreakBlocks();
			}
			return (isBreakable && canPlayerEdit && (m == Material.glass || (m == Material.wood && getType().ordinal() / 2 == (HookshotType.STONE_SHOT.ordinal() / 2))));
		default: return result == Result.ALLOW;
		}
	}

	@Override // getVelocity
	protected float func_70182_d() {
		return 1.25F;
	}

	@Override
	protected float getGravityVelocity() {
		return 0.0F;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.typeOfHit == EnumMovingObjectType.TILE && getTarget() == null) {
			Block block = Block.blocksList[worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ)];
			if (!block.blockMaterial.blocksMovement()) {
				return;
			}
			block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
			if (!inGround && ticksExisted < getMaxDistance()) {
				motionX = motionY = motionZ = 0.0D;
				boolean flag = block instanceof IHookable;
				Material m = flag ? ((IHookable) block).getHookableMaterial(getType(), worldObj, mop.blockX, mop.blockY, mop.blockZ) : block.blockMaterial;
				if ((flag && ((IHookable) block).canAlwaysGrab(getType(), worldObj, mop.blockX, mop.blockY, mop.blockZ)) || canGrabMaterial(m)) {
					inGround = true;
					hitX = mop.blockX + 0.5D;
					hitY = mop.blockY;
					hitZ = mop.blockZ + 0.5D;
				} else if (!worldObj.isRemote) {
					if (canDestroyBlock(block, m, mop.blockX, mop.blockY, mop.blockZ)) {
						worldObj.destroyBlock(mop.blockX, mop.blockY, mop.blockZ, false);
					}
					setDead();
				}
			}

			if (!worldObj.isRemote) {
				worldObj.playSoundAtEntity(this, block.stepSound.getStepSound(), 1.0F, 1.0F);
			} else {
				for (int i = 0; i < 10; ++i) {
					worldObj.spawnParticle("tilecrack_" + block.blockID + "_" + worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ),
							posX, posY, posZ, rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
				}
			}
		} else if (mop.entityHit != null && getTarget() == null) {
			mop.entityHit.attackEntityFrom(getDamageSource(), 1.0F);
			worldObj.playSoundAtEntity(mop.entityHit, "random.wood_click", 1.0F, 1.0F);
			EntityPlayer player = (getThrower() instanceof EntityPlayer ? (EntityPlayer) getThrower() : null);
			if (player != null && player.getCurrentArmor(ArmorIndex.WORN_BOOTS) != null &&
					player.getCurrentArmor(ArmorIndex.WORN_BOOTS).getItem() == ZSSItems.bootsHeavy && player.isSneaking()) {
				setTarget(mop.entityHit);
				motionX = -motionX;
				motionY = -motionY;
				motionZ = -motionZ;
			} else {
				setDead();
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (canUpdate()) {
			if ((ticksExisted > getMaxDistance() && !inGround && getTarget() == null) || ticksExisted > (getMaxDistance() * 8)) {
				if (Config.enableHookshotMissSound()) {
					worldObj.playSoundAtEntity(getThrower() != null ? getThrower() : this, "random.wood_click", 1.0F, 1.0F);
				}
				setDead();
			} else if (getTarget() != null) {
				pullTarget();
			} else {
				pullThrower();
			}
		} else {
			setDead();
		}
	}

	@Override
	public void setDead() {
		super.setDead();
		if (getThrower() instanceof EntityPlayer) {
			((EntityPlayer) getThrower()).clearItemInUse();
		}
	}

	/**
	 * Returns true if the hookshot is allowed to update (i.e. thrower is holding correct item, etc.)
	 */
	protected boolean canUpdate() {
		EntityLivingBase thrower = getThrower();
		if (!isDead && thrower instanceof EntityPlayer && ((EntityPlayer) thrower).isUsingItem()) {
			ItemStack stack = thrower.getHeldItem(); // getItemInUse() is client-side only
			return stack.getItem() instanceof ItemHookShot && ((ItemHookShot) stack.getItem()).getType(stack.getItemDamage()) == getType();
		} else {
			return false;
		}
	}

	/**
	 * Attempts to pull the thrower towards the hookshot's position;
	 * canUpdate() should return true before this method is called
	 */
	protected void pullThrower() {
		EntityLivingBase thrower = getThrower();
		if (thrower != null && inGround) {
			thrower.fallDistance = 0.0F;
			double d = thrower.getDistanceSq(hitX, hitY, hitZ);
			if (!reachedHook) {
				reachedHook = d < 1.0D;
			}
			if (reachedHook && thrower.isSneaking()) {
				thrower.motionX = thrower.motionZ = 0.0D;
				thrower.motionY = -0.15D;
			} else if (reachedHook && d < 1.0D) {
				thrower.motionX = thrower.motionY = thrower.motionZ = 0.0D;
			} else {
				double dx = 0.15D * (hitX - thrower.posX);
				double dy = 0.15D * (hitY + (this.height / 3.0F) - thrower.posY);
				double dz = 0.15D * (hitZ - thrower.posZ);
				TargetUtils.setEntityHeading(thrower, dx, dy, dz, 1.0F, 1.0F, true);
			}
		}
	}

	/**
	 * Pulls target to player; already checked if player is wearing Heavy Boots
	 */
	protected void pullTarget() {
		Entity target = getTarget();
		EntityLivingBase thrower = getThrower();
		if (target != null && thrower != null) {
			if (target instanceof EntityLivingBase) {
				ZSSEntityInfo.get((EntityLivingBase) target).removeBuff(Buff.STUN);
			}
			double d = target.getDistanceSq(thrower.posX, thrower.posY, thrower.posZ);
			if (!reachedHook) {
				reachedHook = d < 9.0D;
			}
			if (reachedHook && d < 9.0D) {
				target.motionX = target.motionY = target.motionZ = 0.0D;
				motionX = motionY = motionZ = 0.0D;
			} else {
				double dx = 0.15D * (thrower.posX - target.posX);
				double dy = 0.15D * (thrower.posY + (this.height / 3.0F) - target.posY);
				double dz = 0.15D * (thrower.posZ - target.posZ);
				if (target instanceof EntityLivingBase) {
					double resist = 1.0D - ((EntityLivingBase) target).getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue();
					dx *= resist;
					dy *= resist;
					dz *= resist;
				}
				TargetUtils.setEntityHeading(target, dx, dy, dz, 1.0F, 1.0F, true);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setDouble("hitX", hitX);
		compound.setDouble("hitY", hitY);
		compound.setDouble("hitZ", hitZ);
		compound.setByte("reachedHook", (byte)(reachedHook ? 1 : 0));
		compound.setByte("shotType", (byte) getType().ordinal());
		compound.setInteger("shotTarget", getTarget() != null ? getTarget().entityId : -1);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		hitX = compound.getDouble("hitX");
		hitY = compound.getDouble("hitY");
		hitZ = compound.getDouble("hitZ");
		reachedHook = (compound.getByte("reachedHook") == 1);
		dataWatcher.updateObject(THROWER_DATA_WATCHER_INDEX, compound.getString("ownerName"));
		dataWatcher.updateObject(SHOTTYPE_DATA_WATCHER_INDEX, HookshotType.values()[compound.getByte("shotType") % HookshotType.values().length]);
		dataWatcher.updateObject(TARGET_DATA_WATCHER_INDEX, compound.getInteger("shotTarget"));
	}
}
