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

package zeldaswordskills.client;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.client.gui.GuiBuffBar;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.inventory.ContainerSkills;
import zeldaswordskills.lib.Config;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.network.GetBombPacket;
import zeldaswordskills.network.OpenGuiPacket;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.ArmorBreak;
import zeldaswordskills.skills.sword.Dodge;
import zeldaswordskills.skills.sword.Parry;
import zeldaswordskills.skills.sword.SpinAttack;
import zeldaswordskills.skills.sword.SwordBreak;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ZSSKeyHandler extends KeyHandler
{
	private final Minecraft mc;

	/** Key index for easy handling and retrieval of keys and key descriptions */
	public static final byte KEY_SKILL_ACTIVATE = 0, KEY_NEXT_TARGET = 1, KEY_ATTACK = 2,
			KEY_LEFT = 3, KEY_RIGHT = 4, KEY_DOWN = 5, KEY_BLOCK = 6, KEY_BOMB = 7,
			KEY_TOGGLE_AUTOTARGET = 8, KEY_TOGGLE_BUFFBAR = 9, KEY_SKILLS_GUI = 10;

	/** Key descriptions - this is what the player sees when changing key bindings in-game */
	public static final String[] desc = { "activate","next","attack","left","right","down",
		"block","bomb","toggleat","togglebuff","skills_gui"};

	/** Default key values */
	private static final int[] keyValues = {Keyboard.KEY_X, Keyboard.KEY_TAB, Keyboard.KEY_UP,
		Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT, Keyboard.KEY_DOWN, Keyboard.KEY_RCONTROL,
		Keyboard.KEY_B, Keyboard.KEY_PERIOD, Keyboard.KEY_V, Keyboard.KEY_P};

	public static final KeyBinding[] keys = new KeyBinding[desc.length];

	/**
	 * Initializes keybindings and registers a new KeyHandler instance
	 */
	public static final void init() {
		boolean[] repeat = new boolean[desc.length];
		for (int i = 0; i < desc.length; ++i) {
			keys[i] = new KeyBinding("key.zss." + desc[i] + ".desc", keyValues[i]);
			repeat[i] = false;
		}
		KeyBindingRegistry.registerKeyBinding(new ZSSKeyHandler(keys, repeat));
	}

	private ZSSKeyHandler(KeyBinding[] keys, boolean[] repeat) {
		super(keys, repeat);
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public String getLabel() {
		return StatCollector.translateToLocal("key.zss.label");
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		if (tickEnd && mc.thePlayer != null) {
			onKeyPressed(mc, kb);
		}
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		if (tickEnd) {
			onKeyReleased(mc, kb);
		}
	}

	/**
	 * Reciprocal version of {@link #onKeyReleased}, to be called when a key is pressed.
	 * Call for any key code, mouse or keyboard, to handle custom key bindings that may
	 * have been remapped to mouse. From MouseEvent, ONLY call this method when the mouse
	 * key is pressed, not when it is released (i.e. when event.buttonstate is true).
	 * @param mc	Pass in Minecraft instance, since this is a static method
	 * @param kb	The key binding pressed; NULL is acceptable
	 */
	public static void onKeyPressed(Minecraft mc, KeyBinding kb) {
		ZSSPlayerInfo skills = ZSSPlayerInfo.get(mc.thePlayer);
		if (mc.inGameHasFocus && skills != null) {
			if (kb == keys[KEY_SKILL_ACTIVATE]) {
				if (skills.hasSkill(SkillBase.swordBasic)) {
					PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.swordBasic).makePacket());
				}
			} else if (kb == keys[KEY_BOMB]) {
				// prevent player from holding RMB while getting bombs, as it can crash the game
				if (mc.gameSettings.keyBindUseItem.pressed) {
					KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false);
				}
				PacketDispatcher.sendPacketToServer(new GetBombPacket().makePacket());
			} else if (kb == keys[KEY_TOGGLE_AUTOTARGET]) {
				if (mc.thePlayer.isSneaking()) {
					mc.thePlayer.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.key.toggletp",
							(Config.toggleTargetPlayers() ? StatCollector.translateToLocal("chat.zss.key.enable") : StatCollector.translateToLocal("chat.zss.key.disable"))));
				} else {
					mc.thePlayer.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.key.toggleat",
							(Config.toggleAutoTarget() ? StatCollector.translateToLocal("chat.zss.key.enable") : StatCollector.translateToLocal("chat.zss.key.disable"))));
				}
			} else if (kb == keys[KEY_TOGGLE_BUFFBAR]) {
				GuiBuffBar.shouldDisplay = !GuiBuffBar.shouldDisplay;
			} else if (kb == keys[KEY_SKILLS_GUI]) {
				PacketDispatcher.sendPacketToServer(new OpenGuiPacket(GuiHandler.GUI_SKILLS).makePacket());
			} else {
				handleTargetingKeys(mc, kb, skills);
			}
		} else if (kb == keys[KEY_SKILLS_GUI] && mc.thePlayer.openContainer instanceof ContainerSkills) {
			KeyBinding.setKeyBindState(kb.keyCode, false);
			mc.thePlayer.closeScreen();
		}
	}

	/**
	 * Reciprocal version of {@link #onKeyPressed}, to be called when a key is released.
	 * Call for any key code, mouse or keyboard, to handle custom key bindings that may
	 * have been remapped to mouse. From MouseEvent, ONLY call this method when the mouse
	 * key is released, not when it is pressed (i.e. when event.buttonstate is false).
	 * @param mc	Pass in Minecraft instance, since this is a static method
	 * @param kb	The key binding pressed; NULL is acceptable
	 */
	public static void onKeyReleased(Minecraft mc, KeyBinding kb) {
		if (kb == keys[KEY_BLOCK]) {
			keys[KEY_BLOCK].pressed = false;
		} else if (kb == keys[KEY_ATTACK]) {
			keys[KEY_ATTACK].pressed = false;
			ZSSPlayerInfo skills = ZSSPlayerInfo.get(mc.thePlayer);
			if (skills.hasSkill(SkillBase.armorBreak)) {
				((ArmorBreak) skills.getPlayerSkill(SkillBase.armorBreak)).keyPressed(mc.thePlayer, false);
			}
		} else if (kb == keys[KEY_LEFT]) {
			keys[KEY_LEFT].pressed = false;
		} else if (kb == keys[KEY_RIGHT]) {
			keys[KEY_RIGHT].pressed = false;
		}
	}

	/**
	 * All ILockOnTarget skill related keys are handled here
	 */
	private static void handleTargetingKeys(Minecraft mc, KeyBinding kb, ZSSPlayerInfo skills) {
		ILockOnTarget skill = skills.getTargetingSkill();
		boolean canInteract = skills.canInteract() && !ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.STUN);

		if (skill == null || !skill.isLockedOn()) {
			return;
		}
		if (kb == keys[KEY_NEXT_TARGET]) {
			skill.getNextTarget(mc.thePlayer);
		} else if (kb == keys[KEY_ATTACK]) {
			Item heldItem = (mc.thePlayer.getHeldItem() != null ? mc.thePlayer.getHeldItem().getItem() : null);
			if (canInteract && !(mc.thePlayer.attackTime > 0 && (Config.affectAllSwings() || heldItem instanceof ISwingSpeed))) {
				keys[KEY_ATTACK].pressed = true;
			} else {
				if (skills.isSkillActive(SkillBase.spinAttack)) {
					((SpinAttack) skills.getPlayerSkill(SkillBase.spinAttack)).keyPressed(kb, mc.thePlayer);
				}
				return;
			}
			if (heldItem != null) {
				if (skills.shouldSkillActivate(SkillBase.dash)) {
					PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.dash).makePacket());
				} else if (skills.shouldSkillActivate(SkillBase.risingCut)) {
					PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.risingCut).makePacket());
					ZSSClientEvents.performComboAttack(mc, skill);
				} else if (skills.shouldSkillActivate(SkillBase.swordBeam)) {
					PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.swordBeam).makePacket());
				} else if (skills.shouldSkillActivate(SkillBase.endingBlow)) {
					PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.endingBlow).makePacket());
					ZSSClientEvents.performComboAttack(mc, skill);
				} else {
					ZSSClientEvents.performComboAttack(mc, skill);
				}
				// handle separately so can attack and begin charging without pressing key twice
				if (skills.hasSkill(SkillBase.armorBreak)) {
					((ArmorBreak) skills.getPlayerSkill(SkillBase.armorBreak)).keyPressed(mc.thePlayer, true);
				}
			} else if (skills.shouldSkillActivate(SkillBase.mortalDraw)) {
				PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.mortalDraw).makePacket());
			} else {
				ZSSClientEvents.performComboAttack(mc, skill);
			}
		} else if (kb == keys[KEY_LEFT] || kb == keys[KEY_RIGHT]) {
			if (kb == keys[KEY_RIGHT]) {
				keys[KEY_RIGHT].pressed = true;
			} else {
				keys[KEY_LEFT].pressed = true;
			}
			if (canInteract) {
				if (skills.hasSkill(SkillBase.dodge) && mc.thePlayer.onGround) {
					((Dodge) skills.getPlayerSkill(SkillBase.dodge)).keyPressed(kb, mc.thePlayer);
				}
				if (skills.hasSkill(SkillBase.spinAttack)) {
					((SpinAttack) skills.getPlayerSkill(SkillBase.spinAttack)).keyPressed(kb, mc.thePlayer);
				}
			}
		} else if (kb == keys[KEY_DOWN] && canInteract) {
			if (PlayerUtils.isUsingItem(mc.thePlayer) && skills.hasSkill(SkillBase.swordBreak)) {
				((SwordBreak) skills.getPlayerSkill(SkillBase.swordBreak)).keyPressed(mc.thePlayer);
			} else if (skills.hasSkill(SkillBase.parry)) {
				((Parry) skills.getPlayerSkill(SkillBase.parry)).keyPressed(mc.thePlayer);
			}
		} else if (kb == keys[KEY_BLOCK] && canInteract) {
			keys[KEY_BLOCK].pressed = true;
		}
	}
}
