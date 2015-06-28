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

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.api.entity.LootableEntityRegistry;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.item.ItemWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.network.server.FallDistancePacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.Parry;
import zeldaswordskills.util.SideHit;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;

/**
 * 
 * A whip: let's the player swing from certain objects and
 * deals slight damage to entities struck.
 * 
 */
public class EntityWhip extends EntityThrowable
{
	/** Watchable object index for thrower entity's id, since EntityThrowable#thrower is both private and not synced to client */
	protected static final int THROWER_INDEX = 22;

	/** Watchable object index for whip's type */
	protected static final int WHIP_TYPE_INDEX = 24;

	/** Watchable object for if the player has reached the hookshot */
	protected static final int IN_GROUND_INDEX = 25;

	/** Watchable objects for whip's impact position, set to center of side of block hit;
	 *  used for determining swing point and also for rendering */
	public static final int HIT_POS_X = 26, HIT_POS_Y = 27, HIT_POS_Z = 28;

	/** Impact position, used for retrieving block; needed on both sides - requires data watcher? */
	private int hitX, hitY, hitZ;

	/** Number of ticks since whip has latched onto a block, since EntityThrowable#ticksInGround field is private */
	private int ticksInGround = 0;

	/** Number of ticks player has been swinging */
	private int swingTicks = 0;

	/** Swing vector heading */
	private Vec3 swingVec = null;

	/** The y-motion factor, based on distance from rotation point */
	private double dy;

	public EntityWhip(World world) {
		super(world);
	}

