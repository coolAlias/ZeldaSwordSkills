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

package zeldaswordskills.skills;

import java.util.UUID;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;

/**
 * 
 * BONUS HEART
 * Description: Each level in this skill grants an extra half heart of health
 *
 */
public class BonusHeart extends SkillPassive
{
	/** Max level that can be reached, regardless of config or other settings */
	public static final int MAX_BONUS_HEARTS = 50;
	private static final UUID bonusHeartUUID = UUID.fromString("14ED99DA-D333-4621-90C8-81C968A082E3");
	private static final AttributeModifier bonusHeartModifier = (new AttributeModifier(bonusHeartUUID, "ZSS Bonus Heart", 2.0D, 0)).setSaved(true);

	protected BonusHeart(String name) {
		super(name);
	}

	private BonusHeart(BonusHeart skill) {
		super(skill);
	}

	@Override
	public BonusHeart newInstance() {
		return new BonusHeart(this);
	}

	@Override
	public byte getMaxLevel() {
		return (byte)(Config.getMaxBonusHearts() + (Config.isHardcoreZeldaFan() ? 7 : 0));
	}

	@Override
	public String getIconTexture() {
		return ModInfo.ID + ":heart_container";
	}

	@Override
	public boolean isLoot() {
		return false;
	}

	@Override
	protected void levelUp(EntityPlayer player) {
		AttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
		if (attributeinstance.getModifier(bonusHeartUUID) != null) { attributeinstance.removeModifier(bonusHeartModifier); }
		AttributeModifier newModifier = (new AttributeModifier(bonusHeartUUID, "Bonus Heart", level * 2.0D, 0)).setSaved(true);
		attributeinstance.applyModifier(newModifier);
		player.heal(player.getMaxHealth());
	}

	@Override
	public void validateSkill(EntityPlayer player) {
		float health = player.getHealth();
		levelUp(player);
		player.setHealth(health);
	}
}
