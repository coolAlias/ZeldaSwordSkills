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

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.api.item.IZoomHelper;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.handler.ZSSCombatEvents;
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.ArmorBreak;
import zeldaswordskills.skills.sword.SpinAttack;
import zeldaswordskills.util.TargetUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * SoundManager is only available on the client side; when run on a server, it will crash
 * if sounds are registered indiscriminately.
 * 
 * Player render events must also be done here, even though they should only be called on
 * the client, they will crash the game when running a dedicated server if placed in a
 * regular event handler.
 *
 */
@SideOnly(Side.CLIENT)
public class ZSSClientEvents
{
	private final Minecraft mc;
	/** True when openGL matrix needs to be popped */
	private boolean needsPop;
	/** Store the current key code for mouse buttons */
	private int mouseKey;
	/** Whether the button during mouse event is Minecraft's keyBindAttack */
	private boolean isAttackKey;
	/** Whether the button during mouse event is Minecraft's keyBindUseItem*/
	private boolean isUseKey;

	public ZSSClientEvents() {
		this.mc = Minecraft.getMinecraft();
	}

	/**
	 * Returns the KeyBinding corresponding to the key code given, or NULL if no key binding is found
	 * @param keyCode Will be a negative number for mouse keys, or positive for keyboard
	 */
	@SideOnly(Side.CLIENT)
	public static KeyBinding getKeyBindFromCode(int keyCode) {
		// Doesn't seem to be an easy way to get the KeyBinding from the key code...
		Iterator iterator = KeyBinding.keybindArray.iterator();
		while (iterator.hasNext()) {
			KeyBinding kb = (KeyBinding) iterator.next();
			if (kb.keyCode == keyCode) {
				return kb;
			}
		}
		return null;
	}

	/**
	 * Attacks current target if player is not currently using an item and {@link ICombo#onAttack}
	 * doesn't return false (i.e. doesn't miss)
	 * @param skill must implement BOTH {@link ILockOnTarget} AND {@link ICombo}
	 */
	@SideOnly(Side.CLIENT)
	public static void performComboAttack(Minecraft mc, ILockOnTarget skill) {
		if (!mc.thePlayer.isUsingItem()) {
			mc.thePlayer.swingItem();
			ZSSCombatEvents.setPlayerAttackTime(mc.thePlayer);
			if (skill instanceof ICombo && ((ICombo) skill).onAttack(mc.thePlayer)) {
				Entity entity = TargetUtils.getMouseOverEntity();
				mc.playerController.attackEntity(mc.thePlayer, (entity != null ? entity : skill.getCurrentTarget()));
			}
		}
	}

	/**
	 * FOV is determined initially in EntityPlayerSP; fov is recalculated for
	 * the vanilla bow only in the case that zoom-enhancing gear is worn
	 */
	@ForgeSubscribe
	public void updateFOV(FOVUpdateEvent event) {
		ItemStack stack = (event.entity.isUsingItem() ? event.entity.getItemInUse() : null);
		if (stack != null) {
			boolean flag = stack.getItem() instanceof IZoom;
			if (flag || stack.getItem() == Item.bow) {
				float magnify = 1.0F;
				for (ItemStack armor : event.entity.inventory.armorInventory) {
					if (armor != null && armor.getItem() instanceof IZoomHelper) {
						magnify += ((IZoomHelper) armor.getItem()).getMagnificationFactor();
					}
				}
				if (flag || magnify != 1.0F) {
					float maxTime = (flag ? ((IZoom) stack.getItem()).getMaxZoomTime() : 20.0F);
					float factor = (flag ? ((IZoom) stack.getItem()).getZoomFactor() : 0.15F);
					float charge = (float) event.entity.getItemInUseDuration() / maxTime;
					AttributeInstance attributeinstance = event.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
					float fov = (event.entity.capabilities.isFlying ? 1.1F : 1.0F);
					fov *= (attributeinstance.getAttributeValue() / (double) event.entity.capabilities.getWalkSpeed() + 1.0D) / 2.0D;
					if (event.entity.capabilities.getWalkSpeed() == 0.0F || Float.isNaN(fov) || Float.isInfinite(fov)) {
						fov = 1.0F;
					}
					if (charge > 1.0F) {
						charge = 1.0F;
					} else {
						charge *= charge;
					}
					event.newfov = fov * (1.0F - charge * factor * magnify);
				}
			}
		}
	}

