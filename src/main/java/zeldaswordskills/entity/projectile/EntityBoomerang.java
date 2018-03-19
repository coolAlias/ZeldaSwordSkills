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

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IBoomerangBlock;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
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
		dataWatcher.updateObject(TARGET_DATAWATCHER_INDEX, target != null ? target.getEntityId() : -1);
	}

	/** Returns a boomerang damage source */
	protected DamageSource getDamageSource() {
		return new DamageSourceBaseIndirect("boomerang", this, getThrower()).setStunDamage(200, 5, true).setProjectile();
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

	public boolean isMagicBoomerang() {
		ItemStack stack = this.getBoomerang();
		return (stack != null && stack.getItem() == ZSSItems.boomerangMagic);
	}

	@Override
	public float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		return 0.0F; // can't be reflected, though it can be deflected by blocking
	}

	@Override
	public void onUpdate() {
		--distance;
		if (shouldDrop() && !worldObj.isRemote) {
			worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY, posZ, getBoomerang()));
			dropXpOrbs();
			releaseDrops(null);
			setDead();
		} else {
			if (ticksExisted % 4 == 0) {
				WorldUtils.playSoundAtEntity(this, Sounds.SWORD_MISS, 0.4F, 0.5F);
			}
			captureDrops();
			captureXpOrbs();
			if (Config.canBoomerangDenude()) {
				destroyVines();
			}
			updateMotion();
			super.onUpdate();
		}
	}

	/**
	 * Whether the boomerang should drop as an item this tick
	 */
	private boolean shouldDrop() {
		return distance < -LIFESPAN || getThrower() == null || !getThrower().isEntityAlive();
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
			// TODO make the boomerang curve
			double d0 = target.posX - this.posX;
			double d1 = target.boundingBox.minY + (double)(target.height) - this.posY;
			double d2 = target.posZ - this.posZ;
			/*
			if (distance > -20) {
			float yaw = (float)(Math.atan2(d0, d2) * 180.0D / Math.PI);
		    float pitch = (float)(Math.atan2(d1, Math.sqrt(d0 * d0 + d2 * d2)) * 180.0D / Math.PI);
		    float f = 0.4F;
		    rotationYaw += (yaw - rotationYaw) / 90.0F;
		    rotationPitch += (pitch - rotationPitch) / 90.0F;
		    motionX = (double)(-MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f);
		    motionZ = (double)(MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f);
		    motionY = (double)(-MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI) * f);
			setThrowableHeading(motionX, motionY, motionZ, getVelocity(), 0.0F);
			} else {
			double d = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
			double d3 = Math.pow(d, 3);
			double k = 0.1D;
			motionX += k / d3 * d0;
			motionY += k / d3 * d1;
			motionZ += k / d3 * d2;
			double velocity = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
			motionX *= getVelocity() / velocity;
			motionY *= getVelocity() / velocity;
			motionZ *= getVelocity() / velocity;
			}
			 */
			setThrowableHeading(d0, d1, d2, getVelocity(), 0.0F);
		}
	}

	/**
	 * Reverses the boomerang's course if not already reversed (i.e. noClip is still false)
	 */
	protected void reverseCourse() {
		if (!this.noClip) {
			this.noClip = true;
			this.distance = Math.min(this.distance, 0);
			this.setThrowableHeading(-this.motionX, -this.motionY, -this.motionZ, this.getVelocity(), 1.0F);
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
		if (mop.typeOfHit == MovingObjectType.ENTITY) {
			EntityLivingBase thrower = this.getThrower();
			if (mop.entityHit != thrower || this.wasReflected) {
				boolean flag = mop.entityHit.attackEntityFrom(this.getDamageSource(), this.getDamage());
				if (flag) {
					playSound(Sounds.DAMAGE_SUCCESSFUL_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
					if (mop.entityHit instanceof EntityLivingBase && thrower != null) {
						// func_151384_a is the new way Thorns is handled
						EnchantmentHelper.func_151384_a((EntityLivingBase) mop.entityHit, thrower);
						EnchantmentHelper.func_151385_b(thrower, mop.entityHit);
					}
				} else if (mop.entityHit instanceof EntityPlayer && !this.isMagicBoomerang()) {
					boolean isBlocking = PlayerUtils.isBlocking((EntityPlayer) mop.entityHit);
					boolean isShield = PlayerUtils.isShield(((EntityPlayer) mop.entityHit).getHeldItem());
					if (isBlocking && isShield) {
						this.reverseCourse();
					}
				}
			}
		} else {
			Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			boolean flag = block.getMaterial().blocksMovement();
			if (block instanceof IBoomerangBlock) {
				flag = ((IBoomerangBlock) block).onBoomerangCollided(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
			} else {
				block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
				float hardness = block.getBlockHardness(worldObj, mop.blockX, mop.blockY, mop.blockZ);
				if (Config.canBoomerangDenude() && !worldObj.isRemote && block.getMaterial() != Material.air && hardness >= 0.0F && hardness < 0.1F) {
					// func_147480_a is destroyBlock
					worldObj.func_147480_a(mop.blockX, mop.blockY, mop.blockZ, true);
				} else if (block instanceof BlockButton || (block instanceof BlockLever && this.isMagicBoomerang())) {
					WorldUtils.activateButton(worldObj, block, mop.blockX, mop.blockY, mop.blockZ);
					flag = true;
				}
			}
			if (flag) {
				this.reverseCourse();
			}
		}
	}

	/**
	 * Attempts to add the item either as the currently riding entity, or as a
	 * captured drop (in which case the item entity is set to dead)
	 * @return true if the item was captured in one form or another
	 */
	public boolean captureItem(EntityItem item) {
		if (item.isEntityAlive()) {
			if (riddenByEntity == null) {
				item.mountEntity(this);
				return true;
			} else if (captureAll && item != riddenByEntity) {
				capturedItems.add(item.getEntityItem());
				item.setDead();
				return true;
			}
		}
		return false;
	}

	/**
	 * Scans for and captures nearby EntityItems
	 */
	private void captureDrops() {
		if (riddenByEntity == null || captureAll) {
			List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(1.0D, 1.0D, 1.0D));
			for (EntityItem item : items) {
				if (captureItem(item) && !captureAll) {
					return;
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
		if (worldObj.getBlock(i, j, k).getMaterial() == Material.vine) {
			// func_147480_a is destroyBlock
			worldObj.func_147480_a(i, j, k, true);
		}
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		super.writeSpawnData(buffer);
		buffer.writeInt(this.distance);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		super.readSpawnData(buffer);
		this.distance = buffer.readInt();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("distance", distance);
		NBTTagCompound item = new NBTTagCompound();
		getBoomerang().writeToNBT(item);
		compound.setTag("item", item);
		compound.setInteger("invSlot", slot);
		compound.setInteger("target", getTarget() != null ? getTarget().getEntityId() : -1);
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
			NBTTagList items = compound.getTagList("items", compound.getId());
			for (int i = 0; i < items.tagCount(); ++i) {
				capturedItems.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) items.getCompoundTagAt(i)));
			}
		}
		xp = compound.getInteger("capturedXP");
	}
}
