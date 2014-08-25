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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	private int breakTimer = 0;

	/** Only for vanilla activation: Current number of ticks remaining before skill will not activate */
	@SideOnly(Side.CLIENT)
	private int ticksTilFail;

	/** Notification to play miss sound; set to true when activated and false when attack parried */
	private boolean playMissSound;

	public SwordBreak(String name) {
		super(name);
		setDisablesLMB();
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
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && PlayerUtils.isHoldingSkillItem(player);
	}

	@Override
	protected float getExhaustion() {
		return 2.0F - (0.1F * level);
	}

	@Override
	public boolean activate(World world, EntityPlayer player) {
		if (super.activate(world, player)) {
			breakTimer = getActiveTime();
			playMissSound = true;
			if (world.isRemote) {
				Minecraft.getMinecraft().gameSettings.keyBindUseItem.pressed = false;
				ZSSKeyHandler.keys[ZSSKeyHandler.KEY_BLOCK].pressed = false;
				player.swingItem();
			}
		}

		return isActive();
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			if (--breakTimer <= getUseDelay() && playMissSound) {
				playMissSound = false;
				WorldUtils.playSoundAtEntity(player.worldObj, player, Sounds.SWORD_MISS, 0.4F, 0.5F);
			}
		} else if (player.worldObj.isRemote && ticksTilFail > 0) {
			if (!Config.requiresDoubleTap() && !Minecraft.getMinecraft().gameSettings.keyBindBack.pressed) {
				PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(this).makePacket());
				ticksTilFail = 0;
			} else {
				--ticksTilFail;
			}
		}
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

	/**
	 * Sets the key pressed and starts the key timer;
	 * only used for vanilla control scheme to prevent activation on movement
	 */
	@SideOnly(Side.CLIENT)
	public void keyPressed(EntityPlayer player) {
		if (!isActive()) {
			if (Config.requiresDoubleTap() && ticksTilFail > 0) {
				PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(this).makePacket());
				ticksTilFail = 0;
			} else {
				ticksTilFail = (Config.requiresDoubleTap() ? 6 : 3);
			}
		}
	}

	/**
	 * Attempts to block the incoming attack, damaging the attacker's weapon
	 * @return true if the attack was blocked and the attack event should be canceled
	 */
	public boolean breakAttack(EntityPlayer player, EntityLivingBase attacker) {
		ItemStack stackToDamage = attacker.getHeldItem();
		if (breakTimer > getUseDelay() && stackToDamage != null && PlayerUtils.isHoldingSkillItem(player)) {
			breakTimer = getUseDelay(); // only block one attack
			WorldUtils.playSoundAtEntity(player.worldObj, player, Sounds.SWORD_STRIKE, 0.4F, 0.5F);
			playMissSound = false;
			int dmg = Math.max(getMaxDamage() / 3, player.worldObj.rand.nextInt(getMaxDamage()));
			stackToDamage.damageItem(dmg, attacker);
			if (stackToDamage.stackSize <= 0) {
				player.worldObj.playSoundAtEntity(attacker, Sounds.ITEM_BREAK, 0.8F, 0.8F + player.worldObj.rand.nextFloat() * 0.4F);
				attacker.setCurrentItemOrArmor(0, null);
			}
			TargetUtils.knockTargetBack(attacker, player);
			return true;
		}
		return false;
	}
}
