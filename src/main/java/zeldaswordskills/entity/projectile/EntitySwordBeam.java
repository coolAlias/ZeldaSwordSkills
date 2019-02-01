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

package zeldaswordskills.entity.projectile;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.SwordBeam;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Sword beam shot from Link's sword when at full health. Inflicts a portion of
 * the original sword's base damage to the first entity struck, less 20% for each
 * additional target thus struck.
 * 
 * If using the Master Sword, the beam will shoot through enemies, hitting all
 * entities in its direct path.
 *
 */
public class EntitySwordBeam extends EntityMobThrowable
{
	/** Track entities already hit to avoid duplicates */
	private final Set<Integer> entitiesHit = Sets.newHashSet();

	/** Damage that will be inflicted on impact */
	private float damage = 4.0F;

	/** Skill level of user; affects range */
	private int level = 1;

	/** Base number of ticks this entity can exist */
	private int lifespan = 12;

	/** Whether this is the master sword version, which can strike  multiple targets */
	private boolean isMaster = false;

	public EntitySwordBeam(World world) {
		super(world);
	}

	public EntitySwordBeam(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntitySwordBeam(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public void entityInit() {
		setSize(0.5F, 0.5F);
	}

	/**
	 * Each level increases the distance the beam will travel
	 */
	public EntitySwordBeam setLevel(int level) {
		this.level = level;
		this.lifespan += level;
		return this;
	}

	/**
	 * Sets amount of damage that will be caused onImpact
	 */
	public EntitySwordBeam setDamage(float amount) {
		this.damage = amount;
		return this;
	}

	/**
	 * Sets whether this beam is capable of striking multiple targets
	 */
	public EntitySwordBeam setMasterSword(boolean isMaster) {
		this.isMaster = isMaster;
		return this;
	}

	@Override
	protected float getVelocity() {
		return 1.0F + (level * 0.15F);
	}

	@Override
	public float getGravityVelocity() {
		return 0.0F;
	}

	@Override
	public float getBrightness(float partialTick) {
		return 1.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float partialTick) {
		return 0xf000f0;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (inGround || ticksExisted > lifespan) {
			setDead();
		} else {
			// should be using this.ticksInAir, but this.ticksExisted should be close enough since ticksInAir is private
			List<MovingObjectPosition> mops = TargetUtils.checkForImpact(this.worldObj, this, this.getThrower(), 0.3D, this.ticksExisted >= 5, this.isMaster);
			if (mops != null) {
				for (MovingObjectPosition mop : mops) {
					if (this.isEntityAlive()) {
						this.onImpact(mop);
					}
				}
			}
		}
		for (int i = 0; i < 2; ++i) {
			worldObj.spawnParticle((i % 2 == 1 ? EnumParticleTypes.CRIT_MAGIC : EnumParticleTypes.CRIT), posX, posY, posZ, motionX + rand.nextGaussian(), 0.01D, motionZ + rand.nextGaussian());
			worldObj.spawnParticle((i % 2 == 1 ? EnumParticleTypes.CRIT_MAGIC : EnumParticleTypes.CRIT), posX, posY, posZ, -motionX + rand.nextGaussian(), 0.01D, -motionZ + rand.nextGaussian());
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (!worldObj.isRemote) {
			EntityLivingBase thrower = this.getThrower();
			EntityPlayer player = (thrower instanceof EntityPlayer ? (EntityPlayer) thrower : null);
			SwordBeam skill = (player != null ? (SwordBeam) ZSSPlayerSkills.get(player).getPlayerSkill(SkillBase.swordBeam) : null);
			if (mop.typeOfHit == MovingObjectType.ENTITY) {
				Entity entity = mop.entityHit;
				if (entity == thrower || this.entitiesHit.contains(entity.getEntityId())) { return; }
				if (player != null && skill != null) {
					skill.onImpact(player, false);
				}
				if (entity.attackEntityFrom(DamageUtils.causeIndirectComboDamage(this, thrower).setProjectile(), damage)) {
					WorldUtils.playSoundAtEntity(entity, Sounds.DAMAGE_SUCCESSFUL_HIT, 0.4F, 0.5F);
				}
				if (this.isMaster && this.damage > 0.5F) {
					this.damage *= 0.8F; // damage reduces for each entity hit
					this.entitiesHit.add(entity.getEntityId());
				} else {
					this.setDead();
				}
			} else {
				Block block = worldObj.getBlockState(mop.getBlockPos()).getBlock();
				if (block.getMaterial() != Material.air) {
					block.onEntityCollidedWithBlock(worldObj, mop.getBlockPos(), this);
				}
				if (block.getMaterial().blocksMovement()) {
					if (player != null && skill != null) {
						skill.onImpact(player, true);
					}
					setDead();
				}
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("isMaster", isMaster);
		compound.setFloat("damage", damage);
		compound.setInteger("level", level);
		compound.setInteger("lifespan", lifespan);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		isMaster = (compound.getBoolean("isMaster"));
		damage = compound.getFloat("damage");
		level = compound.getInteger("level");
		lifespan = compound.getInteger("lifespan");
	}

	@Override
	public float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		return 0.0F; // not able to be reflected
	}
}
