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

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.SkillBase;

public class EntityBlackKnight extends EntityDarknut implements IBossDisplayData
{
	public EntityBlackKnight(World world) {
		super(world);
		setSize(1.2F, 3.6F);
		experienceValue = 50;
		enablePersistence();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		setArmorDamage(60.0F);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(300.0D);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(9.0D); // unarmed
		setWearingCape((byte) 120);
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public int getType() {
		return 1;	// considered 'Mighty'
	}

	@Override
	public EntityBlackKnight setType(int type) {
		return this; // only one type, nothing to set
	}

	@Override
	public void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 100);
		setCurrentItemOrArmor(ArmorIndex.EQUIPPED_HELM, new ItemStack(Items.chainmail_helmet));
		setCurrentItemOrArmor(ArmorIndex.EQUIPPED_LEGS, new ItemStack(Items.chainmail_leggings));
		setCurrentItemOrArmor(ArmorIndex.EQUIPPED_BOOTS, new ItemStack(Items.chainmail_boots));
		ItemStack sword = getEquipmentInSlot(0);
		if (sword != null) {
			switch(worldObj.getDifficulty()) {
			case NORMAL:
				sword.addEnchantment(Enchantment.sharpness, 2);
				break;
			case HARD:
				sword.addEnchantment(Enchantment.sharpness, 4);
				sword.addEnchantment(Enchantment.fireAspect, 1);
			default:
			}
		}
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		super.dropFewItems(recentlyHit, lootingLevel);
		entityDropItem(new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), 0.0F);
	}

	@Override
	public boolean isLightArrowFatal() {
		return false;
	}

	@Override
	public float getLightArrowDamage(float amount) {
		return (amount * 4.0F);
	}

	@Override
	public float getOffensiveModifier(EntityLivingBase entity, ItemStack stack) {
		return super.getOffensiveModifier(entity, stack) - 0.2F; // +2 levels of parry
	}

	@Override
	public float getDefensiveModifier(EntityLivingBase entity, ItemStack stack) {
		return super.getDefensiveModifier(entity, stack) + 0.2F; // +2 levels of parry
	}

	@Override
	protected float getDamageArmorAmount() {
		return damageDarknutArmor(Math.min(getArmorDamage(), 20.0F)); // 3 hits to destroy
	}
}
