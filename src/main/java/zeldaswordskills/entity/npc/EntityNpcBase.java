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

package zeldaswordskills.entity.npc;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import zeldaswordskills.lib.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Generic NPC class adds default villager-like AI and keeps close to a village when possible.
 * 
 * NPCs will not despawn and, depending on config settings, are invulnerable.
 *
 */
public abstract class EntityNpcBase extends EntityCreature implements INpc
{
	/** Village search timer */
	private int randomTickDivider;

	/** Nearest village object, for path-finding */
	public Village villageObj;

	public EntityNpcBase(World world) {
		super(world);
		this.setSize(0.6F, 1.8F);
		this.func_110163_bv(); // sets persistence required to true, meaning will not despawn
		this.getNavigator().setBreakDoors(true);
		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
		this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
		this.tasks.addTask(6, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(6, new EntityAIWander(this, 0.6D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
	}

	/**
	 * Returning a string here will automatically name the entity when spawned.
	 * Null or an empty string are both allowed, and will simply not apply a name.
	 */
	protected abstract String getNameTagOnSpawn();

	/**
	 * Sets the entity's custom name tag when spawned, if applicable
	 */
	private void setNameOnSpawn() {
		String name = getNameTagOnSpawn();
		if (name != null && name.length() > 0) {
			setCustomNameTag(name);
		}
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.5D);
	}

	@Override
	public boolean allowLeashing() {
		return false;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public boolean isEntityInvulnerable() {
		return Config.areNpcsInvulnerable();
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void updateAITick() {
		if (--randomTickDivider <= 0) {
			worldObj.villageCollectionObj.addVillagerPosition(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
			randomTickDivider = 70 + rand.nextInt(50);
			villageObj = worldObj.villageCollectionObj.findNearestVillage(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ), 32);
			if (villageObj == null) {
				detachHome();
			} else {
				ChunkCoordinates chunkcoordinates = villageObj.getCenter();
				setHomeArea(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, (int)(villageObj.getVillageRadius() * 0.6F));
			}
			randomUpdateTick();
		}
		super.updateAITick();
	}

	/**
	 * Update that occurs every 70-120 ticks, after the NPC has searched for a village object
	 */
	protected void randomUpdateTick() {}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte flag) {
		switch(flag) {
		case 12:
			generateRandomParticles("heart");
			break;
		case 13:
			generateRandomParticles("angryVillager");
			break;
		case 14:
			generateRandomParticles("happyVillager");
			break;
		default:
			super.handleHealthUpdate(flag);
		}
	}

	@SideOnly(Side.CLIENT)
	private void generateRandomParticles(String particle) {
		for (int i = 0; i < 5; ++i) {
			double d0 = rand.nextGaussian() * 0.02D;
			double d1 = rand.nextGaussian() * 0.02D;
			double d2 = rand.nextGaussian() * 0.02D;
			worldObj.spawnParticle(particle,
					posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width,
					posY + 1.0D + (double)(rand.nextFloat() * height),
					posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
		}
	}

	@Override
	public void setRevengeTarget(EntityLivingBase entity) {
		super.setRevengeTarget(entity);
		if (villageObj != null && entity != null) {
			villageObj.addOrRenewAgressor(entity);
			if (entity instanceof EntityPlayer) {
				int rep = (isChild() ? -3 : -1);
				villageObj.setReputationForPlayer(((EntityPlayer) entity).getCommandSenderName(), rep);
				if (isEntityAlive()) {
					worldObj.setEntityState(this, (byte) 13);
				}
			}
		}
	}

	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		setNameOnSpawn();
		return data;
	}
}
