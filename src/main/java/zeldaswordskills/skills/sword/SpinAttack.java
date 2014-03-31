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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.handler.ZSSKeyHandler;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.AddExhaustionPacket;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Activated by left or right arrow; player spins in designated direction attacking
 * all enemies within 360 arc (like Link's spin attack in Zelda). With the Super Spin
 * Attack, most attributes are doubled, but it may only be used at full health.
 * 
 * Note that spinning is handled client side only, so the skill doesn't need to be activated on
 * the server. The server only needs to be notified of any entities attacked.
 * 
 * Activation: Hold left or right arrow key to charge up until spin attack commences
 * Vanilla: Begin moving either left or right, then press the other direction to
 * 			commence charging; both keys must be held to continue charging
 * 			Tapping attack will continue the spin (Super Spin Attack only)
 * Arc: 360 degrees, plus an extra 360 degrees for every level of Super Spin Attack
 * Charge time: 20 ticks, minus 2 per level
 * Range: 3.0D or 6.0D, plus 0.5D per level
 * Exhaustion: 3.0F - 0.2F per level, added each spin
 *
 */
public class SpinAttack extends SkillActive
{
	/** Current charge time */
	@SideOnly(Side.CLIENT)
	private int charge;

	/** Tracks current spin progress */
	@SideOnly(Side.CLIENT)
	private float currentSpin;

	/** Set to true when activated, then back to false when spin 'animation' completed */
	private boolean isActive = false;

	/** Direction in which to spin */
	@SideOnly(Side.CLIENT)
	private boolean clockwise;

	/** Used to allow vanilla keys to determine spin direction */
	@SideOnly(Side.CLIENT)
	private boolean wasKeyPressed;

	/** Number of degrees to spin; incremented when keypressed and already active for Super Spin Attack */
	@SideOnly(Side.CLIENT)
	private float arc;

	/** Entities within range upon activation so no entity targeted more than once */
	@SideOnly(Side.CLIENT)
	private List<EntityLivingBase> targets;

	/** Number of times the spin has been 'refreshed' during this activation cycle */
	private int refreshed;

	/** Whether flame particles should render along the sword's arc */
	private boolean isFlaming;

	/** The player's Super Spin Attack level will allow multiple spins and extended range */
	private int superLevel;

	public SpinAttack(String name) {
		super(name);
		setDisablesLMB();
		addDescription(getUnlocalizedDescription(2).replace("super", ""));
	}

	private SpinAttack(SpinAttack skill) {
		super(skill);
	}

	@Override
	public SpinAttack newInstance() {
		return new SpinAttack(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getDescription(EntityPlayer player) {
		if (!isActive()) {
			superLevel = ZSSPlayerInfo.get(player).getSkillLevel(superSpinAttack);
		}
		List<String> desc = getDescription();
		desc.add(getChargeDisplay(getChargeTime()));
		desc.add(getRangeDisplay(getRange()));
		desc.add(getExhaustionDisplay(getExhaustion()));
		return desc;
	}

	@Override
	public boolean canDrop() {
		return this == spinAttack;
	}

	@Override
	public boolean isLoot() {
		return this == spinAttack;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && PlayerUtils.isHoldingSword(player)
				&& ZSSPlayerInfo.get(player).isSkillActive(swordBasic);
	}

	@Override
	protected float getExhaustion() {
		return 3.0F - (0.2F * level);
	}

	/**
	 * Activation only occurs client side, so server is never notified; this is intentional
	 */
	@Override
	public boolean activate(World world, EntityPlayer player) {
		// prevents accidental activation on server
		if (world.isRemote) {
			isActive = super.activate(world, player);
			if (isActive()) {
				refreshed = 0;
				arc = 360F;
				charge = getChargeTime();
				superLevel = (PlayerUtils.getHealthMissing(player) == 0.0F ? ZSSPlayerInfo.get(player).getSkillLevel(superSpinAttack) : 0);
				isFlaming = EnchantmentHelper.getFireAspectModifier(player) > 0;
			}
		}
		return isActive();
	}

	/**
	 * isActive() should return false on the server, so updates only on the client; this is intentional
	 */
	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive() && isCharging()) {
			if (isKeyPressed()) {
				if (charge < (getChargeTime() - 1)) {
					Minecraft.getMinecraft().playerController.sendUseItem(player, player.worldObj, player.getHeldItem());
				}
				--charge;
				if (charge == 0) {
					startSpin(player.worldObj, player);
				}
			} else {
				isActive = false;
				charge = 0;
			}
		}
	}

	/** Returns time required before spin will execute */
	@SideOnly(Side.CLIENT)
	private int getChargeTime() {
		return 20 - (level * 2);
	}

	/** Returns true if the skill is still charging up */
	@SideOnly(Side.CLIENT)
	public boolean isCharging() {
		return charge > 0;
	}

	/**
	 * Sets direction of spin and activates skill when left or right arrow key pressed
	 * or adds extra spin for Super Spin Attack when attack key pressed
	 */
	@SideOnly(Side.CLIENT)
	public void keyPressed(KeyBinding key, EntityPlayer player) {
		if (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK] || key == Minecraft.getMinecraft().gameSettings.keyBindAttack) {
			if (isActive && arc < (360F * (superLevel + 1)) && arc == (360F * refreshed)) {
				arc += 360F;
			}
		} else {
			// prevents activation of Dodge from interfering with spin direction
			if (wasKeyPressed) {
				wasKeyPressed = false;
			} else {
				clockwise = (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT] ||
						(Config.allowVanillaControls() && key == Minecraft.getMinecraft().gameSettings.keyBindRight));
				wasKeyPressed = true;
			}
			if (isKeyPressed()) {
				wasKeyPressed = false;
				activate(player.worldObj, player);
			}
		}
	}

	/** Returns true if either left or right arrow key is currently being pressed (or both in the case of vanilla controls) */
	@SideOnly(Side.CLIENT)
	private boolean isKeyPressed() {
		return ZSSKeyHandler.keys[ZSSKeyHandler.KEY_LEFT].pressed || ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT].pressed
				|| (Config.allowVanillaControls() && (Minecraft.getMinecraft().gameSettings.keyBindLeft.pressed
						&& Minecraft.getMinecraft().gameSettings.keyBindRight.pressed));
	}

	/** Max sword range for striking targets */
	@SideOnly(Side.CLIENT)
	private float getRange() {
		return (3.0F + ((superLevel + level) * 0.5F));
	}

	/** Returns the spin speed modified based on the skill's level */
	@SideOnly(Side.CLIENT)
	public float getSpinSpeed() {
		return 70 + (3 * (superLevel + level));
	}

	/** Initiates spin attack by populating the nearby target list */
	@SideOnly(Side.CLIENT)
	private void startSpin(World world, EntityPlayer player) {
		++refreshed;
		PlayerUtils.playRandomizedSound(player, Sounds.SPIN_ATTACK, 0.4F, 0.5F);
		// TODO PlayerUtils.playSound(player, Sounds.YELL, (world.rand.nextFloat() * 0.4F + 0.5F), world.rand.nextFloat() * 0.2F + 0.95F);
		PacketDispatcher.sendPacketToServer(new AddExhaustionPacket(getExhaustion()).makePacket());
		targets = world.getEntitiesWithinAABB(EntityLivingBase.class, player.boundingBox.expand(getRange(), 0.0D, getRange()));
		if (targets.contains(player)) {
			targets.remove(player);
		}
	}

	/**
	 * Increments the spin progress counter and terminates the spin once it reaches the max spin arc
	 */
	@SideOnly(Side.CLIENT)
	private void incrementSpin(EntityPlayer player) {
		player.swingProgress = 0.5F;
		player.setAngles((clockwise ? getSpinSpeed() : -getSpinSpeed()), 0);
		// 0.15D is the multiplier from Entity.setAngles
		currentSpin += getSpinSpeed() * 0.15D;
		if (currentSpin >= arc) {
			currentSpin = 0.0F;
			isActive = false;
		} else if (currentSpin > (360F * refreshed)) {
			startSpin(player.worldObj, player);
		}
	}

	/**
	 * Updates total spin motion and attacks targets in front of player
	 * @return returns true if animation is in progress
	 */
	@SideOnly(Side.CLIENT)
	public boolean onRenderTick(EntityPlayer player) {
		if (!isCharging() && PlayerUtils.isHoldingSword(player)) {
			List<EntityLivingBase> list = TargetUtils.acquireAllLookTargets(player, (int)(getRange() + 0.5F), 1.0D);
			for (EntityLivingBase target : list) {
				if (targets != null && targets.contains(target)) {
					Minecraft.getMinecraft().playerController.attackEntity(player, target);
					targets.remove(target);
				}
			}
			spawnParticles(player);
			incrementSpin(player);
			return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticles(EntityPlayer player) {
		// TODO these will not show up for other players
		String particle = (isFlaming ? "flame" : (superLevel > 0 ? "magicCrit" : "crit"));
		Vec3 vec3 = player.getLookVec();
		double posX = player.posX + (vec3.xCoord * getRange());
		double posY = player.posY + player.getEyeHeight() - 0.1D;
		double posZ = player.posZ + (vec3.zCoord * getRange());
		for (int i = 0; i < 2; ++i) {
			player.worldObj.spawnParticle(particle, posX, posY, posZ, vec3.xCoord * 0.15D, 0.01D, vec3.zCoord * 0.15D);
		}
	}
}
