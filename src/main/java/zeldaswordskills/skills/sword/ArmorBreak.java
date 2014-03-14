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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.handler.ZSSKeyHandler;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * ARMOR BREAK
 * Description: Unleash a powerful blow that ignores armor
 * Activation: hold the 'up' arrow for (20 - level) ticks
 * Effect: Unleashes an attack that inflicts normal sword damage but ignores armor
 * Exhaustion: 1.2F - (0.06F * level)
 * Special: May only be used while locked on to a target
 * 			Charge time is reduced by 5 ticks when wielding a Master Sword
 * 
 * Using this skill performs an attack that ignores armor but otherwise deals exactly the
 * same damage as a normal attack with the given item would, including all bonuses from other
 * skills and enchantments.
 * 
 * Armor Break cannot be activated by normal means. It must be charged by holding the 'attack'
 * key, and once the charge reaches full, the player will perform the Armor Break attack.
 * 
 */
public class ArmorBreak extends SkillActive
{
	/** Set to 1 when triggered; set to 0 when target struck in onImpact() */
	private int activeTimer = 0;

	/** Current charge time */
	private int charge = 0;

	public ArmorBreak(String name, byte id) {
		super(name, id);
		addDescription(Arrays.asList("armorbreak.desc.0","armorbreak.desc.1"));
		disableUserActivation();
		setDisablesLMB();
	}

	/** Returns number of ticks required before attack will execute: 20 - level */
	private int getChargeTime(EntityPlayer player) {
		return (PlayerUtils.isHoldingMasterSword(player) ? 15 : 20) - level;
	}

	/** Returns true if the skill is still charging up */
	public boolean isCharging() { return charge > 0; }

	private ArmorBreak(ArmorBreak skill) { super(skill); }

	@Override
	public ArmorBreak newInstance() { return new ArmorBreak(this); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getDescription(EntityPlayer player) {
		List<String> desc = getDescription();
		desc.add(getChargeDisplay(getChargeTime(player)));
		desc.add(getExhaustionDisplay(getExhaustion()));
		return desc;
	}

	@Override
	public boolean isActive() { return activeTimer > 0; }

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && ZSSPlayerInfo.get(player).isSkillActive(swordBasic) && PlayerUtils.isHoldingSword(player);
	}

	@Override
	protected float getExhaustion() { return 1.2F - (level * 0.06F); }

	/** Called when key first pressed; initiates charging */
	@SideOnly(Side.CLIENT)
	public void keyPressed(EntityPlayer player) { charge = getChargeTime(player); }

	/** Returns true if skill should continue charging up (key is still held down) */
	@SideOnly(Side.CLIENT)
	private boolean isKeyPressed() {
		return ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK].pressed || (Config.allowVanillaControls() &&
				Minecraft.getMinecraft().gameSettings.keyBindAttack.pressed && !Minecraft.getMinecraft().gameSettings.keyBindAttack.isPressed());
	}

	@Override
	public boolean trigger(World world, EntityPlayer player) {
		if (super.trigger(world, player)) {
			activeTimer = 1;
			ILockOnTarget skill = ZSSPlayerInfo.get(player).getTargetingSkill();
			if (skill != null && skill.isLockedOn()) {// && TargetUtils.canReachTarget(player, skill.getCurrentTarget())) {
				player.attackTargetEntityWithCurrentItem(skill.getCurrentTarget());
			}
		}

		return isActive();
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isCharging()) {
			if (isKeyPressed() && PlayerUtils.isHoldingSword(player)) {
				if (!player.isSwingInProgress) {
					if (charge < (getChargeTime(player) - 1)) {
						Minecraft.getMinecraft().playerController.sendUseItem(player, player.worldObj, player.getHeldItem());
					}
					--charge;
				}
				if (charge == 0) {
					// can't use the standard disable LMB method, since Armor Break will not return true for isActive
					player.attackTime = 4;
					player.swingItem();
					SwordBasic skill = (SwordBasic) ZSSPlayerInfo.get(player).getPlayerSkill(swordBasic);
					if (skill != null && skill.onAttack(player)) {
						PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(this, true).makePacket());
					}
				}
			} else {
				charge = 0;
			}
		}
		
		if (isActive()) {
			--activeTimer;
		}
	}

	/**
	 * WARNING: Something REALLY dirty is about to go down here.
	 * Uses a custom accessor class planted in net.minecraft.entity package to access
	 * protected method 'damageEntity'; sets event amount to zero to prevent further processing
	 */
	public void onImpact(EntityPlayer player, LivingHurtEvent event) {
		activeTimer = 0;
		player.worldObj.playSoundAtEntity(player, ModInfo.SOUND_ARMORBREAK, (player.worldObj.rand.nextFloat() * 0.4F + 0.5F), 1.0F / (player.worldObj.rand.nextFloat() * 0.4F + 0.5F));
		DirtyEntityAccessor.damageEntity(event.entityLiving, DamageUtils.causeArmorBreakDamage(player), event.ammount);
		event.ammount = 0.0F;
	}
}
