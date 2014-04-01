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

package zeldaswordskills.skills.sword;

import java.util.List;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntitySwordBeam;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * SWORD BEAM
 * Description: Shoot a beam of energy from the sword tip
 * Activation: Attack while sneaking and at near full health
 * Effect: Shoots a ranged beam, damaging a single target
 * Damage: Base sword damage (without other bonuses), +1 extra damage per skill level
 * Range: Approximately 12 blocks, plus one block per level
 * Exhaustion: 3.0F - (0.2F * level)
 * Special:
 * 	- May only be used while locked on to a target
 *  - Amount of health required decreases with skill level, down to 1-1/2 hearts below max
 *  - Hitting a target with the beam counts as a direct strike for combos
 *  - Using the Master Sword will shoot a beam that can penetrate multiple targets
 * 
 * Sword beam shot from Link's sword when at full health. Inflicts the sword's full
 * base damage, not including enchantment or other bonuses, to the first entity struck.
 * 
 * If using the Master Sword, the beam will shoot through enemies, hitting all targets
 * in its direct path.
 * 
 */
public class SwordBeam extends SkillActive
{
	/** Used to end combo if the sword beam fails to strike a target */
	private int missTimer;

	public SwordBeam(String name) {
		super(name);
	}

	private SwordBeam(SwordBeam skill) {
		super(skill);
	}

	@Override
	public SwordBeam newInstance() {
		return new SwordBeam(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getDescription(EntityPlayer player) {
		List<String> desc = getDescription();
		desc.add(getDamageDisplay(level, true));
		desc.add(getExhaustionDisplay(getExhaustion()));
		return desc;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && checkHealth(player) && PlayerUtils.isHoldingSword(player)
				&& player.attackTime == 0 && ZSSPlayerInfo.get(player).isSkillActive(swordBasic);
	}

	@Override
	protected float getExhaustion() {
		return 3.0F - (0.2F * level);
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (missTimer > 0 && !player.worldObj.isRemote) {
			--missTimer;
			if (missTimer == 0) {
				ICombo combo = ZSSPlayerInfo.get(player).getComboSkill();
				if (combo != null && combo.isComboInProgress()) {
					combo.getCombo().endCombo(player);
				}
			}
		}
	}

	@Override
	public boolean trigger(World world, EntityPlayer player) {
		if (super.trigger(world, player)) {
			player.attackTime = (player.capabilities.isCreativeMode ? 0 : 20 - level);
			if (!world.isRemote) {
				missTimer = 12 + level;
				WorldUtils.playSoundAtEntity(player.worldObj, player, Sounds.WHOOSH, 0.4F, 0.5F);
				Vec3 vec3 = player.getLookVec();
				EntitySwordBeam beam = new EntitySwordBeam(world, player).setLevel(level);
				beam.setDamage(getDamage(player));
				beam.setMasterSword(PlayerUtils.isHoldingMasterSword(player));
				beam.setPosition(beam.posX + vec3.xCoord * 2, beam.posY + vec3.yCoord * 2, beam.posZ + vec3.zCoord * 2);
				world.spawnEntityInWorld(beam);
			} else {
				player.swingItem();
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Call upon impacting a target to determine whether combo should be terminated
	 * @param endCombo true if the combo should be terminated
	 */
	public void onImpact(EntityPlayer player, boolean endCombo) {
		missTimer = (endCombo && missTimer > 0 ? 1 : 0);
	}

	/** Returns true if players current health is within the allowed limit */
	private boolean checkHealth(EntityPlayer player) {
		float f = (Config.getBeamRequiresFullHealth() ? 0.0F : (0.3F * level));
		return player.capabilities.isCreativeMode || PlayerUtils.getHealthMissing(player) <= f;
	}

	/** Returns player's base damage (with sword) plus 1.0F per level */
	private float getDamage(EntityPlayer player) {
		return (float)(level + player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
	}
}
