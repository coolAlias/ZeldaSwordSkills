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

package zeldaswordskills.entity.mobs;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.TargetUtils;

// TODO switch attack logic to use AI system
public class EntityOctorok extends EntityWaterMob implements IMob, IEntityLootable, IEntityVariant
{
	/** Squid type data watcher index (skeleton's use 13) */
	private static final int OCTOROK_TYPE_INDEX = 13;

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

	/** Replacement for removed 'attackTime' */
	protected int attackTime;

	public EntityOctorok(World world) {
		super(world);
		experienceValue = 5;
		setSize(0.95F, 0.95F);
		rand.setSeed((long)(1 + getEntityId()));
		rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
		tasks.addTask(0, new EntityOctorok.AIMoveRandom());
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(OCTOROK_TYPE_INDEX, (byte)(rand.nextInt(5) == 0 ? 1 : 0));
	}

	/**
	 * Returns the octorok's type: 0 - normal, 1 - bomb shooter
	 */
	public int getType() {
		return dataWatcher.getWatchableObjectByte(OCTOROK_TYPE_INDEX);
	}

	/**
	 * Sets the octorok's type: 0 - normal, 1 - bomb shooter
	 */
	@Override
	public EntityOctorok setType(int type) {
		dataWatcher.updateObject(OCTOROK_TYPE_INDEX, (byte)(type % 2));
		return this;
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
		return super.canAttackClass(clazz) && clazz != EntityOctorok.class;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!worldObj.isRemote && worldObj.getDifficulty() == EnumDifficulty.PEACEFUL) {
			setDead();
		}
		updateAttackTarget();
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
				Entity target = getAttackTarget();
				if (target != null) {
					TargetUtils.setEntityHeading(this, randomMotionVecX, randomMotionVecY, randomMotionVecZ, 0.25F, 1.0F, false);
					faceEntity(target, 30.0F, 120.0F); // squid model values: 30.0F, 210.0F
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

	/**
	 * Updates the current attack target
	 */
	protected void updateAttackTarget() {
		float distance = 0.0F;
		EntityLivingBase entityToAttack = getAttackTarget();
		if (entityToAttack == null) {
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
		if (entityToAttack != getAttackTarget()) {
			setAttackTarget(entityToAttack);
		}
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
		return worldObj.handleMaterialAcceleration(getEntityBoundingBox().expand(0.0D, -0.6000000238418579D, 0.0D), Material.water, this);
	}

	@Override
	public boolean getCanSpawnHere() {
		return posY > 45.0D && posY < 63.0D && worldObj.getDifficulty() != EnumDifficulty.PEACEFUL && super.getCanSpawnHere();
	}

	@Override
	public int getTotalArmorValue() {
		return Math.min(super.getTotalArmorValue() + 2, 20);
	}

	// @Override
	protected EntityLivingBase findPlayerToAttack() {
		EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, 16.0D);
		return entityplayer != null && canEntityBeSeen(entityplayer) ? entityplayer : null;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isEntityInvulnerable(source) || source.isExplosion()) {
			return false;
		} else if (super.attackEntityFrom(source, amount)) {
			Entity entity = source.getEntity();
			if (entity != this && entity instanceof EntityLivingBase && riddenByEntity != entity && ridingEntity != entity) {
				setAttackTarget((EntityLivingBase) entity);
				return true;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Attack entity with 'touch of death'
	 */
	@Override
	public boolean attackEntityAsMob(Entity entity) {
		attackTime = 20;
		float damage = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		int knockback = 0;
		if (entity instanceof EntityLivingBase) {
			damage += EnchantmentHelper.func_152377_a(getHeldItem(), ((EntityLivingBase) entity).getCreatureAttribute());
			knockback += EnchantmentHelper.getKnockbackModifier(this);
		}
		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
		if (flag) {
			if (knockback > 0) {
				entity.addVelocity((double)(-MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F) * (float)knockback * 0.5F), 0.1D, (double)(MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F) * (float)knockback * 0.5F));
				motionX *= 0.6D;
				motionZ *= 0.6D;
			}
			int j = EnchantmentHelper.getFireAspectModifier(this);
			if (j > 0) {
				entity.setFire(j * 4);
			}
			if (entity instanceof EntityLivingBase) {
				EnchantmentHelper.func_151384_a((EntityLivingBase) entity, this);
			}
			EnchantmentHelper.func_151385_b(this, entity);
		}
		return flag;
	}

	/**
	 * Attack entity with ranged attack
	 */
	protected void attackEntityWithRangedAttack(EntityLivingBase entity) {
		attackTime = 20;
		float f = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		Entity projectile;
		int difficulty = worldObj.getDifficulty().getDifficultyId();
		if (getType() == 1) {
			projectile = new EntityBomb(worldObj, this, (EntityLivingBase) entity, 1.0F, (float)(14 - difficulty * 4)).
					setType(BombType.BOMB_WATER).setTime(12 - (difficulty * 2)).setNoGrief().setMotionFactor(0.25F).setDamage(f * 2.0F * difficulty);
		} else {
			projectile = new EntityThrowingRock(worldObj, this, (EntityLivingBase) entity, 1.0F, (float)(14 - difficulty * 4)).
					setIgnoreWater().setDamage(f * difficulty);
		}
		// TODO worldObj.playSoundAtEntity(this, ModInfo.SOUND_WEB_SPLAT, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.0F));
		if (!worldObj.isRemote) {
			worldObj.spawnEntityInWorld(projectile);
		}
	}

	/**
	 * Determines which type of attack (melee or ranged) to perform against the target entity
	 */
	protected void attackEntity(Entity entity, float distance) {
		if (attackTime <= 0) {
			if (distance < 2.0F && entity.getEntityBoundingBox().maxY > getEntityBoundingBox().minY && entity.getEntityBoundingBox().minY < getEntityBoundingBox().maxY) {
				attackEntityAsMob(entity);
			} else if (rand.nextInt(60) == 0 && entity instanceof EntityLivingBase) {
				attackEntityWithRangedAttack((EntityLivingBase) entity);
			}
		}
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		int j = rand.nextInt(2 + lootingLevel) + 1;
		for (int k = 0; k < j; ++k) {
			entityDropItem(new ItemStack(Items.dye, 1, 0), 0.0F);
		}
	}

	@Override
	protected void addRandomDrop() {
		switch(rand.nextInt(8)) {
		case 0:
			entityDropItem(new ItemStack(ZSSItems.treasure, 1, Treasures.TENTACLE.ordinal()), 0.0F);
			break;
		default:
			if (getType() == 1) {
				entityDropItem(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()), 0.0F);
			} else {
				entityDropItem(new ItemStack(rand.nextInt(3) == 1 ? Items.emerald : ZSSItems.smallHeart), 0.0F);
			}
		}
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return 0.2F;
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		if (rand.nextFloat() < (0.1F * (1 + whip.ordinal()))) {
			return new ItemStack(ZSSItems.treasure, 1, Treasures.TENTACLE.ordinal());
		} else if (getType() == 1 && rand.nextFloat() < (0.1F * (1 + whip.ordinal()))) {
			return new ItemStack(ZSSItems.bomb,1,BombType.BOMB_WATER.ordinal());
		}
		return new ItemStack(Items.dye, 1, 0);
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return true;
	}

