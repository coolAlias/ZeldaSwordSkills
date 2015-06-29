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

package zeldaswordskills.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.api.SongAPI;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.api.item.IZoomHelper;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.handler.ZSSCombatEvents;
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.util.TargetUtils;
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
	 * Handles mouse clicks for skills, canceling where appropriate; note that while the player
	 * is locked on with a targeting skill, keyBindAttack will ALWAYS be canceled, as the attack
	 * is passed to {@link #performComboAttack}; if the event were left uncanceled, the attack
	 * would process again in the vanilla system, doubling durability damage to weapons
	 * @buttons no button clicked -1, left button 0, right click 1, middle click 2, possibly 3+ for other buttons
	 * @notes Corresponding key codes for the mouse in Minecraft are (event.button -100)
	 */
	@ForgeSubscribe
	public void onMouseChanged(MouseEvent event) {
		mouseKey = event.button - 100;
		isAttackKey = (mouseKey == mc.gameSettings.keyBindAttack.keyCode);
		isUseKey = (mouseKey == mc.gameSettings.keyBindUseItem.keyCode);
		// no wheel, no button: just moving the mouse around
		// no wheel, unchecked key bound to mouse: passed automatically to the key handler
		if (event.dwheel == 0 && (event.button == -1 || (!isAttackKey && !isUseKey))) {
			return;
		}
		ZSSPlayerSkills skills = ZSSPlayerSkills.get(mc.thePlayer);
		// check pre-conditions for attacking and item use (not stunned, etc.):
		if (event.buttonstate || event.dwheel != 0) {
			if (isAttackKey) {
				// hack for spin attack: allows key press information to be received while animating
				if (skills.isSkillActive(SkillBase.spinAttack) && skills.getActiveSkill(SkillBase.spinAttack).isAnimating()) {
					skills.getActiveSkill(SkillBase.spinAttack).keyPressed(mc, mc.gameSettings.keyBindAttack, mc.thePlayer);
					event.setCanceled(true);
				} else if (skills.isSkillActive(SkillBase.backSlice) && skills.getActiveSkill(SkillBase.backSlice).isAnimating()) {
					skills.getActiveSkill(SkillBase.backSlice).keyPressed(mc, mc.gameSettings.keyBindAttack, mc.thePlayer);
					event.setCanceled(true);
				} else if (!skills.canInteract() || ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.STUN)) {
					event.setCanceled(true);
				} else {
					Item heldItem = (mc.thePlayer.getHeldItem() != null ? mc.thePlayer.getHeldItem().getItem() : null);
					event.setCanceled(heldItem instanceof ItemHeldBlock || (mc.thePlayer.attackTime > 0 && (Config.affectAllSwings() || heldItem instanceof ISwingSpeed)));
				}
			} else if (isUseKey) {
				event.setCanceled(!skills.canInteract() || ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.STUN));
			} else { // cancel mouse wheel while animations are in progress
				event.setCanceled(!skills.canInteract());
			}
		}
		if (event.isCanceled() || !event.buttonstate) {
			return;
		}
		ILockOnTarget skill = skills.getTargetingSkill();
		if (skill != null && skill.isLockedOn() && !skills.isNayruActive()) {
			if (isAttackKey) {
				// mouse attack will always be canceled while locked on, as the click has been handled
				if (Config.allowVanillaControls()) {
					if (!skills.onKeyPressed(mc, mc.gameSettings.keyBindAttack)) {
						// no skill activated - perform a 'standard' attack
						performComboAttack(mc, skill);
					}
					// hack for Armor Break: allows charging to begin without having to press attack key a second time
					if (skills.hasSkill(SkillBase.armorBreak)) {
						skills.getActiveSkill(SkillBase.armorBreak).keyPressed(mc, mc.gameSettings.keyBindAttack, mc.thePlayer);
					}
				}
				// if vanilla controls not enabled, mouse click is ignored (i.e. canceled)
				// if vanilla controls enabled, mouse click was already handled - cancel
				event.setCanceled(true);
			} else if (isUseKey && Config.allowVanillaControls()) {
				if (!skills.canInteract()) {
					event.setCanceled(true);
				}
			}
		} else if (isAttackKey) { // not locked on to a target, normal item swing
			ZSSCombatEvents.setPlayerAttackTime(mc.thePlayer);
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
		// Register all songs as records (i.e. streaming)
		for (AbstractZeldaSong song : SongAPI.getRegisteredSongs()) {
			event.manager.addStreaming(song.getSoundString() + ".ogg");
		}

		// the following sounds have only 1 file each
		event.manager.addSound(Sounds.BOMB_WHISTLE + ".ogg");
		event.manager.addSound(Sounds.BOSS_BATTLE + ".ogg");
		event.manager.addSound(Sounds.BOSS_SPAWN + ".ogg");
		event.manager.addSound(Sounds.CASH_SALE + ".ogg");
		event.manager.addSound(Sounds.HOOKSHOT + ".ogg");
		event.manager.addSound(Sounds.CHU_MERGE + ".ogg");
		event.manager.addSound(Sounds.CORK + ".ogg");
		event.manager.addSound(Sounds.DARKNUT_DIE + ".ogg");
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
		event.manager.addSound(Sounds.OCARINA + ".ogg");
		event.manager.addSound(Sounds.SECRET_MEDLEY + ".ogg");
		event.manager.addSound(Sounds.SPECIAL_DROP + ".ogg");
		event.manager.addSound(Sounds.SUCCESS + ".ogg");
		event.manager.addSound(Sounds.WEB_SPLAT + ".ogg");
		event.manager.addSound(Sounds.WHOOSH + ".ogg");
		event.manager.addSound(Sounds.WHIP + ".ogg");

		// the following have 2
		for (int i = 1; i < 3; ++i) {
			event.manager.addSound(Sounds.DARKNUT_HIT + String.valueOf(i) + ".ogg");
			event.manager.addSound(Sounds.DARKNUT_LIVING + String.valueOf(i) + ".ogg");
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
			event.manager.addSound(Sounds.WHIP_CRACK + String.valueOf(i) + ".ogg");
		}
	}
}
