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

package zeldaswordskills.entity.mobs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ai.EntityAILevitate;
import zeldaswordskills.entity.ai.EntityAIRangedMagic;
import zeldaswordskills.entity.ai.EntityAITeleport;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;

/**
 * 
 * Boss version of Wizzrobe
 *
 */
public class EntityGrandWizzrobe extends EntityWizzrobe implements IBossDisplayData
{
	/** Transformation timer for boss version */
	private int transformTimer;

	public EntityGrandWizzrobe(World world) {
		super(world);
		tasks.addTask(0, new EntityAILevitate(this, 2.5D));
		func_110163_bv(); // sets persistence required to true, meaning will not despawn
		setType(rand.nextInt(WizzrobeType.values().length));
		setSize(1.0F, 3.0F);
		experienceValue = 50;
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

	@Override
	protected void applyTypeTraits() {
		super.applyTypeTraits();
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 100);
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
				setType(rand.nextInt(WizzrobeType.values().length));
			}
		}
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		super.dropFewItems(recentlyHit, lootingLevel);
		entityDropItem(new ItemStack(ZSSItems.heartPiece), 0.0F);
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		return new ItemStack(ZSSItems.treasure,1,Treasures.EVIL_CRYSTAL.ordinal());
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		return wasItemStolen;
	}
}
