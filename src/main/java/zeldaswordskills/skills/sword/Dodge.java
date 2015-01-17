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
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
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
	/** Key that was pressed to initiate dodge */
	@SideOnly(Side.CLIENT)
	private KeyBinding keyPressed;

	/** Current number of ticks remaining before dodge will not activate */
	@SideOnly(Side.CLIENT)
	private int ticksTilFail;

	/** Timer during which player may evade incoming attacks */
	private int dodgeTimer = 0;

	/** Entity dodged, since the attack event may fire multiple times in quick succession for mobs like zombies */
	// TODO make a List<Entity>, for dodging multiple entities; lower dodge chance as list grows in size
	// TODO perhaps limit the size of the list by skill level
	private Entity entityDodged;

	public Dodge(String name) {
		super(name);
	}

	private Dodge(Dodge skill) {
		super(skill);
	}

	@Override
	public Dodge newInstance() {
		return new Dodge(this);
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

	/**
	 * Prevents Dodge from being activated in quick succession, but does not prevent
	 * other skills from being activated once Dodge has finished animating
	 */
	@Override
	public boolean isActive() {
		return (dodgeTimer > 0);
	}

	@Override
	protected float getExhaustion() {
		return 0.05F;
	}

	/** Returns player's base chance to successfully evade an attack, including bonuses from buffs */
	private float getBaseDodgeChance(EntityPlayer player) {
		float evadeUp = ZSSEntityInfo.get(player).getBuffAmplifier(Buff.EVADE_UP) * 0.01F;
		float evadeDown = ZSSEntityInfo.get(player).getBuffAmplifier(Buff.EVADE_DOWN) * 0.01F;
		float speedBonus = 2.0F * (float)(player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed).getAttributeValue() - Dash.BASE_MOVE);
		return ((level * 0.1F) + (evadeUp - evadeDown) + speedBonus);
	}

	/** Returns full chance to dodge an attack, including all bonuses */
	private float getDodgeChance(EntityPlayer player) {
		return getBaseDodgeChance(player) + getTimeBonus();
	}

	/** Amount of time dodge will remain active */
	private int getDodgeTime() {
		return (5 + level);
	}

	/** Returns timing evasion bonus */
	private float getTimeBonus() {
		return ((dodgeTimer + level - 5) * 0.02F);
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && ZSSPlayerSkills.get(player).isSkillActive(swordBasic);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canExecute(EntityPlayer player) {
		return player.onGround && canUse(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isKeyListener(Minecraft mc, KeyBinding key) {
		return ((Config.allowVanillaControls() && (key == mc.gameSettings.keyBindLeft || key == mc.gameSettings.keyBindRight)) ||
				key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_LEFT] || key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean keyPressed(Minecraft mc, KeyBinding key, EntityPlayer player) {
		if (canExecute(player)) {
			if (Config.requiresDoubleTap()) {
				if (ticksTilFail > 0 && key == keyPressed) {
					PacketDispatcher.sendToServer(new ActivateSkillPacket(this));
					ticksTilFail = 0;
					return true;
				} else {
					keyPressed = key;
					ticksTilFail = 6;
				}
				// Single-tap activation only allowed using custom key bindings:
			} else if (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_LEFT] || key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT]) {
				PacketDispatcher.sendToServer(new ActivateSkillPacket(this));
				return true;
			}
		}
		return false; // allow other skills to receive this key press (e.g. Spin Attack)
	}

	@Override
	public boolean onActivated(World world, EntityPlayer player) {
		dodgeTimer = getDodgeTime();
		entityDodged = null;
		return isActive();
	}

	@Override
	protected void onDeactivated(World world, EntityPlayer player) {
		dodgeTimer = 0;
		entityDodged = null;
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			--dodgeTimer;
		} else if (player.worldObj.isRemote && ticksTilFail > 0) {
			--ticksTilFail;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isAnimating() {
		return (dodgeTimer > level);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean onRenderTick(EntityPlayer player, float partialTickTime) {
		double speed = 1.0D + 10.0D * (player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed).getAttributeValue() - Dash.BASE_MOVE);
		if (speed > 1.0D) {
			speed = 1.0D;
		}
		double d = 0.15D * speed * speed;
		Vec3 vec3 = player.getLookVec();
		if (keyPressed == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT] || keyPressed == Minecraft.getMinecraft().gameSettings.keyBindRight) {
			player.addVelocity(-vec3.zCoord * d, 0.0D, vec3.xCoord * d);
		} else {
			player.addVelocity(vec3.zCoord * d, 0.0D, -vec3.xCoord * d);
		}
		return true;
	}

	@Override
	public boolean onBeingAttacked(EntityPlayer player, DamageSource source) {
		if (dodgeTimer > level) { // still able to dodge (used to use isActive(), but changed for animating)
			Entity attacker = source.getEntity();
			if (attacker != null) {
				return (attacker == entityDodged || dodgeAttack(player, attacker));
			}
		}
		return false;
	}

	/**
	 * Returns true if the attack was dodged and the attack event should be canceled
	 */
	private boolean dodgeAttack(EntityPlayer player, Entity attacker) {
		if (player.worldObj.rand.nextFloat() < getDodgeChance(player)) {
			entityDodged = attacker;
			PlayerUtils.playRandomizedSound(player, Sounds.SWORD_MISS, 0.4F, 0.5F);
			return true;
		}
		return false;
	}
}
