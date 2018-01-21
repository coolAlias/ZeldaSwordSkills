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

package zeldaswordskills.entity.mobs;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.api.entity.IReflectable.IReflectableOrigin;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.TargetUtils;

public class EntityOctorok extends EntityWaterMob implements IMob, IEntityLootable
{
	/** Squid-related fields for random movement and rendering */
	public float squidPitch;
	public float prevSquidPitch;
	public float squidYaw;
	public float prevSquidYaw;
	public float squidRotation;
	public float prevSquidRotation;
	public float tentacleAngle;
	public float prevTentacleAngle;
	private float randomMotionSpeed;
	private float rotationVelocity;
	private float field_70871_bB;
	private float randomMotionVecX;
	private float randomMotionVecY;
	private float randomMotionVecZ;

	public EntityOctorok(World world) {
		super(world);
		experienceValue = 5;
		setSize(0.95F, 0.95F);
		rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canAttackClass(Class clazz) {
		return !EntityOctorok.class.isAssignableFrom(clazz) && super.canAttackClass(clazz);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!worldObj.isRemote && worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
			setDead();
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		prevSquidPitch = squidPitch;
		prevSquidYaw = squidYaw;
		prevSquidRotation = squidRotation;
		prevTentacleAngle = tentacleAngle;
		squidRotation += rotationVelocity;
		if (squidRotation > ((float) Math.PI * 2F)) {
			squidRotation -= ((float) Math.PI * 2F);
			if (rand.nextInt(10) == 0) {
				rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
			}
		}
		if (isInWater()) {
			float f;
			if (squidRotation < (float) Math.PI) {
				f = squidRotation / (float) Math.PI;
				tentacleAngle = MathHelper.sin(f * f * (float) Math.PI) * (float) Math.PI * 0.25F;
				if ((double) f > 0.75D) {
					randomMotionSpeed = 1.0F;
					field_70871_bB = 1.0F;
				} else {
					field_70871_bB *= 0.8F;
				}
			} else {
				tentacleAngle = 0.0F;
				randomMotionSpeed *= 0.9F;
				field_70871_bB *= 0.99F;
			}
			if (!worldObj.isRemote) {
				if (entityToAttack != null) {
					TargetUtils.setEntityHeading(this, randomMotionVecX, randomMotionVecY, randomMotionVecZ, 0.25F, 1.0F, false);
					faceEntity(entityToAttack, 30.0F, 120.0F); // squid model values: 30.0F, 210.0F
				} else {
					motionX = (double)(randomMotionVecX * randomMotionSpeed);
					motionY = (double)(randomMotionVecY * randomMotionSpeed);
					motionZ = (double)(randomMotionVecZ * randomMotionSpeed);
				}
			}
			renderYawOffset += (-((float) Math.atan2(motionX, motionZ)) * 180.0F / (float) Math.PI - renderYawOffset) * 0.1F;
			rotationYaw = renderYawOffset;
			squidYaw += (float) Math.PI * field_70871_bB * 1.5F;
			f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
			squidPitch += (-((float) Math.atan2((double) f, motionY)) * 180.0F / (float) Math.PI - squidPitch) * 0.1F;
		} else {
			tentacleAngle = MathHelper.abs(MathHelper.sin(squidRotation)) * (float) Math.PI * 0.25F;
			squidPitch = (float)((double) squidPitch + (double)(-90.0F - squidPitch) * 0.02D);
			if (!worldObj.isRemote) {
				motionX = 0.0D;
				motionY -= 0.08D;
				motionY *= 0.9800000190734863D;
				motionZ = 0.0D;
			}
		}
	}

	@Override
	public void moveEntityWithHeading(float dx, float dz) {
		moveEntity(motionX, motionY, motionZ);
	}

	@Override
	protected void updateEntityActionState() {
		float distance = 0.0F;
		++entityAge;
		if (entityToAttack == null) {
			entityToAttack = findPlayerToAttack();
		} else if (entityToAttack instanceof EntityPlayer && ((EntityPlayer) entityToAttack).capabilities.disableDamage) {
			entityToAttack = findPlayerToAttack();
		} else if (entityToAttack.isEntityAlive() && canAttackClass(entityToAttack.getClass())) {
			distance = entityToAttack.getDistanceToEntity(this);
			if (distance > 16.0F) {
				entityToAttack = null;
			} else if (canEntityBeSeen(entityToAttack)) {
				attackEntity(entityToAttack, distance);
			}
		} else {
			entityToAttack = null;
		}
		if (entityAge > 100) {
			randomMotionVecX = randomMotionVecY = randomMotionVecZ = 0.0F;
		} else if (rand.nextInt(entityToAttack != null ? 25 : 50) == 0 || !inWater || randomMotionVecX == 0.0F && randomMotionVecY == 0.0F && randomMotionVecZ == 0.0F) {
			if (entityToAttack != null && distance > 5.0F) {
				randomMotionVecX = (float)(entityToAttack.posX - posX) * 0.015F;
				randomMotionVecY = (float)(1 + entityToAttack.posY - posY) * 0.015F;
				randomMotionVecZ = (float)(entityToAttack.posZ - posZ) * 0.015F;
			} else {
				float f = rand.nextFloat() * (float) Math.PI * 2.0F;
				randomMotionVecX = MathHelper.cos(f) * 0.2F;
				randomMotionVecY = -0.1F + rand.nextFloat() * 0.3F;
				randomMotionVecZ = MathHelper.sin(f) * 0.2F;
			}
		}
		despawnEntity();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(12.0D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.75D);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

	@Override
	public boolean isInWater() {
		return worldObj.handleMaterialAcceleration(boundingBox.expand(0.0D, -0.6000000238418579D, 0.0D), Material.water, this);
	}

	@Override
	public boolean getCanSpawnHere() {
		return posY > 45.0D && posY < 63.0D && worldObj.difficultySetting != EnumDifficulty.PEACEFUL && super.getCanSpawnHere();
	}

	@Override
	public int getTotalArmorValue() {
		return Math.min(super.getTotalArmorValue() + 2, 20);
	}

	@Override
	protected Entity findPlayerToAttack() {
		EntityPlayer entityplayer = worldObj.getClosestVulnerablePlayerToEntity(this, 16.0D);
		return entityplayer != null && canEntityBeSeen(entityplayer) ? entityplayer : null;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable()) {
			return false;
		} else if (source.getSourceOfDamage() instanceof IReflectableOrigin && ((IReflectableOrigin) source.getSourceOfDamage()).getReflectedOriginEntity() == this) {
			// Auto-kill when hit by its own reflected projectile
			return super.attackEntityFrom(source, Math.max(amount, this.getHealth() * 2.0F));
		} else if (source.getEntity() instanceof EntityOctorok) {
			return false; // don't want octoroks killing their own kind
		} else if (super.attackEntityFrom(source, amount)) {
			Entity entity = source.getEntity();
			if (this.riddenByEntity != entity && this.ridingEntity != entity) {
				if (entity != this) {
					this.entityToAttack = entity;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Return the number of ticks that must pass before the Octorok may make another attack
	 * @param melee True if the last attack was a melee attack
	 * @return
	 */
	protected int getNextAttackTime(boolean melee) {
		if (melee) {
			return 20;
		}
		// Minimum ticks between each ranged attack depends on difficulty
		int min = 20 * (4 - this.worldObj.difficultySetting.getDifficultyId());
		return min + this.rand.nextInt(21) + this.rand.nextInt(21);
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		this.attackTime = this.getNextAttackTime(true);
		float damage = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		int i = 0;
		if (entity instanceof EntityLivingBase) {
			damage += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)entity);
			i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)entity);
		}
		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
		if (flag) {
			if (i > 0) {
				entity.addVelocity((double)(-MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
				motionX *= 0.6D;
				motionZ *= 0.6D;
			}
			int j = EnchantmentHelper.getFireAspectModifier(this);
			if (j > 0) {
				entity.setFire(j * 4);
			}
			if (entity instanceof EntityLivingBase) {
				EnchantmentHelper.func_151384_a((EntityLivingBase)entity, this);
			}
			EnchantmentHelper.func_151385_b(this, entity);
		}
		return flag;
	}

	@Override
	protected void attackEntity(Entity entity, float distance) {
		if (this.attackTime > 0) {
			// can't attack right now
		} else if (distance < 2.0F) {
			if (entity.boundingBox.maxY > this.boundingBox.minY && entity.boundingBox.minY < this.boundingBox.maxY) {
				this.attackEntityAsMob(entity);
			}
		} else if (this.rand.nextInt(60) == 0 && entity instanceof EntityLivingBase && TargetUtils.isTargetInSight(this, entity)) {
			this.attackTime = this.getNextAttackTime(false);
			float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
			Entity projectile = this.getProjectile((EntityLivingBase) entity, damage);
			if (!this.worldObj.isRemote && projectile != null) {
				this.worldObj.playSoundAtEntity(this, Sounds.CORK, 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 1.0F));
				this.worldObj.spawnEntityInWorld(projectile);
			}
		}
	}

	/**
	 * @return the projectile to shoot at the given target
	 */
	protected Entity getProjectile(EntityLivingBase target, float damage) {
		int difficulty = worldObj.difficultySetting.getDifficultyId();
		return new EntityThrowingRock(worldObj, this, target, 0.2F + (difficulty * 0.1F), (float)(14 - difficulty * 4))
				.setIgnoreWater()
				.setDamage(damage * difficulty)
				.setGravityVelocity(0.001F);
	}

	@Override
	protected Item getDropItem() {
		return ZSSItems.throwingRock;
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		int j = rand.nextInt(2 + lootingLevel) + 1;
		for (int k = 0; k < j; ++k) {
			entityDropItem(new ItemStack(getDropItem(), 1, 0), 0.0F);
		}
	}

	@Override
	protected void dropRareDrop(int rarity) {
		switch (rarity) {
		case 1: this.entityDropItem(new ItemStack(ZSSItems.treasure, 1, Treasures.TENTACLE.ordinal()), 0.0F); break;
		default: this.entityDropItem(rand.nextInt(3) == 0 ? new ItemStack(Items.emerald) : new ItemStack(ZSSItems.smallHeart), 0.0F);
		}
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return 0.2F;
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		if (rand.nextFloat() < (0.1F * (1 + whip.ordinal()))) {
			return new ItemStack(ZSSItems.treasure,1,Treasures.TENTACLE.ordinal());
		}
		return new ItemStack(getDropItem(), 1, 0);
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return true;
	}

	@Override
	public boolean isHurtOnTheft(EntityPlayer player, WhipType whip) {
		return Config.getHurtOnSteal();
	}
}