	/**
	 * Handles mouse clicks for skills, canceling where appropriate; note that left click will
	 * ALWAYS be canceled, as the attack is passed to {@link #performComboAttack(Minecraft, ILockOnTarget) performComboAttack};
	 * allowing left click results in the attack processing twice, doubling durability damage to weapons
	 * no button clicked -1, left button 0, right click 1, middle click 2, possibly 3+ for other buttons
	 * NOTE: Corresponding key codes for the mouse in Minecraft are (event.button -100)
	 */
	@ForgeSubscribe
	public void onMouseChanged(MouseEvent event) {
		mouseKey = event.button - 100;
		isAttackKey = (mouseKey == mc.gameSettings.keyBindAttack.keyCode);
		isUseKey = (mouseKey == mc.gameSettings.keyBindUseItem.keyCode);
		if (event.button == -1 && event.dwheel == 0) {
			return;
		} else if ((!isAttackKey && !isUseKey)) {
			if (event.buttonstate) { // pass mouse clicks to custom key handler when pressed
				ZSSKeyHandler.onKeyPressed(mc, getKeyBindFromCode(mouseKey));
			} else {
				ZSSKeyHandler.onKeyReleased(mc, getKeyBindFromCode(mouseKey));
			}
			return;
		}
		EntityPlayer player = mc.thePlayer;
		ZSSPlayerInfo skills = ZSSPlayerInfo.get(player);
		// check pre-conditions for attacking and item use (not stunned, etc.):
		if (event.buttonstate || event.dwheel != 0) {
			if (skills.isSkillActive(SkillBase.mortalDraw)) {
				event.setCanceled(true);
			} else if (isAttackKey) {
				Item heldItem = (player.getHeldItem() != null ? player.getHeldItem().getItem() : null);
				event.setCanceled(ZSSEntityInfo.get(player).isBuffActive(Buff.STUN) || heldItem instanceof ItemHeldBlock ||
						(player.attackTime > 0 && (Config.affectAllSwings() || heldItem instanceof ISwingSpeed)));
			} else if (isUseKey) {
				event.setCanceled(ZSSEntityInfo.get(player).isBuffActive(Buff.STUN));
			}
		} else if (!event.buttonstate && isAttackKey) {
			if (skills.hasSkill(SkillBase.armorBreak)) {
				((ArmorBreak) skills.getPlayerSkill(SkillBase.armorBreak)).keyPressed(player, false);
			}
		}
		if (event.isCanceled()) {
			return;
		}
		ILockOnTarget skill = ZSSPlayerInfo.get(player).getTargetingSkill();
		if (skill != null && skill.isLockedOn() && !skills.isNayruActive()) {
			if (isAttackKey && event.buttonstate) {
				if (!skills.canInteract()) {
					if (skills.isSkillActive(SkillBase.spinAttack)) {
						((SpinAttack) skills.getPlayerSkill(SkillBase.spinAttack)).keyPressed(mc.gameSettings.keyBindAttack, mc.thePlayer);
					}
					event.setCanceled(true);
					return;
				}
				// specific held item and other requirements are checked in skill's canExecute method
				if (Config.allowVanillaControls() && player.getHeldItem() != null) {
					if (skills.shouldSkillActivate(SkillBase.dash)) {
						PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.dash).makePacket());
					} else if (skills.shouldSkillActivate(SkillBase.risingCut)) {
						PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.risingCut).makePacket());
						performComboAttack(mc, skill);
					} else if (skills.shouldSkillActivate(SkillBase.swordBeam)) {
						PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.swordBeam).makePacket());
					} else if (skills.shouldSkillActivate(SkillBase.endingBlow)) {
						PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.endingBlow).makePacket());
						performComboAttack(mc, skill);
					} else {
						performComboAttack(mc, skill);
					}
					// handle separately so can attack and begin charging without pressing key twice
					if (skills.hasSkill(SkillBase.armorBreak)) {
						((ArmorBreak) skills.getPlayerSkill(SkillBase.armorBreak)).keyPressed(player, true);
					}
				} else if (skills.shouldSkillActivate(SkillBase.mortalDraw)) {
					PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.mortalDraw).makePacket());
				} else { // Vanilla controls not enabled simply attacks; handles possibility of being ICombo
					performComboAttack(mc, skill);
				}

				// always cancel left click to prevent weapons taking double durability damage
				event.setCanceled(true);
			} else if (isUseKey && Config.allowVanillaControls()) {
				if (!skills.canInteract() && event.buttonstate) {
					event.setCanceled(true);
				}
			}
		} else { // not locked on to a target, normal item swing
			if (isAttackKey && event.buttonstate) {
				ZSSCombatEvents.setPlayerAttackTime(player);
			}
		}
	}

	@ForgeSubscribe
	public void onRenderPlayer(RenderPlayerEvent.Pre event) {
		ItemStack mask = event.entityPlayer.getCurrentArmor(ArmorIndex.WORN_HELM);
		if (mask != null && mask.getItem() == ZSSItems.maskGiants) {
			GL11.glPushMatrix();
			needsPop = true;
			// TODO generalize transformations based on player's current height rather
			// than on the mask worn; may need to flag this in extended properties to
			// prevent possible conflicts with other mods
			if (event.entityPlayer == mc.thePlayer) {
				if (mc.inGameHasFocus) {
					GL11.glTranslatef(0.0F, -6.325F, 0.0F);
					GL11.glScalef(3.0F, 3.0F, 3.0F);
				}
			} else {
				GL11.glScalef(3.0F, 3.0F, 3.0F);
			}
		}
	}

	@ForgeSubscribe
	public void onRenderPlayer(RenderPlayerEvent.Post event) {
		if (needsPop) {
			GL11.glPopMatrix();
			needsPop = false;
		}
	}

	@ForgeSubscribe
	public void onLoadSound(SoundLoadEvent event) {
		// the following sounds have only 1 file each
		event.manager.addSound(Sounds.BOMB_WHISTLE + ".ogg");
		event.manager.addSound(Sounds.BOSS_BATTLE + ".ogg");
		event.manager.addSound(Sounds.BOSS_SPAWN + ".ogg");
		event.manager.addSound(Sounds.CASH_SALE + ".ogg");
		event.manager.addSound(Sounds.HOOKSHOT + ".ogg");
		event.manager.addSound(Sounds.CHU_MERGE + ".ogg");
		event.manager.addSound(Sounds.CORK + ".ogg");
		event.manager.addSound(Sounds.FAIRY_BLESSING + ".ogg");
		event.manager.addSound(Sounds.FAIRY_LAUGH + ".ogg");
		event.manager.addSound(Sounds.FAIRY_LIVING + ".ogg");
		event.manager.addSound(Sounds.FAIRY_SKILL + ".ogg");
		event.manager.addSound(Sounds.FLAME_ABSORB + ".ogg");
		event.manager.addSound(Sounds.LEVELUP + ".ogg");
		event.manager.addSound(Sounds.LOCK_CHEST + ".ogg");
		event.manager.addSound(Sounds.LOCK_DOOR + ".ogg");
		event.manager.addSound(Sounds.LOCK_RATTLE + ".ogg");
		event.manager.addSound(Sounds.MAGIC_FAIL + ".ogg");
		event.manager.addSound(Sounds.MAGIC_FIRE + ".ogg");
		event.manager.addSound(Sounds.MAGIC_ICE + ".ogg");
		event.manager.addSound(Sounds.MASTER_SWORD + ".ogg");
		event.manager.addSound(Sounds.SECRET_MEDLEY + ".ogg");
		event.manager.addSound(Sounds.SPECIAL_DROP + ".ogg");
		event.manager.addSound(Sounds.SUCCESS + ".ogg");
		event.manager.addSound(Sounds.WEB_SPLAT + ".ogg");
		event.manager.addSound(Sounds.WHOOSH + ".ogg");

		// the following have 2
		for (int i = 1; i < 3; ++i) {
			event.manager.addSound(Sounds.MORTAL_DRAW + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.ROCK_FALL + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.WHIRLWIND + String.valueOf(i) + ".ogg");
		}

		// the following have 3
		for (int i = 1; i < 4; ++i) {
			event.manager.addSound(Sounds.ARMOR_BREAK + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.GRUNT + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HAMMER + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HIT_RUSTY + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HURT_FLESH + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.LEAPING_BLOW + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SLAM + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SPIN_ATTACK + String.valueOf(i) + ".ogg");
		}
		// 4 files each
		for (int i = 1; i < 5; ++i) {
			event.manager.addSound(Sounds.BREAK_JAR + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.HIT_PEG + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SHOCK + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SWORD_CUT + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SWORD_MISS + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.SWORD_STRIKE + String.valueOf(i) + ".ogg");
		}
	}
}
