/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.TargetUtils;

/**
 * 
 * Classic Zelda fairy can be captured in bottles, heals on contact, and is immune to damage.
 * 
 * They spawn naturally in swamps, but only at night, as well as in water dungeons.
 *
 */
public class EntityFairy extends EntityAmbientCreature
{
	/**
	 * randomly selected ChunkCoordinates in a 7x6x7 box around the bat (y offset -2 to 4) towards which it will fly.
	 * upon getting close a new target will be selected
	 */
	protected BlockPos currentFlightTarget;

	/** Home coordinates where this fairy spawned; will not wander too far from here */
	protected BlockPos home = null;

	/** Fairies released from bottles into the wild set this to false so they cannot be recaptured */
	protected boolean canBeBottled = true;

	public EntityFairy(World world) {
		super(world);
		setSize(0.5F, 0.5F);
		isImmuneToFire = true;
	}

	/**
	 * Sets the fairy's home coordinates after calling setPositionAndUpdate
	 */
	public void setFairyHome(BlockPos pos) {
		setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
		home = pos;
	}

	/**
	 * Determines whether this fairy can be recaptured.
	 * Call when released from a bottle after the fairy home, if any, has been set.
	 */
	public void onReleased() {
		canBeBottled = (home != null);
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean canDespawn() {
		return home == null;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() {
		return true;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {}

	@Override
	protected void updateFallState(double par1, boolean par3, Block block, BlockPos pos) {}

	@Override
	protected float getSoundVolume() {
		return 0.1F;
	}

	@Override
	protected String getLivingSound() {
		return Sounds.FAIRY_LIVING;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(5.0D);
	}

	@Override
	protected void collideWithEntity(Entity entity) {
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).heal(1.0F);
		}
		if (entity instanceof EntityPlayer) {
			ZSSPlayerInfo.get((EntityPlayer) entity).restoreMagic(0.5F);
		}
	}

	@Override
	protected boolean interact(EntityPlayer player) {
		ItemStack stack = player.getHeldItem();
		if (canBeBottled && stack != null && stack.getItem() == Items.glass_bottle) {
			if (!worldObj.isRemote) { 
				player.triggerAchievement(ZSSAchievements.fairyCatcher);
				ItemStack fairyBottle = new ItemStack(ZSSItems.fairyBottle);
				if (hasCustomName()) {
					fairyBottle.setStackDisplayName(getCustomNameTag());
				}
				player.setCurrentItemOrArmor(0, fairyBottle);
				if (stack.stackSize > 1) {
					stack.splitStack(1);
					if (!player.inventory.addItemStackToInventory(stack)) {
						player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, stack));
					}
				}
				worldObj.playSoundAtEntity(player, Sounds.CORK, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
				setDead();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		motionY *= 0.6000000238418579D;
		if (!worldObj.isRemote && canDespawn()) {
			if (worldObj.provider.getDimensionId() == -1 && ticksExisted > 60) {
				// TODO terrible scream sound
				setDead();
			}
			if (worldObj.isDaytime() && TargetUtils.canEntitySeeSky(worldObj, this)) {
				setDead();
			}
		}
		/* if (isDead) { // reset the current block's light value
			worldObj.updateLightByType(EnumSkyBlock.Block, MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
		} else {
			illuminateBlocks(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
		} */
	}

	/**
	 * Illuminates nearby blocks
	 * Args: floor_double of current position x/y/z
	 */
	/*private void illuminateBlocks(int x, int y, int z) {
		worldObj.updateLightByType(EnumSkyBlock.Block, MathHelper.floor_double(lastTickPosX), MathHelper.floor_double(lastTickPosY), MathHelper.floor_double(lastTickPosZ));
		worldObj.setLightValue(EnumSkyBlock.Block, x, y, z, 15);
		worldObj.markBlockRangeForRenderUpdate(x, y, z, 12, 12, 12);
		//worldObj.markBlockForUpdate(x, y, z);
		/*
		worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y +1, z);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y +1, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y +1, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y +1, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y +1, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y +1, z);
        worldObj.updateLightByType(EnumSkyBlock.Block, x, y +1, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x, y +1, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x, y -1, z);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y -1, z);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y -1, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y -1, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y -1, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y -1, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y -1, z);
        worldObj.updateLightByType(EnumSkyBlock.Block, x, y -1, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x, y -1, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y, z);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x +1, y, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y, z -1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x -1, y, z);
        worldObj.updateLightByType(EnumSkyBlock.Block, x, y, z +1);
        worldObj.updateLightByType(EnumSkyBlock.Block, x, y, z -1);

	} */

	@Override
	public boolean getCanSpawnHere() {
		// TODO or nearby water, such as underground lakes
		return !worldObj.isDaytime() && TargetUtils.canEntitySeeSky(worldObj, this) && super.getCanSpawnHere();
	}

	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		if (currentFlightTarget != null && (!worldObj.isAirBlock(currentFlightTarget) || currentFlightTarget.getY() < 1)) {
			currentFlightTarget = null;
		}
		if (home != null && (posY < home.getY() || getDistanceSqToCenter(home) > 16D)) {
			currentFlightTarget = new BlockPos(home);
		} else if (currentFlightTarget == null || rand.nextInt(30) == 0 || currentFlightTarget.distanceSqToCenter((int) posX, (int) posY, (int) posZ) < 4.0F) {
			currentFlightTarget = new BlockPos((int) posX + rand.nextInt(7) - rand.nextInt(7), (int) posY + rand.nextInt(6) - 2, (int) posZ + rand.nextInt(7) - rand.nextInt(7));
		}
		double d0 = (double) currentFlightTarget.getX() + 0.5D - posX;
		double d1 = (double) currentFlightTarget.getY() + 0.1D - posY;
		double d2 = (double) currentFlightTarget.getZ() + 0.5D - posZ;
		motionX += (Math.signum(d0) * 0.5D - motionX) * 0.10000000149011612D;
		motionY += (Math.signum(d1) * 0.699999988079071D - motionY) * 0.10000000149011612D;
		motionZ += (Math.signum(d2) * 0.5D - motionZ) * 0.10000000149011612D;
		float f = (float)(Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
		float f1 = MathHelper.wrapAngleTo180_float(f - rotationYaw);
		moveForward = 0.5F;
		rotationYaw += f1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float par1) {
		int i = super.getBrightnessForRender(par1);
		int j = (i & 255) + 120;
		int k = (i >> 16) & 255;
		if (j > 240) { j = 240; }
		return j | k << 16;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("canBeBottled", canBeBottled);
		compound.setBoolean("hasHome", home != null);
		if (home != null) {
			compound.setLong("FairyHome", home.toLong());
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		canBeBottled = compound.getBoolean("canBeBottled");
		if (compound.getBoolean("hasHome")) {
			home = BlockPos.fromLong(compound.getLong("FairyHome"));
		}
	}
}
