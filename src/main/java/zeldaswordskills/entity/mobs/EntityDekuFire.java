/**
    Copyright (C) <2017> <coolAlias>

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

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.entity.ai.EntityAIDynamicRangedAction;
import zeldaswordskills.api.entity.ai.EntityAction;
import zeldaswordskills.api.entity.ai.IEntityDynamicAI;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.WorldUtils;

public class EntityDekuFire extends EntityDekuBaba implements IEntityAdditionalSpawnData, IRangedAttackMob
{
	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		return BiomeType.getBiomeArray(new String[]{"hell"}, BiomeType.FIERY, BiomeType.JUNGLE, BiomeType.PLAINS);
	}

	public static final EntityAction ACTION_SPIT = new EntityAction(EntityDekuBase.flag_index++, 16, 7);
	static {
		EntityDekuBaba.registerAction(ACTION_SPIT);
	}

	/** Time it takes for the gland severing animation to complete */
	public static final int GLAND_DURATION = 20;

	/** Health update flag indicating gland has been severed */
	protected static final byte GLAND_FLAG = EntityDekuBase.flag_index++;

	private boolean has_gland;

	/** Timer used client side for animating the gland falling off */
	public int gland_timer;

	public EntityDekuFire(World world) {
		super(world);
		this.has_gland = true;
		this.isImmuneToFire = true;
	}

	/**
	 * Whether this Fire Baba still has its gland
	 */
	public boolean hasGland() {
		return has_gland;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
	}

	@Override
	protected void addAITasks() {
		super.addAITasks();
		this.tasks.addTask(2, new EntityAIDynamicRangedAction<EntityDekuFire>(this, ACTION_SPIT, 4.0F, 40.0F, 20, 60, 0.0F, true).setDifficultyScaled());
	}

	@Override
	public float getActionSpeed(int action_id) {
		int i = getDifficultyModifier() - 2;
		if (action_id == ACTION_SPIT.id) {
			return 0.6F + (i * 0.1F);
		}
		return super.getActionSpeed(action_id);
	}

	@Override
	public boolean canExecute(int action_id, IEntityDynamicAI ai) {
		if (action_id == ACTION_SPIT.id && !hasGland()) {
			return false;
		}
		return super.canExecute(action_id, ai);
	}

	@Override
	public void performAction(int action_id, IEntityDynamicAI ai) {
		Entity target = getCurrentTarget();
		if (isConfused()) {
			// do nothing
		} else if (action_id == ACTION_SPIT.id) {
			if (target instanceof EntityLivingBase) {
				attackEntityWithRangedAttack((EntityLivingBase) target, 1.0F);
			}
		} else {
			super.performAction(action_id, ai);
		}
	}

	/**
	 * @param rangeRatio Distance between the target and shooter divided by the shooter's maximum range, between 0.1F and 1.0F but not used here
	 */
	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float rangeRatio) {
		int difficulty = worldObj.getDifficulty().getDifficultyId();
		EntityMagicSpell spell = new EntityMagicSpell(worldObj, this, (EntityLivingBase) target, 0.375F + (0.125F * (float) difficulty), (float)(14 - difficulty * 4));
		spell.setGravityVelocity(0.01F);
		// Adjust position to account for animation head position
		float d = 1.5F;
		Vec3 vec3 = this.getLook(1.0F);
		spell.posX = this.posX + vec3.xCoord * d;
		spell.posY = this.posY + (double)(this.height / 2.0F) + 0.5D;
		spell.posZ = this.posZ + vec3.zCoord * d;
		spell.setArea(0.4F + (0.2F * difficulty));
		float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		spell.setDamage(damage);
		spell.setReflectChance(1.0F);
		spell.disableGriefing();
		spell.disableTrailingParticles();
		if (!worldObj.isRemote) {
			WorldUtils.playSoundAtEntity(this, Sounds.SPIT, 0.4F, 0.7F);
			worldObj.spawnEntityInWorld(spell);
		}
	}

	@Override
	public boolean isAttack(int action_id) {
		return action_id == ACTION_SPIT.id || super.isAttack(action_id);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (gland_timer != 0) {
			gland_timer += (gland_timer < 0 ? 1 : -1);
			if (gland_timer == 0) {
				has_gland = false; // set to false client side only after timer so it can still render
			}
		}
	}

	@Override
	protected float getSlashDamage(DamageSource source, float amount) {
		float damage = super.getSlashDamage(source, amount);
		if (has_gland && isSlashing(source)) {
			damage *= 0.25F;
		}
		return damage;
	}

	@Override
	protected void onProneAttack(DamageSource source, float amount) {
		if (!worldObj.isRemote && isSlashing(source)) {
			has_gland = false; // set to false immediately on server
			worldObj.setEntityState(this, GLAND_FLAG);
		}
		super.onProneAttack(source, amount);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte flag) {
		if (flag == GLAND_FLAG) {
			gland_timer = (rand.nextFloat() < 0.5F ? GLAND_DURATION : -GLAND_DURATION);
		} else {
			super.handleStatusUpdate(flag);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("has_gland", has_gland);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.has_gland = compound.getBoolean("has_gland");
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeBoolean(has_gland);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		has_gland = buffer.readBoolean();
	}
}