	@Override
	public boolean isHurtOnTheft(EntityPlayer player, WhipType whip) {
		return Config.getHurtOnSteal();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("octorokType", (byte) getType());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("octorokType")) {
			setType(compound.getByte("octorokType"));
		}
	}

	//========================= Below this line copied from EntitySquid ===================//
	public void setRandomMotion(float randomMotionX, float randomMotionY, float randomMotionZ) {
		this.randomMotionVecX = randomMotionX;
		this.randomMotionVecY = randomMotionY;
		this.randomMotionVecZ = randomMotionZ;
	}

	public boolean isMovingRandomly() {
		return randomMotionVecX != 0.0F || randomMotionVecY != 0.0F || randomMotionVecZ != 0.0F;
	}

	class AIMoveRandom extends EntityAIBase {
		private EntityOctorok entity = EntityOctorok.this;
		@Override
		public boolean shouldExecute() {
			return true;
		}
		@Override
		public void updateTask() {
			int i = entity.getAge();
			Entity target = entity.getAttackTarget();
			if (i > 100) {
				entity.setRandomMotion(0.0F, 0.0F, 0.0F);
			} else if (target != null && entity.getRNG().nextInt(25) == 0) {
				float dx = (float)(target.posX - posX) * 0.015F;
				float dy = (float)(1 + target.posY - posY) * 0.015F;
				float dz = (float)(target.posZ - posZ) * 0.015F;
				entity.setRandomMotion(dx, dy, dz);
			} else if (entity.getRNG().nextInt(50) == 0 || !entity.inWater || !entity.isMovingRandomly()) {
				float f = entity.getRNG().nextFloat() * (float)Math.PI * 2.0F;
				float dx = MathHelper.cos(f) * 0.2F;
				float dy = -0.1F + entity.getRNG().nextFloat() * 0.2F;
				float dz = MathHelper.sin(f) * 0.2F;
				entity.setRandomMotion(dx, dy, dz);
			}
		}
	}
}
