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
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * DODGE
 * Description: Avoid damage by quickly dodging out of the way
 * Activation: Tap left or right arrow key to dodge in that direction
 * Exhaustion: 0.05F
 * Duration: (5 + level) ticks; this is the amount of time during which damage may be avoided
 * Special: - May only be used while locked on to a target
 * 			- Chance to avoid damage is 10% per level, plus a timing bonus of up to 20%
 * 
 * Tap 'left' or 'right' arrow key to dodge out of harm's way (activated when key released);
 * can be configured to require double-tap and / or to allow use of default movement keys ('a'
 * and 'd') for activation in addition to arrow keys.
 * 
 * While dodging, there is a chance to avoid any incoming attacks.
 * 
 */
public class Dodge extends SkillActive
{
	/** Current number of ticks remaining before dodge will not activate */
	@SideOnly(Side.CLIENT)
	private int ticksTilFail;

	/** Timer during which player may evade incoming attacks */
	private int dodgeTimer = 0;

	/** Key that was pressed to initiate dodge */
	@SideOnly(Side.CLIENT)
	private KeyBinding keyPressed;

	public Dodge(String name) {
		super(name);
		setDisablesLMB();
	}

	private Dodge(Dodge skill) {
		super(skill);
	}

	@Override
	public Dodge newInstance() {
		return new Dodge(this);
	}

	@Override
	public boolean isActive() {
		return (dodgeTimer > 0);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(List<String> desc, EntityPlayer player) {
		desc.add(StatCollector.translateToLocalFormatted(getInfoString("info", 1),
				(int)(getBaseDodgeChance(player) * 100)));
		desc.add(StatCollector.translateToLocalFormatted(getInfoString("info", 2),
				(getDodgeTime() + level - 5) * 2)); // don't use real time bonus, since timer is zero
		desc.add(getTimeLimitDisplay(getDodgeTime()));
		desc.add(getExhaustionDisplay(getExhaustion()));
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && ZSSPlayerInfo.get(player).isSkillActive(swordBasic);
	}
	
	@Override
	protected float getExhaustion() {
		return 0.05F;
	}

	/**
	 * Doesn't send packet back to client as it's too slow; client activated directly
	 */
	@Override
	public boolean activate(World world, EntityPlayer player) {
		if (canUse(player)) {
			ZSSPlayerInfo.get(player).setCurrentActiveSkill(this);
			dodgeTimer = getDodgeTime();
			if (!player.capabilities.isCreativeMode) {
				player.addExhaustion(getExhaustion());
			}
		}
		return isActive();
	}

	/** Amount of time dodge will remain active */
	private int getDodgeTime() {
		return (5 + level);
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			--dodgeTimer;
		} else if (player.worldObj.isRemote && ticksTilFail > 0) {
			if (!Config.requiresDoubleTap() && !keyPressed.pressed) {
				activate(player.worldObj, player);
				PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(this).makePacket());
				ticksTilFail = 0;
			} else {
				--ticksTilFail;
			}
		}
	}

	/** Returns player's base chance to successfully evade an attack, including bonuses from buffs */
	private float getBaseDodgeChance(EntityPlayer player) {
		float evadeUp = ZSSEntityInfo.get(player).getBuffAmplifier(Buff.EVADE_UP) * 0.01F;
		float evadeDown = ZSSEntityInfo.get(player).getBuffAmplifier(Buff.EVADE_DOWN) * 0.01F;
		return ((level * 0.1F) + evadeUp - evadeDown);
	}

	/** Returns timing evasion bonus */
	private float getTimeBonus() {
		return ((dodgeTimer + level - 5) * 0.02F);
	}

	/** Returns full chance to dodge an attack, including all bonuses */
	private float getDodgeChance(EntityPlayer player) {
		return getBaseDodgeChance(player) + getTimeBonus();
	}

	/**
	 * Returns true if the attack was dodged and the attack event should be canceled
	 */
	public boolean dodgeAttack(EntityPlayer player) {
		if (player.worldObj.rand.nextFloat() < getDodgeChance(player)) {
			PlayerUtils.playRandomizedSound(player, Sounds.SWORD_MISS, 0.4F, 0.5F);
			return true;
		}
		return false;
	}

	/**
	 * Sets the key pressed and starts the key timer
	 */
	@SideOnly(Side.CLIENT)
	public void keyPressed(KeyBinding key, EntityPlayer player) {
		if (!isActive()) {
			if (Config.requiresDoubleTap() && ticksTilFail > 0 && key == keyPressed) {
				activate(player.worldObj, player);
				PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(this).makePacket());
				ticksTilFail = 0;
			} else {
				keyPressed = key;
				ticksTilFail = (Config.requiresDoubleTap() ? 6 : 3);
			}
		}
	}

	/**
	 * Updates the dodge animation each render tick
	 * @return returns true if animation is in progress
	 */
	@SideOnly(Side.CLIENT)
	public boolean onRenderTick(EntityPlayer player) {
		if (dodgeTimer > level) {
			double d = 0.05D;
			if (player.getCurrentArmor(ArmorIndex.WORN_BOOTS) != null && player.getCurrentArmor(ArmorIndex.WORN_BOOTS).getItem() == ZSSItems.bootsHeavy) {
				d = 0.01D;
			}
			Vec3 vec3 = player.getLookVec();
			if (keyPressed == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT] || (Config.allowVanillaControls()
					&& keyPressed == Minecraft.getMinecraft().gameSettings.keyBindRight)) {
				player.addVelocity(-vec3.zCoord * d, 0.0D, vec3.xCoord * d);
			} else {
				player.addVelocity(vec3.zCoord * d, 0.0D, -vec3.xCoord * d);
			}
			return true;
		}
		return false;
	}
}
