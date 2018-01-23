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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ai.EntityAILevitate;
import zeldaswordskills.entity.ai.EntityAIRangedMagic;
import zeldaswordskills.entity.ai.EntityAITeleport;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.SkillBase;

/**
 * 
 * Boss version of Wizzrobe
 *
 */
public class EntityWizzrobeGrand extends EntityWizzrobe implements IBossDisplayData
{
	/** Data watcher index for the Grand Wizzrobe's current magic type */
	protected static final int TYPE_INDEX = 16;

	/** Transformation timer for boss version */
	private int transformTimer;

	public EntityWizzrobeGrand(World world) {
		super(world);
		this.tasks.addTask(0, new EntityAILevitate(this, 2.5D));
		this.targetTasks.taskEntries.clear();
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.func_110163_bv(); // sets persistence required to true, meaning will not despawn
		this.setType(this.rand.nextInt(MagicType.values().length));
		this.setSize(1.0F, 3.0F);
		this.experienceValue = 50;
	}

	@Override
	public MagicType getMagicType() {
		return MagicType.values()[this.dataWatcher.getWatchableObjectByte(TYPE_INDEX) % MagicType.values().length];
	}

	/** Set the current Magic Type */
	private void setType(MagicType type) {
		this.dataWatcher.updateObject(TYPE_INDEX, (byte)(type.ordinal()));
		this.applyTypeTraits();
	}

	/** Set the current Magic Type by index */
	private void setType(int type) {
		// Skip water type
		if (type == MagicType.WATER.ordinal()) {
			type += 1 + this.rand.nextInt(MagicType.values().length - 1);
		}
		this.setType(MagicType.values()[type % MagicType.values().length]);
	}

	@Override
	protected EntityAITeleport getNewTeleportAI() {
		return new EntityAITeleport(this, 16.0D, 60, false, true, true, true, true);
	}

	@Override
	protected EntityAIRangedMagic getMagicAI() {
		return new EntityAIRangedMagic(this, 10, 40, 24.0D);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(300.0D);
	}

	protected void applyTypeTraits() {
		ZSSEntityInfo info = ZSSEntityInfo.get(this);
		info.removeAllBuffs();
		info.applyBuff(Buff.RESIST_MAGIC, Integer.MAX_VALUE, 50);
		info.applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 100);
		switch (this.getMagicType()) {
		case FIRE:
			info.applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 50);
			info.applyBuff(Buff.WEAKNESS_COLD, Integer.MAX_VALUE, 100);
			break;
		case ICE:
			info.applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 50);
			info.applyBuff(Buff.WEAKNESS_FIRE, Integer.MAX_VALUE, 100);
			break;
		case LIGHTNING:
			info.applyBuff(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 50);
			break;
		case WIND:
			break;
		case WATER:
			break;
		}
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public int getTotalArmorValue() {
		return super.getTotalArmorValue() + (worldObj.difficultySetting.getDifficultyId() * 4);
	}

	@Override
	protected float getTelevadeChance() {
		return 1.0F;
	}

	@Override
	protected int getBaseCastingTime() {
		return 60;
	}

	@Override
	protected float getBaseSpellDamage() {
		return 8.0F;
	}

	@Override
	protected float getSpellAoE() {
		return 2.5F;
	}

	@Override
	protected float getReflectChance() {
		return 1.0F; // 100% reflect chance
	}

	@Override
	protected float getReflectedDamage(float damage) {
		// Note that Wizzrobes have 50% magic resistance as well as e.g. 50% Fire Resist
		float i = 1.0F + (2 * Math.max(1, this.worldObj.difficultySetting.getDifficultyId()));
		return Math.max(damage, this.getMaxHealth() / i);
	}

	@Override
	protected float getMinInterruptDamage() {
		return 8.0F;
	}

	@Override
	protected float getMaxInterruptDamage() {
		return 32.0F; // 8 damage has 25% interrupt chance
	}

	@Override
	public int beginSpellCasting(EntityLivingBase target) {
		if (transformTimer > 0) {
			return 0;
		}
		return super.beginSpellCasting(target);
	}

	@Override
	public void castPassiveSpell() {} // TODO give him some passive spells?

	@Override
	public void castRangedSpell(EntityLivingBase target, float range) {
		super.castRangedSpell(target, range);
		transformTimer = 10;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (transformTimer > 0) {
			if (--transformTimer == 0 && !worldObj.isRemote) {
				this.setType(this.rand.nextInt(MagicType.values().length));
			}
		}
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		super.dropFewItems(recentlyHit, lootingLevel);
		this.entityDropItem(new ItemStack(ZSSItems.skillOrb, 1, SkillBase.bonusHeart.getId()), 0.0F);
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		return new ItemStack(ZSSItems.treasure, 1, Treasures.EVIL_CRYSTAL.ordinal());
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return wasItemStolen;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("MagicType", this.dataWatcher.getWatchableObjectByte(TYPE_INDEX));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.dataWatcher.updateObject(TYPE_INDEX, compound.getByte("MagicType"));
	}
}
