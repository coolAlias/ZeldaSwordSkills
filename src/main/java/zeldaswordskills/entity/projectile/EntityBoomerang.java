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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentThorns;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceStunIndirect;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

public class EntityBoomerang extends EntityMobThrowable
{
	/** Watchable object index for the boomerang ItemStack */
	private static final int ITEM_DATAWATCHER_INDEX = 22;

	/** Watchable object index for target entity's id */
	private static final int TARGET_DATAWATCHER_INDEX = 23;

	/** Distance this boomerang has traveled; starts out equal to the maximum range */
	private int distance = 12;

	/** Ticks allowed before the boomerang falls to the ground if it can't reach its owner */
	private static final int LIFESPAN = 100;

	/** The original inventory slot occupied by the boomerang item */
	private int slot;

	/** Whether this boomerang can carry multiple items */
	private boolean captureAll = false;

	/** ItemStack version of captured drops is more efficient for NBT storage */
	private List<ItemStack> capturedItems = new ArrayList<ItemStack>();

	/** The amount of xp captured */
	private int xp = 0;

	public EntityBoomerang(World world) {
		super(world);
	}

	public EntityBoomerang(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityBoomerang(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityBoomerang(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		setTarget(target);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(TARGET_DATAWATCHER_INDEX, -1);
		// data type 5 is ItemStack, as seen in EntityItem's entityInit()
		dataWatcher.addObjectByDataType(ITEM_DATAWATCHER_INDEX, 5);
	}

	/** Sets the boomerang's original itemstack and inventory slot index */
	public EntityBoomerang setInvStack(ItemStack stack, int slot) {
		dataWatcher.updateObject(ITEM_DATAWATCHER_INDEX, stack);
		this.slot = slot;
		return this;
	}

	/** Returns the boomerang itemstack */
	public ItemStack getBoomerang() {
		return dataWatcher.getWatchableObjectItemStack(ITEM_DATAWATCHER_INDEX);
	}

	/** Sets the distance this boomerang can travel before it must return */
	public EntityBoomerang setRange(int range) {
		this.distance = range;
		return this;
	}

	/** Sets this boomerang to capture all dropped items */
	public EntityBoomerang setCaptureAll(boolean captureAll) {
		this.captureAll = captureAll;
		return this;
	}

	/** Returns the current target, if any */
	protected EntityLivingBase getTarget() {
		int id = dataWatcher.getWatchableObjectInt(TARGET_DATAWATCHER_INDEX);
		return (id > 0 ? (EntityLivingBase) worldObj.getEntityByID(id) : null);
	}

	/** Sets this the current target */
	public void setTarget(EntityLivingBase target) {
		dataWatcher.updateObject(TARGET_DATAWATCHER_INDEX, target != null ? target.entityId : -1);
	}

	/** Returns a boomerang damage source */
	protected DamageSource getDamageSource() {
		return new DamageSourceStunIndirect("boomerang", this, getThrower(), 200, 5).setCanStunPlayers().setProjectile();
	}

	@Override
	protected float getGravityVelocity() {
		return 0.0F;
	}

	/** Returns the boomerang's velocity */
	protected float getVelocity() {
		return func_70182_d();
	}

	/** Returns the boomerang's velocity */
	@Override
	protected float func_70182_d() {
		return 1.25F;
	}

	@Override
	public void onUpdate() {
		if (--distance < -LIFESPAN && !worldObj.isRemote) {
			worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY, posZ, getBoomerang()));
			dropXpOrbs();
			releaseDrops(null);
			setDead();
		} else {
			if (ticksExisted % 4 == 0) {
				WorldUtils.playSoundAtEntity(worldObj, this, Sounds.SWORD_MISS, 0.4F, 0.5F);
			}
			captureDrops();
			captureXpOrbs();
			destroyVines();
			updateMotion();
			super.onUpdate();
		}
	}

