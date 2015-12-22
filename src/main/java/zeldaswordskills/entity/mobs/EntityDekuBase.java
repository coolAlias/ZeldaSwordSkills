/**
    Copyright (C) <2016> <coolAlias>

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
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.ai.IEntityAnimationOffset;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntitySwordBeam;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class EntityDekuBase extends EntityCreature implements IMob, IEntityAnimationOffset
{
	/** Incrementing byte flag for #handleHealthUpdate to prevent action flag conflicts */
	protected static byte flag_index = 10;

	/** Byte flag for #handleHealthUpdate indicating that custom death animation should play */
	public static final byte CUSTOM_DEATH = EntityDekuBase.flag_index++;

	/**
	 * Set to other than 0 when a custom death animation should play. Uses an int
	 * to allow for different death animations. Only used client-side for rendering.
	 */
	public int custom_death;

	/** Used client side to offset animations relying on ticksExisted for their timing */
	private final int ticksExistedOffset;

	public EntityDekuBase(World world) {
		super(world);
		this.addAITasks();
		this.setSize(0.75F, 2.0F);
		this.experienceValue = 5;
		this.ticksExistedOffset = this.getTickOffset(world);
	}

	/**
	 * Return the {@link #ticksExistedOffset tick offset} for this entity's animations
	 */
	protected int getTickOffset(World world) {
		return world.rand.nextInt(3600);
	}

	@Override
	public int getTicksExistedOffset(int action_id) {
		return ticksExistedOffset;
	}

	/**
	 * Called during entity construction to add AI tasks
	 */
	protected void addAITasks() {}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(1.0D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0D);
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 100);
	}

	@Override
	protected String getHurtSound() {
		return Sounds.LEAF_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.LEAF_HIT;
	}

	/**
	 * Prevents Deku Baba from being moved by most means
	 */
	@Override
	public void addVelocity(double dx, double dy, double dz) {}

	@Override
	public boolean handleWaterMovement() {
		return false; // can't be pushed by water
	}

	@Override
	public boolean handleLavaMovement() {
		return false; // can't be pushed by lava
	}

	@Override
	public void setSprinting(boolean sprinting) {}

	@Override
	protected void jump() {}

	@Override
	public void fall(float distance) {}

	@Override
	protected boolean func_146066_aG() {
		return true; // true to drop loot when killed by a player
	}

	/**
	 * Whether this entity is currently capable of attacking
	 */
	protected boolean canAttack() {
		return true;
	}

	@Override
	public boolean canAttackClass(Class clazz) {
		return !EntityDekuBase.class.isAssignableFrom(clazz) && super.canAttackClass(clazz);
	}

	@Override
	public boolean canEntityBeSeen(Entity entity) {
		Vec3 start = Vec3.createVectorHelper(this.posX, this.posY + this.getEyeHeight(), this.posZ);
		Vec3 end = Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
		return this.worldObj.func_147447_a(start, end, false, true, false) == null;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (!canAttack()) {
			return false;
		}
		// copied from EntityMob
		float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		int i = 0;
		if (entity instanceof EntityLivingBase) {
			f += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)entity);
			i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)entity);
		}
		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), f);
		if (flag) {
			if (i > 0) {
				entity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
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

	/**
	 * Returns if the damage source is fatal to this deku in its current state
	 */
	protected boolean isSourceFatal(DamageSource source) {
		if (source.getSourceOfDamage() instanceof EntityBoomerang) {
			return true;
		} else if (source.getEntity() instanceof EntityPlayer) {
			return (ZSSPlayerSkills.get((EntityPlayer) source.getEntity()).isSkillActive(SkillBase.spinAttack));
		} else if (source.isExplosion() && ZSSEntityInfo.get(this).hasIngestedBomb()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the damage is a slashing type
	 */
	protected boolean isSlashing(DamageSource source) {
		if (source.getSourceOfDamage() instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) source.getSourceOfDamage();
			if (PlayerUtils.isSword(entity.getHeldItem())) {
				return true; // this covers all swords plus spin attack
			}
		} else if (source.isProjectile()) {
			// TODO return source.getEntity() instanceof ISlashing
			return source.getSourceOfDamage() instanceof EntityBoomerang || source.getSourceOfDamage() instanceof EntitySwordBeam;
		}
		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.worldObj.isRemote && this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
			this.setDead();
		}
	}

	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
		if (!worldObj.isRemote) {
			byte flag = getCustomDeathFlag(source);
			if (flag != 0) {
				worldObj.setEntityState(this, flag);
			}
		}
	}

	/**
	 * Return non-zero flag for a custom death animation; this flag will be sent to {@link #handleHealthUpdate(byte)}
	 */
	protected byte getCustomDeathFlag(DamageSource source) {
		return (isSlashing(source) ? CUSTOM_DEATH : 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte flag) {
		if (flag == CUSTOM_DEATH) {
			custom_death = (this.rand.nextInt(2) == 0 ? -1 : 1);
		} else {
			super.handleHealthUpdate(flag);
		}
	}

	@Override
	protected Item getDropItem() {
		return Items.stick;
	}

	@Override
	public boolean getCanSpawnHere() {
		if (this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
			return false;
		} else if (!this.isValidSpawnBlock() || !this.isValidLightLevel()) {
			return false;
		}
		return super.getCanSpawnHere();
	}

	/**
	 * Checks whether the block beneath the Deku is a valid block, e.g. grass or dirt,
	 * by calling {@link #isValidMaterial(Material)}
	 */
	protected boolean isValidSpawnBlock() {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);
		return isValidMaterial(worldObj.getBlock(i, j - 1, k).getMaterial());
	}

	/**
	 * Checks whether the block Material type is valid for purposes of spawning
	 */
	protected boolean isValidMaterial(Material material) {
		return material == Material.grass || material == Material.ground ||
				material == Material.clay || material == Material.sand;
	}

	/**
	 * Checks to make sure the light is not too bright where the mob is spawning
	 */
	protected boolean isValidLightLevel() {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);
		if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > this.rand.nextInt(32)) {
			return false;
		}
		int light = this.worldObj.getBlockLightValue(i, j, k);
		if (this.worldObj.isThundering()) {
			int sky = this.worldObj.skylightSubtracted;
			this.worldObj.skylightSubtracted = 10;
			light = this.worldObj.getBlockLightValue(i, j, k);
			this.worldObj.skylightSubtracted = sky;
		}
		return light <= this.rand.nextInt(8);
	}
}
