/**
    Copyright (C) <2018> <coolAlias>

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

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ICyclableItem;
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.network.server.CycleItemModePacket;
import zeldaswordskills.network.server.GetBombPacket;
import zeldaswordskills.network.server.OpenGuiPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;

@SideOnly(Side.CLIENT)
public class ZSSKeyHandler
{
	private final Minecraft mc;

	// Key index for easy handling and retrieval of keys and key descriptions
	public static final byte KEY_SKILL_ACTIVATE = 0;
	public static final byte KEY_NEXT_TARGET = 1;
	public static final byte KEY_ATTACK = 2;
	public static final byte KEY_LEFT = 3;
	public static final byte KEY_RIGHT = 4;
	public static final byte KEY_DOWN = 5;
	public static final byte KEY_BLOCK = 6;
	public static final byte KEY_BOMB = 7;
	public static final byte KEY_TOGGLE_AUTOTARGET = 8;
	public static final byte KEY_TOGGLE_BUFFBAR = 9;
	public static final byte KEY_SKILLS_GUI = 10;
	public static final byte KEY_PREV_MODE = 11;
	public static final byte KEY_NEXT_MODE = 12;

	/** Key descriptions - this is what the player sees when changing key bindings in-game */
	public static final String[] desc = {
			"activate",
			"next",
			"attack",
			"left",
			"right",
			"down",
			"block",
			"bomb",
			"toggleat",
			"togglebuff",
			"skills_gui",
			"prev_mode",
			"next_mode"
	};

	/** Default key values */
	private static final int[] keyValues = {
			Keyboard.KEY_X, // Activate lockon
			Keyboard.KEY_TAB, // Next target
			Keyboard.KEY_UP, // Attack
			Keyboard.KEY_LEFT,
			Keyboard.KEY_RIGHT,
			Keyboard.KEY_DOWN,
			Keyboard.KEY_RCONTROL, // Block / use item
			Keyboard.KEY_B, // Get bomb
			Keyboard.KEY_PERIOD, // Toggle auto-targeting
			Keyboard.KEY_V, // Toggle buff bar
			Keyboard.KEY_P, // Open skill GUI
			Keyboard.KEY_LBRACKET, // Previous item mode
			Keyboard.KEY_RBRACKET // Next item mode
	};

	public static final KeyBinding[] keys = new KeyBinding[desc.length];

	public ZSSKeyHandler() {
		this.mc = Minecraft.getMinecraft();
		for (int i = 0; i < desc.length; ++i) {
			keys[i] = new KeyBinding("key.zss." + desc[i] + ".desc", keyValues[i], "key.zss.label");
			ClientRegistry.registerKeyBinding(keys[i]);
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (Keyboard.getEventKeyState()) {
			onKeyPressed(mc, Keyboard.getEventKey());
		}
	}

	/**
	 * Call for any key code, mouse or keyboard, to handle custom key bindings that may
	 * have been remapped to mouse. From MouseEvent, ONLY call this method when the mouse
	 * key is pressed, not when it is released (i.e. when event.buttonstate is true).
	 * @param mc	Pass in Minecraft instance, since this is a static method
	 * @param kb	The key code of the key pressed; for the mouse, this is the mouse button number minus 100
	 */
	public static void onKeyPressed(Minecraft mc, int kb) {
		if (mc.inGameHasFocus && mc.thePlayer != null) {
			ZSSPlayerSkills skills = ZSSPlayerSkills.get(mc.thePlayer);
			if (kb == mc.gameSettings.keyBindSprint.getKeyCode()) {
				// Don't allow sprinting while in mid-air (motionY is < 0 even when standing on a block)
				int x = MathHelper.floor_double(mc.thePlayer.posX);
				int y = MathHelper.floor_double(mc.thePlayer.posY - mc.thePlayer.getYOffset());
				int z = MathHelper.floor_double(mc.thePlayer.posZ);
				if (!mc.theWorld.isSideSolid(new BlockPos(x, y - 1, z), EnumFacing.UP)) {
					KeyBinding.setKeyBindState(kb, false);
				}
			} else if (kb == keys[KEY_SKILL_ACTIVATE].getKeyCode()) {
				if (skills.hasSkill(SkillBase.swordBasic)) {
					PacketDispatcher.sendToServer(new ActivateSkillPacket(SkillBase.swordBasic));
				}
			} else if (kb == keys[KEY_BOMB].getKeyCode()) {
				// prevent player from holding RMB while getting bombs, as it can crash the game
				if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
					KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
				}
				PacketDispatcher.sendToServer(new GetBombPacket());
			} else if (kb == keys[KEY_TOGGLE_AUTOTARGET].getKeyCode()) {
				if (mc.thePlayer.isSneaking()) {
					ChatComponentTranslation desc = new ChatComponentTranslation(Config.toggleTargetPlayers() ? "chat.zss.key.enable" : "chat.zss.key.disable");
					PlayerUtils.sendTranslatedChat(mc.thePlayer, "chat.zss.key.toggletp", desc);
				} else {
					ChatComponentTranslation desc = new ChatComponentTranslation(Config.toggleAutoTarget() ? "chat.zss.key.enable" : "chat.zss.key.disable");
					PlayerUtils.sendTranslatedChat(mc.thePlayer, "chat.zss.key.toggleat", desc);
				}
			} else if (kb == keys[KEY_TOGGLE_BUFFBAR].getKeyCode()) {
				if (mc.thePlayer.isSneaking()) {
					Config.isComboHudEnabled = !Config.isComboHudEnabled;
					ChatComponentTranslation desc = new ChatComponentTranslation(Config.isComboHudEnabled ? "chat.zss.key.enable" : "chat.zss.key.disable");
					PlayerUtils.sendTranslatedChat(mc.thePlayer, "chat.zss.key.togglehud", desc);
				} else {
					Config.isBuffBarEnabled = !Config.isBuffBarEnabled;
				}
			} else if (kb == keys[KEY_SKILLS_GUI].getKeyCode()) {
				PacketDispatcher.sendToServer(new OpenGuiPacket(GuiHandler.GUI_SKILLS));
			} else if (kb == keys[KEY_PREV_MODE].getKeyCode() || kb == keys[KEY_NEXT_MODE].getKeyCode()) {
				boolean next = kb == keys[KEY_NEXT_MODE].getKeyCode();
				ItemStack stack = mc.thePlayer.getHeldItem();
				if (stack != null && stack.getItem() instanceof ICyclableItem) {
					if (next) {
						((ICyclableItem) stack.getItem()).nextItemMode(stack, mc.thePlayer);
					} else {
						((ICyclableItem) stack.getItem()).prevItemMode(stack, mc.thePlayer);
					}
					PacketDispatcher.sendToServer(new CycleItemModePacket(next));
				}
			} else {
				handleTargetingKeys(mc, kb, skills);
			}
		}
	}

	/**
	 * All ILockOnTarget skill related keys are handled here
	 */
	private static void handleTargetingKeys(Minecraft mc, int kb, ZSSPlayerSkills skills) {
		ILockOnTarget skill = skills.getTargetingSkill();
		// key interaction is disabled if the player is stunned or a skill animation is in progress
		boolean canInteract = skills.canInteract() && !ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.STUN);
		if (skill == null || !skill.isLockedOn()) {
			return;
		}
		if (kb == keys[KEY_NEXT_TARGET].getKeyCode()) {
			skill.getNextTarget(mc.thePlayer);
		} else if (kb == keys[KEY_ATTACK].getKeyCode() || kb == mc.gameSettings.keyBindAttack.getKeyCode()) {
			KeyBinding key = (kb == keys[KEY_ATTACK].getKeyCode() ? keys[KEY_ATTACK] : mc.gameSettings.keyBindAttack);
			Item heldItem = (mc.thePlayer.getHeldItem() != null ? mc.thePlayer.getHeldItem().getItem() : null);
			boolean flag = (heldItem instanceof ItemHeldBlock || (!ZSSPlayerInfo.get(mc.thePlayer).canAttack() && (Config.affectAllSwings() || heldItem instanceof ISwingSpeed)));
			if (canInteract && !flag) {
				KeyBinding.setKeyBindState(key.getKeyCode(), true);
			} else if (!flag) {
				// hack for Super Spin Attack, as it requires key press to be passed while animation is in progress
				if (skills.isSkillActive(SkillBase.spinAttack)) {
					skills.getActiveSkill(SkillBase.spinAttack).keyPressed(mc, key, mc.thePlayer);
					return;
				} else if (skills.isSkillActive(SkillBase.backSlice)) {
					skills.getActiveSkill(SkillBase.backSlice).keyPressed(mc, key, mc.thePlayer);
					return;
				}
			}
			// Only allow attack key to continue processing if it was set to pressed
			if (key.isKeyDown()) {
				if (!skills.onKeyPressed(mc, key)) {
					ZSSClientEvents.performComboAttack(mc, skill);
				}
				// hack for Armor Break to begin charging without having to press attack again
				if (skills.hasSkill(SkillBase.armorBreak)) {
					skills.getActiveSkill(SkillBase.armorBreak).keyPressed(mc, key, mc.thePlayer);
				}
			}
		} else if (canInteract) {
			// Only works for keys mapped to custom key bindings, which is fine for remapped mouse keys
			KeyBinding key = getKeyBindFromCode(mc, kb);
			if (key != null) {
				KeyBinding.setKeyBindState(kb, true);
				skills.onKeyPressed(mc, key);
			}
		}
	}

	/**
	 * Returns the KeyBinding corresponding to the key code given, or NULL if no key binding is found.
	 * Currently handles all custom keys, plus the following vanilla keys:
	 * <br>Always allowed: keyBindForward, keyBindJump
	 * <br>{@link Config#allowVanillaControls}: keyBindLeft, keyBindRight, keyBindBack
	 * @param keyCode	Will be a negative number for mouse keys, or positive for keyboard
	 * @param mc		Pass in Minecraft instance as a workaround to get vanilla KeyBindings
	 */
	@SideOnly(Side.CLIENT)
	public static KeyBinding getKeyBindFromCode(Minecraft mc, int keyCode) {
		for (KeyBinding k : keys) {
			if (k.getKeyCode() == keyCode) {
				return k;
			}
		}
		// there must be a better way to do this...
		if (keyCode == mc.gameSettings.keyBindForward.getKeyCode()) {
			return mc.gameSettings.keyBindForward;
		} else if (keyCode == mc.gameSettings.keyBindJump.getKeyCode()) {
			return mc.gameSettings.keyBindJump;
		} else if (Config.allowVanillaControls) {
			if (keyCode == mc.gameSettings.keyBindLeft.getKeyCode()) {
				return mc.gameSettings.keyBindLeft;
			} else if (keyCode == mc.gameSettings.keyBindRight.getKeyCode()) {
				return mc.gameSettings.keyBindRight;
			} else if (keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
				return mc.gameSettings.keyBindBack;
			}
		}
		return null;
	}
}
