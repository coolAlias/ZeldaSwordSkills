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
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.bidirectional.ActivateSkillPacket;
import zeldaswordskills.network.packet.server.RefreshSpinPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
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
 * Range: 3.0D plus 0.5D per level each of Spin and Super Spin Attack
 * Exhaustion: 3.0F - 0.2F per level, added each spin
 *
 */
public class SpinAttack extends SkillActive
{
	/** Current charge time; only ever set on the client - server is never charging */
	private int charge;

	/** Current spin progress is incremented each tick and signals that the skill is active */
	private float currentSpin;

	/** Number of degrees to spin and used as flag for isActive(); incremented by 360F each time spin is refreshed */
	private float arc;

	/** Number of times the spin has been 'refreshed' during this activation cycle; incremented in {@link #startSpin} */
	private int refreshed;

	/** Direction in which to spin */
	@SideOnly(Side.CLIENT)
	private boolean clockwise;

	/** Used to allow vanilla keys to determine spin direction */
	@SideOnly(Side.CLIENT)
	private boolean wasKeyPressed;

	/** Entities within range upon activation so no entity targeted more than once */
	@SideOnly(Side.CLIENT)
	private List<EntityLivingBase> targets;

	/** Whether flame particles should render along the sword's arc */
	private boolean isFlaming;

	/** The player's Super Spin Attack level will allow multiple spins and extended range */
	private int superLevel;

