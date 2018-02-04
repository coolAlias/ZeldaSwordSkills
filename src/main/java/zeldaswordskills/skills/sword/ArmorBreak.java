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

package zeldaswordskills.skills.sword;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.DirtyEntityAccessor;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * ARMOR BREAK
 * Description: Unleash a powerful blow that ignores armor
 * Activation: Hold attack for (20 - level) ticks
 * Effect: Unleashes an attack that inflicts normal weapon damage but ignores armor
 * Exhaustion: 2.0F - (0.1F * level)
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

	/**
	 * Flags whether the vanilla keyBindAttack was used to trigger this skill, in which
	 * case the keybinding state must be manually set to false once the skill activates;
	 * this is because the key is still pressed, and vanilla behavior is to attack like
	 * crazy as long as the key is held, which is not very cool. For custom key bindings
	 * this is not an issue, as it only results in an attack when the key is first pressed.
	 * 
	 * Another issue: while mouse state is true, if the cursor moves over a block, the player
	 * will furiously swing his arm at it, as though trying to break it. Perhaps it is better
	 * to set the key state to false as before and track 'buttonstate' from within the skill,
	 * though in that case it needs to listen for key releases as well as presses.
	 * Note that this is the default vanilla behavior for holding down the attack key, so
	 * perhaps it is best to leave it as is.
	 */
	private boolean requiresReset;

	public ArmorBreak(String name) {
		super(name);
	}

	private ArmorBreak(ArmorBreak skill) {
		super(skill);
	}

	@Override
	public ArmorBreak newInstance() {
		return new ArmorBreak(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(List<String> desc, EntityPlayer player) {
		desc.add(getChargeDisplay(getChargeTime(player)));
		desc.add(getExhaustionDisplay(getExhaustion()));
	}

	@Override
	protected boolean allowUserActivation() {
		return false;
	}

	@Override
	public boolean isActive() {
		return activeTimer > 0;
	}

	@Override
	protected float getExhaustion() {
		return 2.0F - (0.1F * level);
	}

	/** Returns number of ticks required before attack will execute: 20 - level */
	private int getChargeTime(EntityPlayer player) {
		return (PlayerUtils.isHoldingMasterSword(player) ? 15 : 20) - level;
	}

	/** Returns true if the skill is still charging up; always false on the server, as charge is handled client-side */
	public boolean isCharging(EntityPlayer player) {
		ILockOnTarget target = ZSSPlayerSkills.get(player).getTargetingSkill();
		return charge > 0 && target != null && target.isLockedOn();
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && PlayerUtils.isWeapon(player.getHeldItem());
	}

	/**
	 * ArmorBreak does not listen for any keys so that there is no chance it is bypassed by
	 * another skill processing first; instead, keyPressed must be called manually, both
	 * when the attack key is pressed (and, to handle mouse clicks, when released)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isKeyListener(Minecraft mc, KeyBinding key) {
		return false;
	}

	/**
	 * Must be called manually when the attack key is pressed (and, for the mouse, when released);
	 * this is necessary to allow charging to start from a single key press, when other skills
	 * might otherwise preclude ArmorBreak's keyPressed from being called.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean keyPressed(Minecraft mc, KeyBinding key, EntityPlayer player) {
		requiresReset = (key == mc.gameSettings.keyBindAttack);
		if (requiresReset || key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK]) {
			charge = getChargeTime(player);
			if (requiresReset) {
				// manually set the keybind state, since it will not be set by the canceled mouse event
				// releasing the mouse unsets it normally, but it must be manually unset if the skill is triggered
				KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
			}
			return true; // doesn't matter, as ArmorBreak is handled outside the normal framework
		}
		return false;
	}

	/**
	 * Returns true if the attack key is still pressed (i.e. ArmorBreak should continue to charge)
	 */
	@SideOnly(Side.CLIENT)
	public boolean isKeyPressed() {
		return (ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK].isKeyDown() || (Config.allowVanillaControls && Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown()));
	}

	/**
	 * ArmorBreak's activation was triggered from the client side and it will be over
	 * on the server by the time the client receives the packet, so don't bother
	 */
	@Override
	protected boolean sendClientUpdate() {
		return true;
	}

	@Override
	protected boolean onActivated(World world, EntityPlayer player) {
		activeTimer = 1; // needs to be active for hurt event to process correctly
		if (world.isRemote) {
			player.swingItem();
		} else {
			// Attack first so skill still active upon impact, then set timer to zero
			ILockOnTarget skill = ZSSPlayerSkills.get(player).getTargetingSkill();
			if (skill != null && skill.isLockedOn() && TargetUtils.canReachTarget(player, skill.getCurrentTarget())) {
				player.attackTargetEntityWithCurrentItem(skill.getCurrentTarget());
			}
		}
		// Armor Break is never added to the list of active skills, since the skill is
		// typically on longer active after attacking; luckily, it doesn't need to be
		return false;
	}

	@Override
	protected void onDeactivated(World world, EntityPlayer player) {
		activeTimer = 0;
		charge = 0;
		ZSSPlayerInfo.get(player).armSwing = 0.0F;
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isCharging(player)) {
			if (isKeyPressed() && PlayerUtils.isWeapon(player.getHeldItem())) {
				if (!player.isSwingInProgress) {
					int maxCharge = getChargeTime(player);
					if (charge < maxCharge - 1) {
						ZSSPlayerInfo.get(player).armSwing = 0.25F + 0.75F * ((float)(maxCharge - charge) / (float) maxCharge);
					}
					--charge;
				}
				// ArmorBreak triggers here, on the client side first, so onActivated need not process on the client
				if (charge == 0) {
					ZSSPlayerInfo.get(player).armSwing = 0.0F;
					// can't use the standard animation methods to prevent key/mouse input,
					// since Armor Break will not return true for isActive
					ZSSPlayerInfo.get(player).setAttackTime(4); // flag for isAnimating? no player parameter; // flag for isAnimating? no player parameter
					player.swingItem();
					if (requiresReset) { // activated by vanilla attack key: manually unset the key state (fix for mouse event issues)
						KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
					}
					SwordBasic skill = (SwordBasic) ZSSPlayerSkills.get(player).getPlayerSkill(swordBasic);
					if (skill != null && skill.onAttack(player)) {
						PacketDispatcher.sendToServer(new ActivateSkillPacket(this, true));
					} else { // player missed - swing arm manually since no activation packet will be sent
						player.swingItem();
					}
				}
			} else {
				ZSSPlayerInfo.get(player).armSwing = 0.0F;
				charge = 0;
			}
		} else if (charge > 0) {
			ZSSPlayerInfo.get(player).armSwing = 0.0F;
			charge = 0;
		}
		if (isActive()) {
			activeTimer = 0;
		}
	}

	/**
	 * Deactivates this skill and inflicts armor-ignoring damage directly to the
	 * target; note that this causes the LivingHurtEvent to repost, but since the
	 * skill is no longer active it will behave normally. The current event's
	 * damage is set to zero to avoid double damage.
	 */
	public void onImpact(EntityPlayer player, LivingHurtEvent event) {
		activeTimer = 0;
		WorldUtils.playSoundAtEntity(player, Sounds.ARMOR_BREAK, 0.4F, 0.5F);
		DirtyEntityAccessor.damageEntity(event.entityLiving, DamageUtils.causeArmorBreakDamage(player), event.ammount);
		event.ammount = 0.0F;
	}
}
