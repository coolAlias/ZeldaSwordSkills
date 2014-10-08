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

package zeldaswordskills.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.TargetUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	protected ChunkCoordinates currentFlightTarget;

	/** Home coordinates where this fairy spawned; will not wander too far from here */
	protected int[] home = null;

	public EntityFairy(World world) {
		super(world);
		setSize(0.5F, 0.5F);
		isImmuneToFire = true;
	}

	/**
	 * Sets the fairy's home coordinates after calling setPositionAndUpdate
	 */
	public void setFairyHome(double x, double y, double z) {
		setPositionAndUpdate(x, y, z);
		home = new int[]{(int) x, (int) y, (int) z};
	}

	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
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
	protected void fall(float par1) {}

	@Override
	protected void updateFallState(double par1, boolean par3) {}

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
	}

	@Override
	protected boolean interact(EntityPlayer player) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() == Items.glass_bottle) {
			player.triggerAchievement(ZSSAchievements.fairyCatcher);
			player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.fairyBottle));
			if (stack.stackSize > 1) {
				stack.splitStack(1);
				if (!player.inventory.addItemStackToInventory(stack)) {
					player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, stack));
				}
			}
			worldObj.playSoundAtEntity(player, Sounds.CORK, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
			setDead();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		motionY *= 0.6000000238418579D;
		if (!worldObj.isRemote) {
			if (worldObj.provider.dimensionId == -1 && ticksExisted > 60) {
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

		if (currentFlightTarget != null && (!worldObj.isAirBlock(currentFlightTarget.posX, currentFlightTarget.posY, currentFlightTarget.posZ) || currentFlightTarget.posY < 1)) {
			currentFlightTarget = null;
		}

		if (home != null && (posY < home[1] || getDistanceSq(home[0], home[1], home[2]) > 16D)) {
			currentFlightTarget = new ChunkCoordinates(home[0], home[1], home[2]);
		} else if (currentFlightTarget == null || rand.nextInt(30) == 0 || currentFlightTarget.getDistanceSquared((int) posX, (int) posY, (int) posZ) < 4.0F) {
			currentFlightTarget = new ChunkCoordinates((int) posX + rand.nextInt(7) - rand.nextInt(7), (int) posY + rand.nextInt(6) - 2, (int) posZ + rand.nextInt(7) - rand.nextInt(7));
		}

		double d0 = (double) currentFlightTarget.posX + 0.5D - posX;
		double d1 = (double) currentFlightTarget.posY + 0.1D - posY;
		double d2 = (double) currentFlightTarget.posZ + 0.5D - posZ;
		motionX += (Math.signum(d0) * 0.5D - motionX) * 0.10000000149011612D;
		motionY += (Math.signum(d1) * 0.699999988079071D - motionY) * 0.10000000149011612D;
		motionZ += (Math.signum(d2) * 0.5D - motionZ) * 0.10000000149011612D;
		float f = (float)(Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
		float f1 = MathHelper.wrapAngleTo180_float(f - rotationYaw);
		moveForward = 0.5F;
		rotationYaw += f1;
	}

	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float par1) {
		int i = super.getBrightnessForRender(par1);
		int j = (i & 255) + 120;
		int k = (i >> 16) & 255;
		if (j > 240) { j = 240; }
		return j | k << 16;
	}

	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("hasHome", home != null);
		if (home != null) {
			compound.setIntArray("FairyHome", home);
		}
	}

	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.getBoolean("hasHome")) {
			home = compound.getIntArray("FairyHome");
		}
	}
}