	/**
	 * Adjusts boomerang's motion
	 */
	protected void updateMotion() {
		if (distance < 0 && getTarget() != getThrower()) {
			setTarget(getThrower());
		}
		EntityLivingBase target = getTarget(); 
		if (target != null) {
			double d0 = target.posX - this.posX;
			double d1 = target.boundingBox.minY + (double)(target.height) - this.posY;
			double d2 = target.posZ - this.posZ;
			setThrowableHeading(d0, d1, d2, getVelocity(), 0.0F);
		}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer player) {
		if (distance < 0 && !worldObj.isRemote) {
			if (player.inventory.getStackInSlot(slot) == null) {
				player.inventory.setInventorySlotContents(slot, getBoomerang());
			} else {
				int i = player.inventory.getFirstEmptyStack();
				if (i >= 0) {
					player.inventory.setInventorySlotContents(i, player.inventory.getStackInSlot(slot));
					player.inventory.setInventorySlotContents(slot, getBoomerang());
				} else {
					PlayerUtils.addItemToInventory(player, getBoomerang());
				}
			}
			dropXpOrbs();
			releaseDrops(player);
			setDead();
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.typeOfHit == EnumMovingObjectType.ENTITY) {
			if (mop.entityHit != getThrower() && mop.entityHit.attackEntityFrom(getDamageSource(), getDamage())) {
				playSound(Sounds.DAMAGE_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
				if (mop.entityHit instanceof EntityLivingBase && getThrower() != null) {
					EnchantmentThorns.func_92096_a(getThrower(), (EntityLivingBase) mop.entityHit, rand);
				}
			}
		} else {
			int blockId = worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
			Block block = (blockId > 0 ? Block.blocksList[blockId] : null);
			if (block != null) {
				boolean flag = block.blockMaterial.blocksMovement();
				float hardness = block.getBlockHardness(worldObj, mop.blockX, mop.blockY, mop.blockZ);
				block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
				if (block.blockMaterial != Material.air && hardness >= 0.0F && hardness < 0.1F && !worldObj.isRemote) {
					worldObj.destroyBlock(mop.blockX, mop.blockY, mop.blockZ, true);
				} else if (block instanceof BlockButton || (block instanceof BlockLever &&
						getBoomerang() != null && getBoomerang().getItem() == ZSSItems.boomerangMagic)) {
					WorldUtils.activateButton(worldObj, blockId, mop.blockX, mop.blockY, mop.blockZ);
					flag = true;
				}
				if (flag && !noClip) {
					noClip = true;
					distance = Math.min(distance, 0);
					setThrowableHeading(-motionX, -motionY, -motionZ, getVelocity(), 1.0F);
				}
			}
		}
	}

	/**
	 * Scans for and captures nearby EntityItems
	 */
	private void captureDrops() {
		if (riddenByEntity == null || captureAll) {
			List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(1.0D, 1.0D, 1.0D));
			for (EntityItem item : items) {
				if (riddenByEntity == null) {
					item.mountEntity(this);
				} else if (captureAll && item != riddenByEntity) {
					ItemStack stack = item.getEntityItem();
					// check for items that aren't supposed to be picked up
					if (stack.getItem() != ZSSItems.powerPiece && stack.getItem() != ZSSItems.smallHeart) {
						capturedItems.add(item.getEntityItem());
						item.setDead();
					}
				}
			}
		}
	}

	/**
	 * Releases all captured drops either into the player's inventory or on the ground, if player is null
	 */
	private void releaseDrops(EntityPlayer player) {
		for (ItemStack stack : capturedItems) {
			if (player != null) {
				PlayerUtils.addItemToInventory(player, stack);
			} else {
				WorldUtils.spawnItemWithRandom(worldObj, stack, posX, posY, posZ);
			}
		}
	}

	/**
	 * Scans for and captures nearby XP Orbs
	 */
	private void captureXpOrbs() {
		List<EntityXPOrb> orbs = worldObj.getEntitiesWithinAABB(EntityXPOrb.class, boundingBox.expand(1.0D, 1.0D, 1.0D));
		for (EntityXPOrb orb : orbs) {
			xp += orb.getXpValue();
			worldObj.playSoundAtEntity(this, Sounds.XP_ORB, 0.1F, 0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));
			orb.setDead();
		}
	}

	/**
	 * Drops all captured xp as orbs
	 */
	private void dropXpOrbs() {
		int i = xp;
		while (i > 0) {
			int j = EntityXPOrb.getXPSplit(i);
			i -= j;
			worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, j));
			worldObj.playSoundAtEntity(this, Sounds.XP_ORB, 0.1F, 0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));
		}
	}

	/**
	 * Checks for and destroys vines each update tick
	 */
	private void destroyVines() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(posY);
		int k = MathHelper.floor_double(posZ);
		Material m = worldObj.getBlockMaterial(i, j, k);
		if (m == Material.vine) {
			worldObj.destroyBlock(i, j, k, true);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("distance", distance);
		NBTTagCompound item = new NBTTagCompound();
		getBoomerang().writeToNBT(item);
		compound.setTag("item", item);
		compound.setInteger("invSlot", slot);
		compound.setInteger("target", getTarget() != null ? getTarget().entityId : -1);
		compound.setBoolean("captureAll", captureAll);
		if (captureAll) {
			NBTTagList items = new NBTTagList();
			for (ItemStack stack : capturedItems) {
				NBTTagCompound dropNBT = new NBTTagCompound();
				stack.writeToNBT(dropNBT);
				items.appendTag(dropNBT);
			}
			compound.setTag("items", items);
		}
		compound.setInteger("capturedXP", xp);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		distance = compound.getInteger("distance");
		dataWatcher.updateObject(ITEM_DATAWATCHER_INDEX, ItemStack.loadItemStackFromNBT(compound.getCompoundTag("item")));
		slot = compound.getInteger("invSlot");
		dataWatcher.updateObject(TARGET_DATAWATCHER_INDEX, compound.hasKey("target") ? compound.getInteger("target") : -1);
		captureAll = compound.getBoolean("captureAll");
		if (captureAll) {
			NBTTagList items = compound.getTagList("items");
			for (int i = 0; i < items.tagCount(); ++i) {
				capturedItems.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) items.tagAt(i)));
			}
		}
		xp = compound.getInteger("capturedXP");
	}
}
