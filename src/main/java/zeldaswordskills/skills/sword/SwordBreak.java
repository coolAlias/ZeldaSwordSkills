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

package zeldaswordskills.skills.sword;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * SWORD BREAK
 * Activation: Double-tap back while blocking
 * Effect: A fierce block that is capable of destroying the opponent's blade
 * Exhaustion: 2.0F - (0.1 * level)
 * Damage: Up to 90 durability damage to the opponent's held item (15 * (level + 1))
 * Duration: Time allowed before skill fails is 2 ticks at level 1, up to 8 ticks at max level
 * Notes:
 * - Only works when being attacked by an enemy holding an item
 * - Has no effect other than blocking the attack if the attacker's held item can not be damaged
 * - Must release the block key in between uses
 *
 */
public class SwordBreak extends SkillActive
{
	/** Timer during which player is considered actively parrying */
	private int breakTimer;

	/** Only for double-tap activation: Current number of ticks remaining before skill will not activate */
	@SideOnly(Side.CLIENT)
	private int ticksTilFail;

	/** Notification to play miss sound; set to true when activated and false when attack parried */
	private boolean playMissSound;

	public SwordBreak(String name) {
		super(name);
	}

	private SwordBreak(SwordBreak skill) {
		super(skill);
	}

	@Override
	public SwordBreak newInstance() {
		return new SwordBreak(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(List<String> desc, EntityPlayer player) {
		desc.add(StatCollector.translateToLocalFormatted(getInfoString("info", 1), getMaxDamage()));
		desc.add(getTimeLimitDisplay(getActiveTime() - getUseDelay()));
		desc.add(getExhaustionDisplay(getExhaustion()));
	}

	@Override
	public boolean isActive() {
		return (breakTimer > 0);
	}

	@Override
	protected float getExhaustion() {
		return 2.0F - (0.1F * level);
	}

	/** Number of ticks that skill will be considered active */
	private int getActiveTime() {
		return 6 + level;
	}

	/** Number of ticks before player may attempt to use this skill again */
	private int getUseDelay() {
		return (5 - (level / 2)); // 2 tick usage window at level 1
	}

	/** Maximum amount of damage that may be caused to the opponent's weapon */
	private int getMaxDamage() {
		return (level + 1) * 15;
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && PlayerUtils.isHoldingSkillItem(player);
	}

	/**
	 * Only allow activation if player is using item, to prevent clashing with Parry
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canExecute(EntityPlayer player) {
		return canUse(player) && PlayerUtils.isBlocking(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isKeyListener(Minecraft mc, KeyBinding key) {
		return (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_DOWN] || (Config.allowVanillaControls() && key == mc.gameSettings.keyBindBack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean keyPressed(Minecraft mc, KeyBinding key, EntityPlayer player) {
		if (canExecute(player)) {
			if (Config.requiresDoubleTap()) {
				if (ticksTilFail > 0) {
					PacketDispatcher.sendToServer(new ActivateSkillPacket(this));
					ticksTilFail = 0;
					return true;
				} else {
					ticksTilFail = 6;
				}
			} else if (key != mc.gameSettings.keyBindBack) { // activate on first press, but not for vanilla key!
				PacketDispatcher.sendToServer(new ActivateSkillPacket(this));
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean onActivated(World world, EntityPlayer player) {
		breakTimer = getActiveTime();
		playMissSound = true;
		if (world.isRemote) {
			KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), false);
			KeyBinding.setKeyBindState(ZSSKeyHandler.keys[ZSSKeyHandler.KEY_BLOCK].getKeyCode(), false);
			player.swingItem();
		}
		return isActive();
	}

	@Override
	protected void onDeactivated(World world, EntityPlayer player) {
		breakTimer = 0;
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			if (--breakTimer <= getUseDelay() && playMissSound) {
				playMissSound = false;
				WorldUtils.playSoundAtEntity(player, Sounds.SWORD_MISS, 0.4F, 0.5F);
			}
		} else if (player.worldObj.isRemote && ticksTilFail > 0) {
			--ticksTilFail;
		}
	}

	@Override
	public boolean onBeingAttacked(EntityPlayer player, DamageSource source) {
		if (source.getSourceOfDamage() instanceof EntityLivingBase) {
			EntityLivingBase attacker = (EntityLivingBase) source.getSourceOfDamage();
			ItemStack stackToDamage = attacker.getHeldItem();
			if (breakTimer > getUseDelay() && stackToDamage != null && PlayerUtils.isHoldingSkillItem(player)) {
				breakTimer = getUseDelay(); // only block one attack
				WorldUtils.playSoundAtEntity(player, Sounds.SWORD_STRIKE, 0.4F, 0.5F);
				playMissSound = false;
				if (!player.worldObj.isRemote) {
					int dmg = Math.max(getMaxDamage() / 3, player.worldObj.rand.nextInt(getMaxDamage()));
					stackToDamage.damageItem(dmg, attacker);
					if (stackToDamage.stackSize <= 0) {
						player.worldObj.playSoundAtEntity(attacker, Sounds.ITEM_BREAK, 0.8F, 0.8F + player.worldObj.rand.nextFloat() * 0.4F);
						attacker.setCurrentItemOrArmor(0, null);
					}
				}
				TargetUtils.knockTargetBack(attacker, player);
				return true;
			} // don't deactivate early, as there is a delay between uses
		}
		return false;
	}
}