	public EntityWhip(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityWhip(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		setSize(0.25F, 0.25F);
		dataWatcher.addObject(THROWER_INDEX, "");
		dataWatcher.addObject(WHIP_TYPE_INDEX, WhipType.WHIP_SHORT.ordinal());
		dataWatcher.addObject(IN_GROUND_INDEX, (byte) 0);
		dataWatcher.addObject(HIT_POS_X, 0.0F);
		dataWatcher.addObject(HIT_POS_Y, 0.0F);
		dataWatcher.addObject(HIT_POS_Z, 0.0F);
	}

	/**
	 * Return's this entity's hookshot Type
	 */
	public WhipType getType() {
		return WhipType.values()[dataWatcher.getWatchableObjectInt(WHIP_TYPE_INDEX) % WhipType.values().length];
	}

	/**
	 * Sets the whip's type; returns itself for convenience
	 */
	public EntityWhip setType(WhipType type) {
		dataWatcher.updateObject(WHIP_TYPE_INDEX, type.ordinal());
		return this;
	}

	public float getMaxDistance() {
		return (float) Config.getWhipRange() * (getType().isExtended() ? 1.5F : 1.0F);
	}

	public void setThrower(EntityPlayer player) {
		dataWatcher.updateObject(THROWER_INDEX, player != null ? player.getCommandSenderName() : "");
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

	protected float getDamage() {
		switch(getType()) {
		case WHIP_SHORT: return 1.0F;
		case WHIP_LONG: return 2.0F;
		case WHIP_MAGIC: return 4.0F;
		}
		return 1.0F;
	}

	/** Returns a whip damage source */
	protected DamageSource getDamageSource() {
		return new DamageSourceBaseIndirect("whip", this, getThrower()).setStunDamage(40, 10, true);
	}

	/**
	 * Returns true if the whip can destroy the material type
	 */
	protected boolean canBreakBlock(Block block, Material m, int x, int y, int z, int side) {
		EntityLivingBase thrower = getThrower();
		if (block instanceof IWhipBlock) {
			return ((IWhipBlock) block).canBreakBlock(getType(), thrower, worldObj, x, y, z, side);
		}
		boolean isBreakable = block.getBlockHardness(worldObj, x, y, z) >= 0.0F;
		boolean canPlayerEdit = false;
		if (thrower instanceof EntityPlayer) {
			canPlayerEdit = ((EntityPlayer) thrower).capabilities.allowEdit && Config.canHookshotBreakBlocks();
		}
		// can dislodge blocks such as torches, leaves, flowers, etc.
		return (isBreakable && canPlayerEdit && (block instanceof BlockTorch || m == Material.leaves || m == Material.plants));
	}

	/**
	 * Returns true if the whip can grapple the block at x/y/z
	 */
	protected boolean canGrabBlock(Block block, int x, int y, int z, int side) {
		if (block instanceof IWhipBlock) {
			return ((IWhipBlock) block).canGrabBlock(getType(), getThrower(), worldObj, x, y, z, side);
		}
		switch (getType()) {
		case WHIP_MAGIC:
			// this excludes things like dirt, most plants, etc.
			if (block instanceof BlockSandStone || block instanceof BlockHugeMushroom || 
					(block.getMaterial().blocksMovement() && block.getBlockHardness(worldObj, x, y, z) > 1.0F)) {
				return true;
			} // otherwise, fall through to standard case:
		case WHIP_SHORT:
		case WHIP_LONG:
			int clear = 0;
			if (isSideClear(x + 1, y, z) && isSideClear(x - 1, y, z)) {
				++clear;
			}
			if (isSideClear(x, y + 1, z) && isSideClear(x, y - 1, z)) {
				++clear;
			}
			if (isSideClear(x, y, z + 1) && isSideClear(x, y, z - 1)) {
				++clear;
			}
			return (clear > 1 && (block instanceof BlockFence || block instanceof BlockLog ||
					block instanceof BlockLever || block instanceof BlockSign ||
					block instanceof BlockLadder));
		}
		return false;
	}

	/**
	 * Returns true if the coordinates given are clear of obstacles, such that the
	 * whip would be able to freely move through the space and latch on
	 */
	protected boolean isSideClear(int x, int y, int z) {
		Material m = worldObj.getBlock(x, y, z).getMaterial();
		return (!m.blocksMovement() || m == Material.leaves);
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
		if (mop.typeOfHit == MovingObjectType.BLOCK) {
			Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			if (!isInGround() && ticksExisted < getMaxDistance()) {
				WorldUtils.playSoundAtEntity(this, Sounds.WHIP_CRACK, 1.0F, 0.2F);
				motionX = motionY = motionZ = 0.0D;
				if (canGrabBlock(block, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit)) {
					setInGround(true);
					AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(worldObj, mop.blockX, mop.blockY, mop.blockZ);
					// bounding box may be null, depending on the block
					if (box != null) {
						posX = box.minX + ((box.maxX - box.minX) / 2.0D);
						posY = box.minY + ((box.maxY - box.minY) / 2.0D);
						posZ = box.minZ + ((box.maxZ - box.minZ) / 2.0D);
						switch(mop.sideHit) {
						case 5: posX = box.maxX; break; // EAST
						case 4: posX = box.minX - 0.015D; break; // WEST (a little extra to compensate for block border, otherwise renders black)
						case 3: posZ = box.maxZ; break; // SOUTH
						case 2: posZ = box.minZ - 0.015D; break; // NORTH (a little extra to compensate for block border, otherwise renders black)
						case SideHit.TOP: posY = box.maxY; break;
						case SideHit.BOTTOM: posY = box.minY - 0.015D; break;
						}
					} else {
						// adjusting posX/Y/Z here seems to make no difference to the rendering, even when client side makes same changes
						posX = (double) mop.blockX + 0.5D;
						posY = (double) mop.blockY + 0.5D;
						posZ = (double) mop.blockZ + 0.5D;
						// side hit documentation is wrong!!! based on printing out mop.sideHit:
						// 2 = NORTH (face of block), 3 = SOUTH, 4 = WEST, 5 = EAST, 0 = BOTTOM, 1 = TOP
						switch(mop.sideHit) {
						//case 5: posX += 0.5D; break; // EAST
						//case 4: posX -= 0.515D; break; // WEST (a little extra to compensate for block border, otherwise renders black)
						//case 3: posZ += 0.5D; break; // SOUTH
						//case 2: posZ -= 0.515D; break; // NORTH (a little extra to compensate for block border, otherwise renders black)
						case SideHit.TOP: posY = mop.blockY + 1.0D; break;
						case SideHit.BOTTOM: posY = mop.blockY - 0.015D; break;
						}
					}
					// however, setting position as watched values and using these on the client works... weird
					dataWatcher.updateObject(HIT_POS_X, (float) posX);
					dataWatcher.updateObject(HIT_POS_Y, (float) posY);
					dataWatcher.updateObject(HIT_POS_Z, (float) posZ);
					// unfortunately, this means the datawatcher values are no longer usable for getting the block,
					// so need to store hitX/Y/Z separately for updating
					hitX = mop.blockX;
					hitY = mop.blockY;
					hitZ = mop.blockZ;
				}  else if (canBreakBlock(block, block.getMaterial(), mop.blockX, mop.blockY, mop.blockZ, mop.sideHit)) {
					if (!worldObj.isRemote) {
						// don't drop items for players in creative mode
						boolean drop = (getThrower() instanceof EntityPlayer ? !(((EntityPlayer) getThrower()).capabilities.isCreativeMode) : true);
						worldObj.func_147480_a(mop.blockX, mop.blockY, mop.blockZ, drop);
						setDead();
					}
				} else if (block.getMaterial().blocksMovement()) {
					// Only call onEntityCollidedWithBlock if the whip didn't already grab or break the block
					block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
				} else {
					block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
					return;
				}
			}
		} else if (mop.entityHit != null) {
			worldObj.playSoundAtEntity(mop.entityHit, Sounds.WHIP_CRACK, 1.0F, 1.0F);
			boolean inflictDamage = true; // set to false if held item disarmed
			if (mop.entityHit instanceof EntityLivingBase) {
				EntityLivingBase target = (EntityLivingBase) mop.entityHit;
				if (getThrower() instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) getThrower();
					if (lootTarget(player, target)) {
						inflictDamage = (target instanceof IEntityLootable ? ((IEntityLootable) target).isHurtOnTheft(player, getType()) : Config.getHurtOnSteal());
					} else if (target.getHeldItem() != null && ZSSPlayerSkills.get(player).hasSkill(SkillBase.parry)) {
						float chance = Parry.getDisarmModifier(player, target);
						float yaw = (target.rotationYaw - player.rotationYaw);
						while (yaw >= 360.0F) { yaw -= 360.0F; }
						while (yaw < 0.0F) { yaw += 360.0F; }
						yaw = Math.abs(Math.abs(yaw) - 180.0F);
						// should be impossible to disarm from more than 90 degrees to either side
						// however, rotationYaw does not seem to be the most reliable, but it's close enough
						float mod = 0.5F - (0.25F * (yaw / 45.0F));
						chance = 0.05F + (chance * mod); // max chance is 0.3F, min is 0.05F
						if (worldObj.rand.nextFloat() < chance) {
							WorldUtils.dropHeldItem(target);
							inflictDamage = false;
						}
					}
				}
				if (inflictDamage && target.getEquipmentInSlot(ArmorIndex.EQUIPPED_CHEST) != null) {
					inflictDamage = false; // cannot damage armor-wearing opponents
				}
			}
			if (inflictDamage) {
				mop.entityHit.attackEntityFrom(getDamageSource(), getDamage());
			}
			setDead();
		}
	}

	private boolean lootTarget(EntityPlayer player, EntityLivingBase target) {
		if (target.getEntityData().getBoolean("LootableEntityFlag")) {
			return false;
		}
		IEntityLootable lootable = (target instanceof IEntityLootable ? (IEntityLootable) target : null);
		float lootChance = (lootable != null ? lootable.getLootableChance(player, getType())
				: LootableEntityRegistry.getEntityLootChance(target.getClass()));
		lootChance *= Config.getWhipLootMultiplier();
		boolean wasItemStolen = false;
		if (rand.nextFloat() < lootChance) {
			ItemStack loot = (lootable != null ? lootable.getEntityLoot(player, getType())
					: LootableEntityRegistry.getEntityLoot(target.getClass()));
			// TODO remove the following if Skulltulas are added:
			if (target instanceof EntitySpider && rand.nextInt(25) == 0) {
				loot = new ItemStack(ZSSItems.skulltulaToken);
			}
			if (loot != null) {
				EntityItem item = new EntityItem(worldObj, posX, posY + 1, posZ, loot);
				double dx = player.posX - posX;
				double dy = player.posY - posY;
				double dz = player.posZ - posZ;
				TargetUtils.setEntityHeading(item, dx, dy, dz, 1.0F, 1.0F, true);
				if (!worldObj.isRemote) {
					worldObj.spawnEntityInWorld(item);
				}
				player.triggerAchievement(ZSSAchievements.orcaThief);
				wasItemStolen = true;
			}
		}

		if (lootable == null || lootable.onLootStolen(player, wasItemStolen)) {
			if (!worldObj.isRemote) {
				target.getEntityData().setBoolean("LootableEntityFlag", true);
			}
		}
		return wasItemStolen;
	}

	@Override
	public void onUpdate() {
		// Copied from Hookshot: avoiding entityThrowable's update while in ground
		if (isInGround()) {
			lastTickPosX = posX;
			lastTickPosY = posY;
			lastTickPosZ = posZ;
			super.onEntityUpdate();
		} else {
			super.onUpdate();
		}
		if (canUpdate()) {
			// can hold onto whip for five minutes
			if (isInGround() && ticksExisted < 6000) {
				++ticksInGround;
				if (shouldSwing()) {
					swingThrower();
				}
			} else if (ticksExisted > getMaxDistance()) {
				WorldUtils.playSoundAtEntity(this, Sounds.WHIP_CRACK, 0.5F, 0.2F);
				setDead();
			}
		} else {
			setDead();
		}
	}

	/**
	 * Returns true if player should swing (player far enough below whip, whip not attached to a lever, etc.)
	 */
	private boolean shouldSwing() {
		Block block = worldObj.getBlock(hitX, hitY, hitZ);
		if (block.getMaterial() == Material.air) {
			setDead();
			return false;
		}
		if (block instanceof IWhipBlock) {
			Result result = ((IWhipBlock) block).shouldSwing(this, worldObj, hitX, hitY, hitZ, ticksInGround);
			switch (result) {
			case ALLOW: return true;
			case DENY: return false;
			default: // continue on to rest of processing
			}
		}
		if (isDead) { // in case IWhipBlock killed the whip entity
			return false;
		} else if (block instanceof BlockLever) {
			if (ticksInGround > 10 && !worldObj.isRemote) {
				WorldUtils.activateButton(worldObj, block, hitX, hitY, hitZ);
				setDead();
			}
			return false;
		} else if (worldObj.isRemote && swingVec == null && getThrower() != null) {
			// make sure thrower's y position is below impact position before starting swing
			return getThrower().posY < dataWatcher.getWatchableObjectFloat(HIT_POS_Y);
		}
		return true;
	}

	/**
	 * Use method instead of 'this.dead = true' to clear the player's item in use.
	 */
	@Override
	public void setDead() {
		super.setDead();
		if (getThrower() instanceof EntityPlayer) {
			if (getThrower() instanceof EntityPlayerMP && !worldObj.isRemote) {
				PacketDispatcher.sendTo(new UnpressKeyPacket(UnpressKeyPacket.RMB), (EntityPlayerMP) getThrower());
			}
		}
	}

	/**
	 * Returns true if the whip is allowed to update (i.e. thrower is holding correct item, etc.)
	 */
	protected boolean canUpdate() {
		EntityLivingBase thrower = getThrower();
		if (!isDead && thrower instanceof EntityPlayer && ((EntityPlayer) thrower).isUsingItem()) {
			return thrower.getHeldItem() != null && thrower.getHeldItem().getItem() instanceof ItemWhip;
		} else {
			return false;
		}
	}

	protected void swingThrower() {
		EntityLivingBase thrower = getThrower();
		if (thrower != null && !thrower.onGround && isInGround()) {
			if (thrower.worldObj.isRemote) {
				// Determine the swing variables on first swing tick:
				float x = dataWatcher.getWatchableObjectFloat(HIT_POS_X);
				float y = dataWatcher.getWatchableObjectFloat(HIT_POS_Y);
				float z = dataWatcher.getWatchableObjectFloat(HIT_POS_Z);
				if (swingTicks == 0 && swingVec == null && thrower.motionY < 0) {
					swingVec = Vec3.createVectorHelper((x - thrower.posX), y - (thrower.posY + thrower.getEyeHeight()), (z - thrower.posZ)).normalize();
					dy = (thrower.getDistance(x, y, z) / 7.0D); // lower divisor gives bigger change in y
					// calculate horizontal distance to find initial swing tick position
					// as distance approaches zero, swing ticks should approach ticks required / 2
					// as distance approaches maxDistance, swing ticks should approach zero
					// this makes sure player's arc is even around pivot point
					double d = Math.min(thrower.getDistance(x, thrower.posY, z), getMaxDistance());
					swingTicks = MathHelper.floor_double(((getMaxDistance() - d) / getMaxDistance()) * 8);
				}
				if (swingVec != null) {
					double sin = Math.sin(10.0D * swingTicks * Math.PI / 180.0D);
					double f = 0.8D; // arbitrary horizontal motion factor
					thrower.motionX = (sin * swingVec.xCoord * f);
					thrower.motionZ = (sin * swingVec.zCoord * f);
					// y motion needs to oscillate twice as quickly, so it goes up on the other side of the swing
					thrower.motionY = dy * -Math.sin(20.0D * swingTicks * Math.PI / 180.0D);
					// check for horizontal collisions that should stop swinging motion
					MovingObjectPosition mop = TargetUtils.checkForImpact(worldObj, thrower, this, -(thrower.width / 4.0F), false);
					if (mop != null && mop.typeOfHit != MovingObjectType.MISS) {
						thrower.motionX = -thrower.motionX * 0.15D;
						thrower.motionY = -thrower.motionY * 0.15D;
						thrower.motionZ = -thrower.motionZ * 0.15D;
						swingVec = null;
					}
					++swingTicks; // increment at end
					if (thrower.fallDistance > 0 && thrower.motionY < 0) {
						// 0.466885F seems to be roughly the amount added each tick while swinging; round for a little extra server-side padding
						PacketDispatcher.sendToServer(new FallDistancePacket(thrower, -0.467F));
						thrower.fallDistance -= 0.467F;
					}
				} else if (swingTicks > 0) {
					// still let player hang there after colliding, but move towards center
					if (thrower.getDistanceSq(x, thrower.posY, z) > 1.0D) {
						double dx = x - thrower.posX;
						double dz = z - thrower.posZ;
						thrower.motionX = 0.15D * dx;
						thrower.motionZ = 0.15D * dz;
					}
					if (thrower.posY < (y - (getMaxDistance() / 2.0D))) {
						thrower.motionY = 0;
					}
					++swingTicks; // increment at end
					PacketDispatcher.sendToServer(new FallDistancePacket(thrower, 0.0F));
					thrower.fallDistance = 0.0F;
				}
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setFloat("hitPosX", dataWatcher.getWatchableObjectFloat(HIT_POS_X));
		compound.setFloat("hitPosY", dataWatcher.getWatchableObjectFloat(HIT_POS_Y));
		compound.setFloat("hitPosZ", dataWatcher.getWatchableObjectFloat(HIT_POS_Z));
		compound.setInteger("hitX", hitX);
		compound.setInteger("hitY", hitY);
		compound.setInteger("hitZ", hitZ);
		compound.setByte("customInGround", (byte)(isInGround() ? 1 : 0));
		compound.setInteger("whipType", getType().ordinal());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		dataWatcher.updateObject(HIT_POS_X, compound.getFloat("hitPosX"));
		dataWatcher.updateObject(HIT_POS_Y, compound.getFloat("hitPosY"));
		dataWatcher.updateObject(HIT_POS_Z, compound.getFloat("hitPosZ"));
		hitX = compound.getInteger("hitX");
		hitY = compound.getInteger("hitY");
		hitZ = compound.getInteger("hitZ");
		// retrieving owner name saved by super-class EntityThrowable
		dataWatcher.updateObject(THROWER_INDEX, compound.getString("ownerName"));
		dataWatcher.updateObject(WHIP_TYPE_INDEX, WhipType.values()[compound.getInteger("whipType") % WhipType.values().length]);
		dataWatcher.updateObject(IN_GROUND_INDEX, compound.getByte("customInGround"));
	}
}
