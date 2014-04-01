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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Attacking while blocking and locked on to a target will execute a bash attack.
 * The player charges into the target, inflicting damage and knocking the target back.
 * 
 * Range: 4 blocks plus 1 block per additional level
 * Damage: 4 plus 1 per additional level
 * Knockback: 2 blocks, plus 1 per additional level
 * Exhaustion: 1.0F minus 0.1F per level (0.5F at level 5)
 * Special: Must be at least 2 blocks away from target when skill is activated to
 * 			inflict damage, minus 0.2F per level (down to 1 block at level 5)
 * 
 */
public class Dash extends SkillActive
{
	/** True when Slam is used and while the player is in motion towards the target */
	private boolean isActive = false;

	/** Total distance currently traveled */
	private double distance;

	/** Target acquired from ILockOnTarget skill */
	private Entity target;

	public Dash(String name) {
		super(name);
		setDisablesLMB();
	}

	private Dash(Dash skill) {
		super(skill);
	}

	@Override
	public Dash newInstance() {
		return new Dash(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getDescription(EntityPlayer player) {
		List<String> desc = getDescription();
		desc.add(getRangeDisplay(getRange()));
		desc.add(getDamageDisplay(getDamage(), false));
		desc.add(getExhaustionDisplay(getExhaustion()));
		return desc;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canExecute(EntityPlayer player) {
		return player.isUsingItem() && player.onGround;
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && PlayerUtils.isHoldingSword(player)
				&& ZSSPlayerInfo.get(player).isSkillActive(swordBasic);
	}

	/** Damage is base damage plus 1.0F per level */
	private float getDamage() {
		return (4.0F + level);
	}

	/** Range increases by 1 block per level */
	private double getRange() {
		return (3.0D + level);
	}

	/** Deactivates skill and resets local variables */
	private void deactivate() {
		isActive = false;
		distance = 0.0D;
	}

	@Override
	protected float getExhaustion() {
		return 1.0F - (0.1F * level);
	}

	@Override
	public boolean activate(World world, EntityPlayer player) {
		isActive = super.activate(world, player);
		if (isActive) {
			ILockOnTarget skill = ZSSPlayerInfo.get(player).getTargetingSkill();
			if (skill != null && skill.isLockedOn()) {
				target = skill.getCurrentTarget();
			} else {
				target = TargetUtils.acquireLookTarget(player, (int) getRange(), getRange(), true);
			}
		}
		return isActive();
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			if (target instanceof EntityLivingBase) {
				double d0 = 0.15D * (target.posX - player.posX);
				double d1 = 0.15D * (target.posY + (double)(target.height / 3.0F) - player.posY);
				double d2 = 0.15D * (target.posZ - player.posZ);
				Vec3 vec3 = player.worldObj.getWorldVec3Pool().getVecFromPool(d0, d1, d2);

				float f = 1.0F;
				if (player.getCurrentArmor(ArmorIndex.WORN_BOOTS) != null) {
					if (player.getCurrentArmor(ArmorIndex.WORN_BOOTS).getItem() == ZSSItems.bootsPegasus) {
						f = 1.5F;
					} else if (player.getCurrentArmor(ArmorIndex.WORN_BOOTS).getItem() == ZSSItems.bootsHeavy) {
						f = 0.2F;
					}
				}

				player.addVelocity(vec3.xCoord * f, 0.0D, vec3.zCoord * f);
				distance += vec3.lengthVector();

				if (f > 0.5F && target.getDistanceSqToEntity(player) <= 9.0D) {
					if (distance > (2.0D - (0.2D * level))) {
						target.attackEntityFrom(DamageUtils.causeNonSwordDamage(player), getDamage() * f);
						target.addVelocity(vec3.xCoord * f * (0.2D + (0.1D * level)), 0.1D + f * (level * 0.025D), vec3.zCoord * f * (0.2D + (0.1D * level)));
					}
					player.motionX = player.motionZ = 0.0D;
					player.addVelocity(-vec3.xCoord, 0.0D, -vec3.zCoord);
					WorldUtils.playSoundAtEntity(player.worldObj, player, Sounds.SLAM, 0.4F, 0.5F);
					deactivate();
				}

				if (distance > getRange()) {
					deactivate();
				}
			} else {
				deactivate();
			}
		}
	}
}
