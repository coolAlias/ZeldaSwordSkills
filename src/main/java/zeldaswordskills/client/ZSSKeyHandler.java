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
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.lib.Config;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.network.GetBombPacket;
import zeldaswordskills.network.OpenGuiPacket;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
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
				mc.thePlayer.closeScreen();
			}
		}
	}

	/**
	 * Only fires for custom key bindings, not all keys
	 */
	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		if (tickEnd) {
			kb.pressed = false;
		}
	}

	/**
	 * All ILockOnTarget skill related keys are handled here
	 */
	private static void handleTargetingKeys(Minecraft mc, KeyBinding kb, ZSSPlayerInfo skills) {
		ILockOnTarget skill = skills.getTargetingSkill();
		// key interaction is disabled if the player is stunned or a skill animation is in progress
		boolean canInteract = skills.canInteract() && !ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.STUN);

		if (skill == null || !skill.isLockedOn()) {
			return;
		}
		if (kb == keys[KEY_NEXT_TARGET]) {
			skill.getNextTarget(mc.thePlayer);
		} else if (kb == keys[KEY_ATTACK]) {
			Item heldItem = (mc.thePlayer.getHeldItem() != null ? mc.thePlayer.getHeldItem().getItem() : null);
			boolean flag = (heldItem instanceof ItemHeldBlock || (mc.thePlayer.attackTime > 0 && (Config.affectAllSwings() || heldItem instanceof ISwingSpeed)));
			if (canInteract && !flag) {
				kb.pressed = true;
			} else if (!flag) {
				// hack for Super Spin Attack, as it requires key press to be passed while animation is in progress
				if (skills.isSkillActive(SkillBase.spinAttack)) {
					skills.getActiveSkill(SkillBase.spinAttack).keyPressed(mc, kb, mc.thePlayer);
					return;
				}
			}
			// Only allow attack key to continue processing if it was set to pressed
			if (kb.pressed) {
				// Nayru's Love prevents skill activation, but can still attack
				if (skills.isNayruActive() || !skills.onKeyPressed(mc, kb)) {
					ZSSClientEvents.performComboAttack(mc, skill);
				}
				// hack for Armor Break to begin charging without having to press attack again
				if (skills.hasSkill(SkillBase.armorBreak)) {
					skills.getActiveSkill(SkillBase.armorBreak).keyPressed(mc, kb, mc.thePlayer);
				}
			}
		} else if (canInteract) {
			// always allowed to block, but other keys only allowed if Nayru's Love not active
			kb.pressed = (kb == keys[KEY_BLOCK] || !skills.isNayruActive());
			// Nayru's Love prevents all skill interaction
			if (!skills.isNayruActive()) {
				skills.onKeyPressed(mc, kb);
			}
		}
	}
}
