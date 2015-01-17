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
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.block.IHookable.HookshotType;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemHookShot;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.SideHit;
import zeldaswordskills.util.TargetUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;

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
	protected static final int THROWER_INDEX = 22;

	/** Watchable object index for target entity's id */
	protected static final int TARGET_INDEX = 23;

	/** Watchable object index for hookshot's type */
	protected static final int SHOTTYPE_INDEX = 24;

	/** Watchable object for if the player has reached the hookshot */
	protected static final int IN_GROUND_INDEX = 25;

	/**
	 * Watchable objects for hookshot's impact position - prevents client from occasionally trying to go to 0,0,0 (due to client values not being set)
	 * Used instead of super's integer versions and set to inside the struck block to prevent the player's motion from freaking out
	 */
	public static final int HIT_POS_X = 26, HIT_POS_Y = 27, HIT_POS_Z = 28;

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
		dataWatcher.addObject(THROWER_INDEX, "");
		dataWatcher.addObject(TARGET_INDEX, -1);
		dataWatcher.addObject(SHOTTYPE_INDEX, HookshotType.WOOD_SHOT.ordinal());
		dataWatcher.addObject(IN_GROUND_INDEX, (byte) 0);
		dataWatcher.addObject(HIT_POS_X, 0.0F);
		dataWatcher.addObject(HIT_POS_Y, 0.0F);
		dataWatcher.addObject(HIT_POS_Z, 0.0F);
	}

	/**
	 * Return's this entity's hookshot Type
	 */
	public HookshotType getType() {
		return HookshotType.values()[dataWatcher.getWatchableObjectInt(SHOTTYPE_INDEX)  % HookshotType.values().length];
	}

	/**
	 * Sets the shot's type; returns itself for convenience
	 */
	public EntityHookShot setType(HookshotType type) {
		dataWatcher.updateObject(SHOTTYPE_INDEX, type.ordinal());
		return this;
	}

	public int getMaxDistance() {
		return Config.getHookshotRange() * (getType().isExtended() ? 2 : 1);
	}

	public void setThrower(EntityPlayer player) {
		dataWatcher.updateObject(THROWER_INDEX, player != null ? player.getCommandSenderName() : "");
	}

	protected Entity getTarget() {
		int id = dataWatcher.getWatchableObjectInt(TARGET_INDEX);
		return (id == -1 ? null : worldObj.getEntityByID(id));
	}

	protected void setTarget(Entity entity) {
		dataWatcher.updateObject(TARGET_INDEX, entity != null ? entity.getEntityId() : -1);
	}

	@Override
	public EntityLivingBase getThrower() {
		String name = dataWatcher.getWatchableObjectString(THROWER_INDEX);
		return (name.equals("") ? null : worldObj.getPlayerEntityByName(name));
	}

	public boolean isInGround() {
		return (dataWatcher.getWatchableObjectByte(IN_GROUND_INDEX) & 1) == 1;
	}

	protected void setInGround(boolean isInGround) {
		dataWatcher.updateObject(IN_GROUND_INDEX, isInGround ? (byte) 1 : (byte) 0);
		inGround = isInGround;
	}

	/** Returns a hookshot damage source */
	protected DamageSource getDamageSource() {
		return new DamageSourceBaseIndirect("hookshot", this, getThrower()).setStunDamage(50, 1, true).setProjectile();
	}

	/**
	 * Returns true if the block at x/y/z can be grappled by this type of hookshot
	 */
	protected boolean canGrabBlock(Block block, int x, int y, int z, int side) {
		Material material = block.getMaterial();
		Result result = Result.DEFAULT;
		if (block instanceof IHookable) {
			result = ((IHookable) block).canGrabBlock(getType(), worldObj, x, y, z, side);
			material = ((IHookable) block).getHookableMaterial(getType(), worldObj, x, y, z);
		} else if (Config.allowHookableOnly()) {
			return false;
		}
		switch(result) {
		case DEFAULT:
			switch(getType()) {
			case WOOD_SHOT:
			case WOOD_SHOT_EXT:
				return material == Material.wood;
			case CLAW_SHOT:
			case CLAW_SHOT_EXT:
				return material == Material.rock || (block instanceof BlockPane && material == Material.iron);
			case MULTI_SHOT:
			case MULTI_SHOT_EXT:
				return material == Material.wood || material == Material.rock || material == Material.ground ||
				material == Material.grass || material == Material.clay;
			}
		default: return (result == Result.ALLOW);
		}
	}

	/**
	 * Returns true if the hookshot can destroy the material type
	 */
	protected boolean canDestroyBlock(Block block, int x, int y, int z, int side) {
		Result result = Result.DEFAULT;
		if (block instanceof IHookable) {
			result = ((IHookable) block).canDestroyBlock(getType(), worldObj, x, y, z, side);
		} else if (Config.allowHookableOnly()) {
			return false;
		}
		switch(result) {
		case DEFAULT:
			boolean isBreakable = block.getBlockHardness(worldObj, x, y, z) >= 0.0F;
			boolean canPlayerEdit = false;
			if (getThrower() instanceof EntityPlayer) {
				canPlayerEdit = ((EntityPlayer) getThrower()).capabilities.allowEdit && Config.canHookshotBreakBlocks();
			}
			Material m = block.getMaterial();
			return (isBreakable && canPlayerEdit && (m == Material.glass || (m == Material.wood && getType().ordinal() / 2 == (HookshotType.CLAW_SHOT.ordinal() / 2))));
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
		if (mop.typeOfHit == MovingObjectType.BLOCK && getTarget() == null) {
			Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			if (!block.getMaterial().blocksMovement()) {
				return;
			}
			if (block.getMaterial() != Material.air) {
				block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
			}
			if (!isInGround() && ticksExisted < getMaxDistance()) {
				motionX = motionY = motionZ = 0.0D;
				if (canGrabBlock(block, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit)) {
					setInGround(true);
					// adjusting posX/Y/Z here seems to make no difference to the rendering, even when client side makes same changes
					posX = (double) mop.blockX + 0.5D;
					posY = (double) mop.blockY + 0.5D;
					posZ = (double) mop.blockZ + 0.5D;
					// side hit documentation is wrong!!! based on printing out mop.sideHit:
					// 2 = NORTH (face of block), 3 = SOUTH, 4 = WEST, 5 = EAST, 0 = BOTTOM, 1 = TOP
					switch(mop.sideHit) {
					case 5: posX += 0.5D; break; // EAST
					case 4: posX -= 0.515D; break; // WEST (a little extra to compensate for block border, otherwise renders black)
					case 3: posZ += 0.5D; break; // SOUTH
					case 2: posZ -= 0.515D; break; // NORTH (a little extra to compensate for block border, otherwise renders black)
					case SideHit.TOP: posY = mop.blockY + 1.0D; break;
					case SideHit.BOTTOM: posY = mop.blockY; break;
					}
					// however, setting position as watched values and using these on the client works... weird
					dataWatcher.updateObject(HIT_POS_X, (float) posX);
					dataWatcher.updateObject(HIT_POS_Y, (float) posY);
					dataWatcher.updateObject(HIT_POS_Z, (float) posZ);
				} else if (!worldObj.isRemote) {
					if (canDestroyBlock(block, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit)) {
						worldObj.func_147480_a(mop.blockX, mop.blockY, mop.blockZ, false);
					}
					setDead();
				}
			}

			if (!worldObj.isRemote) {
				worldObj.playSoundAtEntity(this, block.stepSound.getStepResourcePath(), 1.0F, 1.0F);
			} else {
				for (int i = 0; i < 10; ++i) {
					worldObj.spawnParticle("blockcrack_" + Block.getIdFromBlock(block) + "_" + worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ),
							posX, posY, posZ, rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
				}
			}
		} else if (mop.entityHit != null && getTarget() == null) {
			mop.entityHit.attackEntityFrom(getDamageSource(), 1.0F);
			worldObj.playSoundAtEntity(mop.entityHit, Sounds.WOOD_CLICK, 1.0F, 1.0F);
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
		// Added DataWatcher to track inGround separately from EntityThrowable, and
		// avoid the super.onUpdate if the hookshot is in the ground; not sure what
		// changed from 1.6.4, but EntityThrowable's onUpdate is no longer working
		// acceptably for the Hookshot
		if (isInGround()) {
			super.onEntityUpdate();
		} else {
			super.onUpdate();
		}
		if (canUpdate()) {
			if ((ticksExisted > getMaxDistance() && !isInGround() && getTarget() == null) || ticksExisted > (getMaxDistance() * 8)) {
				if (Config.enableHookshotMissSound()) {
					worldObj.playSoundAtEntity(getThrower() != null ? getThrower() : this, Sounds.WOOD_CLICK, 1.0F, 1.0F);
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
		if (getThrower() instanceof EntityPlayerMP && !worldObj.isRemote) {
			PacketDispatcher.sendTo(new UnpressKeyPacket(UnpressKeyPacket.RMB), (EntityPlayerMP) getThrower());
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
		if (thrower != null && isInGround()) {
			thrower.fallDistance = 0.0F;
			double d = thrower.getDistanceSq(dataWatcher.getWatchableObjectFloat(HIT_POS_X), dataWatcher.getWatchableObjectFloat(HIT_POS_Y), dataWatcher.getWatchableObjectFloat(HIT_POS_Z));

			if (!reachedHook) {
				reachedHook = d < 1.0D;
			}
			if (reachedHook && thrower.isSneaking()) {
				thrower.motionX = thrower.motionZ = 0.0D;
				thrower.motionY = -0.15D;
			} else if (reachedHook && d < 1.0D) {
				thrower.motionX = thrower.motionY = thrower.motionZ = 0.0D;
			} else {
				double dx = 0.15D * (dataWatcher.getWatchableObjectFloat(HIT_POS_X) - thrower.posX);
				double dy = 0.15D * (dataWatcher.getWatchableObjectFloat(HIT_POS_Y) + (this.height / 3.0F) - thrower.posY);
				double dz = 0.15D * (dataWatcher.getWatchableObjectFloat(HIT_POS_Z) - thrower.posZ);
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
		compound.setFloat("hitPosX", dataWatcher.getWatchableObjectFloat(HIT_POS_X));
		compound.setFloat("hitPosY", dataWatcher.getWatchableObjectFloat(HIT_POS_Y));
		compound.setFloat("hitPosZ", dataWatcher.getWatchableObjectFloat(HIT_POS_Z));
		compound.setByte("customInGround", (byte)(isInGround() ? 1 : 0));
		compound.setByte("reachedHook", (byte)(reachedHook ? 1 : 0));
		compound.setByte("shotType", (byte) getType().ordinal());
		compound.setInteger("shotTarget", getTarget() != null ? getTarget().getEntityId() : -1);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		dataWatcher.updateObject(HIT_POS_X, compound.getFloat("hitPosX"));
		dataWatcher.updateObject(HIT_POS_Y, compound.getFloat("hitPosY"));
		dataWatcher.updateObject(HIT_POS_Z, compound.getFloat("hitPosZ"));
		reachedHook = (compound.getByte("reachedHook") == 1);
		dataWatcher.updateObject(THROWER_INDEX, compound.getString("ownerName"));
		dataWatcher.updateObject(SHOTTYPE_INDEX, HookshotType.values()[compound.getByte("shotType") % HookshotType.values().length]);
		dataWatcher.updateObject(TARGET_INDEX, compound.getInteger("shotTarget"));
		dataWatcher.updateObject(IN_GROUND_INDEX, compound.getByte("customInGround"));
	}
}