	public SpinAttack(String name) {
		super(name);
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
	public void addInformation(List<String> desc, EntityPlayer player) {
		byte temp = level;
		if (!isActive()) {
			superLevel = (checkHealth(player) ? ZSSPlayerSkills.get(player).getSkillLevel(superSpinAttack) : 0);
			level = ZSSPlayerSkills.get(player).getSkillLevel(spinAttack);
		}
		desc.add(getChargeDisplay(getChargeTime()));
		desc.add(getRangeDisplay(getRange()));
		desc.add(StatCollector.translateToLocalFormatted(getInfoString("info", 1).replace("super", ""), superLevel + 1));
		desc.add(getExhaustionDisplay(getExhaustion()));
		level = temp;
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
		return arc > 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isAnimating() {
		return isActive() && !isCharging();
	}

	@Override
	protected float getExhaustion() {
		return 3.0F - (0.2F * level);
	}

	/** Returns time required before spin will execute */
	private int getChargeTime() {
		return 20 - (level * 2);
	}

	/** Returns true if the skill is still charging up; always false on the server */
	private boolean isCharging() {
		return charge > 0;
	}

	/** Returns true if the arc may be extended by 360 more degrees */
	private boolean canRefresh() {
		return (refreshed < (superLevel + 1) && arc == (360F * refreshed));
	}

	/** Max sword range for striking targets */
	private float getRange() {
		return (3.0F + ((superLevel + level) * 0.5F));
	}

	/** Returns the spin speed modified based on the skill's level */
	private float getSpinSpeed() {
		return 70 + (3 * (superLevel + level));
	}

	/** Returns true if players current health is within the allowed limit */
	private boolean checkHealth(EntityPlayer player) {
		return player.capabilities.isCreativeMode || PlayerUtils.getHealthMissing(player) <= Config.getHealthAllowance(level);
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canExecute(EntityPlayer player) {
		// return super.canUse instead of this.canUse to avoid !isActive() check; allows
		// canExecute to be checked when refreshing super spin attack
		return super.canUse(player) && PlayerUtils.isHoldingSkillItem(player);
	}

	/**
	 * Returns true if either left or right arrow key is currently being pressed (or both in the case of vanilla controls)
	 */
	@SideOnly(Side.CLIENT)
	private boolean isKeyPressed() {
		return ZSSKeyHandler.keys[ZSSKeyHandler.KEY_LEFT].getIsKeyPressed() || ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT].getIsKeyPressed()
				|| (Config.allowVanillaControls() && (Minecraft.getMinecraft().gameSettings.keyBindLeft.getIsKeyPressed()
						&& Minecraft.getMinecraft().gameSettings.keyBindRight.getIsKeyPressed()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isKeyListener(Minecraft mc, KeyBinding key) {
		// attack key is handled separately in order to intercept the key before it may be passed
		// to other skills; this is necessary to prevent another skill activating while spin attack is active
		return (//key == mc.gameSettings.keyBindAttack || key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK] ||
				(Config.allowVanillaControls() && (key == mc.gameSettings.keyBindLeft || key == mc.gameSettings.keyBindRight)) ||
				key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_LEFT] || key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT]);
	}

	/**
	 * Sets direction of spin and activates skill when left or right arrow key pressed
	 * or adds extra spin for Super Spin Attack when attack key pressed
	 * NOTE: Super Spin Attack requires this method to be called explicitly for the
	 * 		 attack key, since the attack key is normally only processed if canInteract
	 * 		 returns true, which is not the case while spin attack is active
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean keyPressed(Minecraft mc, KeyBinding key, EntityPlayer player) {
		if (key == mc.gameSettings.keyBindAttack || key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK]) {
			if (isActive() && canRefresh() && canExecute(player)) {
				PacketDispatcher.sendToServer(new RefreshSpinPacket());
				arc += 360F;
				return true;
			}
		} else if (!isCharging()) {
			// prevents activation of Dodge from interfering with spin direction
			if (wasKeyPressed) {
				wasKeyPressed = false;
			} else {
				clockwise = (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT] || key == mc.gameSettings.keyBindRight);
				wasKeyPressed = true;
			}
			if (isKeyPressed()) {
				wasKeyPressed = false;
				charge = getChargeTime();
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean onActivated(World world, EntityPlayer player) {
		currentSpin = 0F;
		arc = 360F;
		refreshed = 0;
		superLevel = (checkHealth(player) ? ZSSPlayerSkills.get(player).getSkillLevel(superSpinAttack) : 0);
		isFlaming = EnchantmentHelper.getFireAspectModifier(player) > 0;
		startSpin(world, player);
		return true;
	}

	@Override
	protected void onDeactivated(World world, EntityPlayer player) {
		currentSpin = 0.0F;
		arc = 0.0F;
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		// isCharging can only be true on the client, which is where charging is handled
		if (isCharging()) { // check isRemote before accessing @client stuff anyway, just in case charge somehow set on server
			if (player.worldObj.isRemote && isKeyPressed()) {
				if (charge < (getChargeTime() - 1)) {
					Minecraft.getMinecraft().playerController.sendUseItem(player, player.worldObj, player.getHeldItem());
				}
				--charge;
				if (charge == 0 && canExecute(player)) {
					PacketDispatcher.sendToServer(new ActivateSkillPacket(this));
				}
			} else {
				charge = 0;
			}
		} else if (isActive()) {
			incrementSpin(player);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean onRenderTick(EntityPlayer player, float partialTickTime) {
		if (PlayerUtils.isHoldingSkillItem(player)) {
			List<EntityLivingBase> list = TargetUtils.acquireAllLookTargets(player, (int)(getRange() + 0.5F), 1.0D);
			for (EntityLivingBase target : list) {
				if (targets != null && targets.contains(target)) {
					Minecraft.getMinecraft().playerController.attackEntity(player, target);
					targets.remove(target);
				}
			}
			spawnParticles(player);
			player.swingProgress = 0.5F;
			player.setAngles((clockwise ? getSpinSpeed() : -getSpinSpeed()), 0);
		}
		return true;
	}

	/**
	 * Initiates spin attack and increments refreshed
	 * Client populates the nearby target list
	 * Server plays spin sound and, if not the first spin, adds exhaustion
	 */
	private void startSpin(World world, EntityPlayer player) {
		++refreshed;
		if (world.isRemote) {
			targets = world.getEntitiesWithinAABB(EntityLivingBase.class, player.boundingBox.expand(getRange(), 0.0D, getRange()));
			if (targets.contains(player)) {
				targets.remove(player);
			}
		} else {
			WorldUtils.playSoundAtEntity(player, Sounds.SPIN_ATTACK, 0.4F, 0.5F);
			if (refreshed > 1) {
				player.addExhaustion(getExhaustion());
			}
		}
	}

	/**
	 * Updates the spin progress counter and terminates the spin once it reaches the max spin arc
	 */
	private void incrementSpin(EntityPlayer player) {
		// 0.15D is the multiplier from Entity.setAngles, but that is too little now that no longer in render tick
		// 0.24D results in a perfect circle per spin, at all levels, taking 21 ticks to complete at level 1, and 15 at level 10
		currentSpin += getSpinSpeed() * 0.24D;
		if (currentSpin >= arc) {
			deactivate(player);
		} else if (currentSpin > (360F * refreshed)) {
			startSpin(player.worldObj, player);
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticles(EntityPlayer player) {
		// TODO these will not be seen by other players
		String particle = (isFlaming ? "flame" : (superLevel > 0 ? "magicCrit" : "crit"));
		Vec3 vec3 = player.getLookVec();
		double posX = player.posX + (vec3.xCoord * getRange());
		double posY = player.posY + player.getEyeHeight() - 0.1D;
		double posZ = player.posZ + (vec3.zCoord * getRange());
		for (int i = 0; i < 2; ++i) {
			player.worldObj.spawnParticle(particle, posX, posY, posZ, vec3.xCoord * 0.15D, 0.01D, vec3.zCoord * 0.15D);
		}
	}

	/**
	 * Called on the server after receiving the {@link RefreshSpinPacket}
	 */
	public void refreshServerSpin(EntityPlayer player) {
		if (canRefresh() && super.canUse(player) && PlayerUtils.isHoldingSkillItem(player)) {
			arc += 360F;
		}
	}
}
