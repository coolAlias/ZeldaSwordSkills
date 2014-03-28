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

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntitySwordBeam;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * SWORD BEAM
 * Description: Shoot a beam of energy from the sword tip
 * Activation: attack while sneaking and at near full health
 * Effect: Shoots a ranged beam, damaging a single target
 * Damage: Base sword damage (without other bonuses), +1 extra damage per skill level
 * Range: Approximately 12 blocks, plus one block per level (TODO test max distance)
 * Exhaustion: 0.9F - (0.06F * level)
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
	public SwordBeam(String name) {
		super(name);
		addDescription(Arrays.asList("swordbeam.desc.0","swordbeam.desc.1","swordbeam.desc.2"));
	}

	private SwordBeam(SwordBeam skill) { super(skill); }

	@Override
	public SwordBeam newInstance() { return new SwordBeam(this); }

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getDescription(EntityPlayer player) {
		List<String> desc = getDescription();
		desc.add(getDamageDisplay(level, true));
		desc.add(getExhaustionDisplay(getExhaustion()));
		return desc;
	}

	@Override
	public boolean isActive() { return false; }

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && checkHealth(player) && ZSSPlayerInfo.get(player).isSkillActive(swordBasic) && PlayerUtils.isHoldingSword(player);
	}

	@Override
	protected float getExhaustion() { return 0.9F - (level * 0.06F); }

	@Override
	public boolean trigger(World world, EntityPlayer player) {
		if (super.trigger(world, player)) {
			if (!world.isRemote) {
				// TODO play sound
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

	/** Returns true if players current health is within the allowed limit */
	private boolean checkHealth(EntityPlayer player) {
		return player.capabilities.isCreativeMode || PlayerUtils.getHealthMissing(player) < (0.31F * level);
	}

	/** Returns player's base damage (with sword) plus 1.0F per level */
	private float getDamage(EntityPlayer player) {
		return (float)(level + player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
	}
}
