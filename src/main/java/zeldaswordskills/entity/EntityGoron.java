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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentThorns;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityGoron extends EntityVillager
{
	/** Flag for handling attack timer during client-side health update */
	private static Byte ATTACK_FLAG = (byte) 4;

	public EntityGoron(World world) {
		this(world, 0);
	}

	public EntityGoron(World world, int profession) {
		super(world, profession);
		setSize(1.2F, 2.8F);
		setCanPickUpLoot(true);
		tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		tasks.addTask(2, new EntityAIAttackOnCollide(this, IMob.class, 1.0D, false));
		tasks.addTask(3, new EntityAIMoveThroughVillage(this, 0.6D, true));
		tasks.addTask(3, new EntityAIMoveTowardsTarget(this, getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue(), 16.0F));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(60.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.3D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setAttribute(0.75D);
		getAttributeMap().func_111150_b(SharedMonsterAttributes.attackDamage);
	}

	@Override
	public void onLivingUpdate() {
		updateArmSwingProgress();
		super.onLivingUpdate();
		if (getAITarget() instanceof EntityPlayer || getAttackTarget() instanceof EntityPlayer) {
			// func_142015_aE() returns the revenge timer:
			if (ticksExisted > func_142015_aE() + 30 || recentlyHit < 70) {
				setRevengeTarget(null);
				setAttackTarget(null);
				attackingPlayer = null;
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isEntityInvulnerable()) {
			return false;
		} else if (super.attackEntityFrom(source, amount)) {
			Entity entity = source.getEntity();
			if (riddenByEntity != entity && ridingEntity != entity) {
				if (entity != this) {
					entityToAttack = entity;
				}
				return true;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
    public void handleHealthUpdate(byte flag) {
		if (flag == ATTACK_FLAG) {
			// matches golem's value for rendering; not the same as value on server
			attackTime = 10;
		} else {
			super.handleHealthUpdate(flag);
		}
	}
	
	@Override
	public boolean attackEntityAsMob(Entity entity) {
		float amount = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		int knockback = 0;
		attackTime = 20; // set to 20 in EntityMob#attackEntity, but seems to be unnecessary due to AI
		worldObj.setEntityState(this, ATTACK_FLAG);
		
		if (entity instanceof EntityLivingBase) {
			amount += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) entity);
			knockback += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) entity);
		}

		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), amount);
		if (flag) {
			if (knockback > 0) {
				float f = (float) knockback * 0.5F;
				double dx = -MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F) * f;
				double dz = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F) * f;
				entity.addVelocity(dx, 0.1D, dz);
				motionX *= 0.6D;
				motionZ *= 0.6D;
			}

			int fire = EnchantmentHelper.getFireAspectModifier(this);
			if (fire > 0) {
				entity.setFire(fire * 4);
			}

			if (entity instanceof EntityLivingBase) {
				EnchantmentThorns.func_92096_a(this, (EntityLivingBase) entity, rand);
			}
		}

		return flag;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void collideWithEntity(Entity entity) {
		if (entity instanceof IMob && getRNG().nextInt(20) == 0) {
			setAttackTarget((EntityLivingBase) entity);
		}
		super.collideWithEntity(entity);
	}

	@Override
	public int getTotalArmorValue() {
		return super.getTotalArmorValue() + (isChild() ? 0 : 6);
	}

	// TODO update sounds to Goron-specific sounds
	@Override
	protected String getLivingSound() {
		return isTrading() ? "mob.villager.haggle" : "mob.villager.idle";
	}

	@Override
	protected String getHurtSound() {
		return "mob.villager.hit";
	}

	@Override
	protected String getDeathSound() {
		return "mob.villager.death";
	}

	/**
	 * Creates a child entity for {@link EntityAgeable#createChild(EntityAgeable)}
	 * @return class-specific instance, rather than generic EntityAgeable
	 */
	@Override
	public EntityGoron func_90012_b(EntityAgeable entity) {
		EntityGoron goron = new EntityGoron(worldObj);
		goron.onSpawnWithEgg(null);
		return goron;
	}
}
