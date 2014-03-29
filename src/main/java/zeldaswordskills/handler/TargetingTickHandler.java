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

package zeldaswordskills.handler;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.lib.Config;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.Dash;
import zeldaswordskills.skills.sword.Dodge;
import zeldaswordskills.skills.sword.Parry;
import zeldaswordskills.skills.sword.RisingCut;
import zeldaswordskills.skills.sword.SpinAttack;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A render tick handler for updating the player's facing while locked on to a target.
 *
 */
@SideOnly(Side.CLIENT)
public class TargetingTickHandler implements ITickHandler
{
	private final Minecraft mc;

	/** The player whose view will update */
	private EntityPlayer player = null;

	/** Target from player's currently active ILockOnTarget */
	private Entity target = null;

	/** Whether the left movement key has been pressed; used every tick */
	boolean isLeftPressed;

	public TargetingTickHandler() {
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public EnumSet<TickType> ticks() { return EnumSet.of(TickType.RENDER); }

	@Override
	public String getLabel() { return null; }

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		player = mc.thePlayer;
		if (player != null && ZSSPlayerInfo.get(player) != null) {
			ZSSPlayerInfo skills = ZSSPlayerInfo.get(player);
			ILockOnTarget skill = skills.getTargetingSkill();
			if (skill != null) {
				// Flag is true if an animation is in progress
				boolean flag = false;

				if (skills.isSkillActive(SkillBase.dodge)) {
					flag = ((Dodge) skills.getPlayerSkill(SkillBase.dodge)).onRenderTick(player);
				} else if (skills.isSkillActive(SkillBase.spinAttack)) {
					flag = ((SpinAttack) skills.getPlayerSkill(SkillBase.spinAttack)).onRenderTick(player);
				} else if (skills.isSkillActive(SkillBase.risingCut)) {
					flag = ((RisingCut) skills.getPlayerSkill(SkillBase.risingCut)).onRenderTick(player);
				}
				if (!flag && skill.isLockedOn()) {
					target = skill.getCurrentTarget();
					updatePlayerView();
				}
				if (skill.isLockedOn() && skills.canInteract()) {
					if (isVanillaKeyPressed(mc.gameSettings.keyBindJump)) {
						if (skills.hasSkill(SkillBase.risingCut) && !skills.isSkillActive(SkillBase.risingCut) && !player.isUsingItem() && player.isSneaking()) {
							((RisingCut) skills.getPlayerSkill(SkillBase.risingCut)).keyPressed();
						} else if (skills.hasSkill(SkillBase.leapingBlow) && !skills.isSkillActive(SkillBase.leapingBlow) && player.isUsingItem()) {
							skills.activateSkill(mc.theWorld, SkillBase.leapingBlow);
							mc.gameSettings.keyBindUseItem.pressed = false;
							ZSSKeyHandler.keys[ZSSKeyHandler.KEY_BLOCK].pressed = false;
							if (skills.hasSkill(SkillBase.dash)) {
								((Dash) skills.getPlayerSkill(SkillBase.dash)).keyPressed(false);
							}
						}
					} else if (Config.allowVanillaControls()) {
						isLeftPressed = isVanillaKeyPressed(mc.gameSettings.keyBindLeft);
						if (isLeftPressed || isVanillaKeyPressed(mc.gameSettings.keyBindRight)) {
							if (skills.hasSkill(SkillBase.spinAttack)) {
								((SpinAttack) skills.getPlayerSkill(SkillBase.spinAttack)).keyPressed((isLeftPressed ? mc.gameSettings.keyBindLeft : mc.gameSettings.keyBindRight), player);
							}
							if (skills.hasSkill(SkillBase.dodge) && player.onGround) {
								((Dodge) skills.getPlayerSkill(SkillBase.dodge)).keyPressed((isLeftPressed ? mc.gameSettings.keyBindLeft : mc.gameSettings.keyBindRight), player);
							}
						} else if (isVanillaKeyPressed(mc.gameSettings.keyBindBack) && skills.hasSkill(SkillBase.parry)) {
							((Parry) skills.getPlayerSkill(SkillBase.parry)).keyPressed(player);
						}
					}
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		this.player = null;
		this.target = null;
	}

	/**
	 * Returns true if a vanilla keybinding is both pressed and isPressed()
	 * This is necessary to prevent skills from being activated as soon as locking on to a target,
	 * (when isPressed() is still true) or while the key is held down (pressed is true).
	 */
	@SideOnly(Side.CLIENT)
	private boolean isVanillaKeyPressed(KeyBinding key) { return key.isPressed() && key.pressed; }

	/**
	 * Rotates the player to face the current target
	 */
	private void updatePlayerView() {
		double dx = player.posX - target.posX;
		double dz = player.posZ - target.posZ;
		double angle = Math.atan2(dz, dx) * 180 / Math.PI;
		double pitch = Math.atan2(player.posY - (target.posY + (target.height / 2.0F)), Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI;
		double distance = player.getDistanceToEntity(target);
		float rYaw = (float)(angle - player.rotationYaw);
		while (rYaw > 180) { rYaw -= 360; }
		while (rYaw < -180) { rYaw += 360; }
		rYaw += 90F;
		float rPitch = (float) pitch - (float)(10.0F / Math.sqrt(distance)) + (float)(distance * Math.PI / 90);
		player.setAngles(rYaw, -(rPitch - player.rotationPitch));
	}
}
