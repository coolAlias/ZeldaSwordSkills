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

package zeldaswordskills.skills;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.bidirectional.ActivateSkillPacket;
import zeldaswordskills.network.packet.bidirectional.DeactivateSkillPacket;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Base class for active skills. Extend this class to add specific functionality.
 * 
 * Note that additional fields in child classes are not saved to NBT, so should not be
 * used to store any data that needs to be maintained between game sessions.
 * 
 * Unless the skill's activation is handled exclusively by the client side, ONLY activate or trigger
 * the skill on the server, as a packet will be sent automatically to notify the client
 * 
 * Each Skill should contain the following documentation followed by a full description
 * 
 * NAME: name of the skill
 * Description: one-line summary of skill
 * Activation:  Standard - activated by selecting the skill in the Skill Bar and pressing 'x'
 * 				Triggered - this skill can not be directly activated by the player
 * 				(toggle) - this skill is toggled on or off when activated
 * 				other - give details of how to activate the skill
 * Exhaustion: format is [0.0F +- (amount * level)]
 * Damage: if any, give the amount and additional details as necessary
 * Duration: if any, give the amount in ticks or seconds, as applicable
 * Range: if restricted, give the range in an applicable format, such as in blocks
 * Area: if any, give the dimensions and additional details as necessary
 * Special: any special notes
 * 
 * Full description goes here.
 *
 */
public abstract class SkillActive extends SkillBase
{
	/**
	 * Constructs the first instance of a skill and stores it in the skill list
	 * @param name	this is the unlocalized name and should not contain any spaces
	 */
	protected SkillActive(String name) {
		super(name, true);
	}

	protected SkillActive(SkillActive skill) {
		super(skill);
	}

	/**
	 * Return false if this skill may not be directly activated manually, in which case it
	 * should have some other method of {@link #trigger(World, EntityPlayer) triggering}
	 * @return	Default returns TRUE, allowing activation via {@link #activate}
	 */
	protected boolean allowUserActivation() {
		return true;
	}

	/** Returns true if this skill is currently active, however that is defined by the child class */
	public abstract boolean isActive();

	/** Amount of exhaustion added to the player each time this skill is used */
	protected abstract float getExhaustion();

	/**
	 * Return true to automatically add exhaustion amount upon activation.
	 * Used by LeapingBlow, since it may or may not trigger upon landing.
	 */
	protected boolean autoAddExhaustion() {
		return true;
	}

	@Override
	protected void levelUp(EntityPlayer player) {}

	/**
	 * Returns true if this skill can currently be used by the player (i.e. activated or triggered)
	 * @return 	Default returns true if the skill's level is at least one and either the player
	 * 			is in Creative Mode or the food bar is not empty 
	 */
	public boolean canUse(EntityPlayer player) {
		return (level > 0 && (player.capabilities.isCreativeMode || player.getFoodStats().getFoodLevel() > 0));
	}

	/**
	 * Called only on the client side as a pre-activation check for some skills;
	 * typical use is to check if all necessary keys have been pressed
	 */
	@SideOnly(Side.CLIENT)
	public boolean canExecute(EntityPlayer player) {
		return true;
	}

	/**
	 * Return true if {@link #keyPressed} should be called when the given key is pressed
	 */
	@SideOnly(Side.CLIENT)
	public boolean isKeyListener(Minecraft mc, KeyBinding key) {
		return false;
	}

	/**
	 * This method is called if {@link #isKeyListener} returns true for the given key,
	 * allowing the skill to handle the key input accordingly. Note that each key press
	 * may only be handled once, on a first-come first-serve basis.
	 * @return	True signals that the key press was handled: no other key listeners
	 * 			will receive this key press
	 */
	@SideOnly(Side.CLIENT)
	public boolean keyPressed(Minecraft mc, KeyBinding key, EntityPlayer player) {
		return false;
	}

	/**
	 * Whether this skill automatically sends an {@link ActivateSkillPacket} to the client from {@link #trigger}
	 */
	protected boolean sendClientUpdate() {
		return true;
	}

	/**
	 * Called after a skill is activated via {@link #trigger}; on the client, this only
	 * gets called after receiving the {@link ActivateSkillPacket} sent when triggered on
	 * the server. If {@link #sendClientUpdate} returns false, then the packet is not sent
	 * and this method will not be called on the client, in which case any client-side
	 * requirements (e.g. player.swingItem) should be done when sending the activation
	 * packet to the server.
	 * 
	 * Anything that needs to happen when the skill is activated should be done here,
	 * such as setting timers, etc.
	 * 
	 * @return	Return true to have this skill added to the currently active skills list;
	 * 			Typically returns {@link #isActive}, which is almost always true after this method is called
	 */
	protected abstract boolean onActivated(World world, EntityPlayer player);

	/**
	 * Called when the skill is forcefully deactivated on either side via {@link #deactivate}.
	 * 
	 * Each implementation MUST guarantee that {@link isActive} no longer returns
	 * true once the method has completed.
	 * 
	 * {@link #isAnimating} is not required to return false after deactivation, though it usually should.
	 */
	protected abstract void onDeactivated(World world, EntityPlayer player);

	/**
	 * This is the method that should be called when a player tries to activate a skill
	 * directly, e.g. from a key binding, HUD, or other such means, to ensure that skills
	 * with special activation requirements are not circumvented: i.e. {@link #trigger} is
	 * called only if direct {@link #allowUserActivation() user activation} is allowed.
	 * 
	 * @return false if the skill could not be activated, or returns {@link #trigger}
	 */
	public final boolean activate(World world, EntityPlayer player) {
		return (allowUserActivation() ? trigger(world, player, false) : false);
	}

	/**
	 * Forcefully deactivates a skill.
	 * 
	 * Call this method on either side to ensure that the skill is deactivated on both.
	 * If the skill is currently {@link #isActive active}, then {@link #onDeactivated}
	 * is called and a {@link DeactivateSkillPacket} is sent to the other side.
	 * 
	 * If the skill is still active after onDeactivated was called, a SEVERE message
	 * is generated noting the skill that failed to meet onDeactivated's specifications,
	 * as such behavior may result in severe instability or even crashes and should be
	 * fixed immediately.
	 */
	public final void deactivate(EntityPlayer player) throws IllegalStateException {
		if (isActive()) {
			onDeactivated(player.worldObj, player);
			if (isActive()) {
				LogHelper.severe(getDisplayName() + " is still active after onDeactivated called - this may result in SEVERE errors or even crashes!!!");
			} else if (player.worldObj.isRemote) {
				PacketDispatcher.sendToServer(new DeactivateSkillPacket(this));
			} else {
				PacketDispatcher.sendTo(new DeactivateSkillPacket(this), (EntityPlayerMP) player);
			}
		}
	}

	/**
	 * This method should not be called directly except from an {@link ActivateSkillPacket}
	 * sent by a skill when it determines that any special activation requirements have been
	 * met (e.g. Armor Break must first charge up by holding the 'attack' key for a while).
	 * 
	 * If {@link #canUse} returns true, the skill will be activated.
	 * {@link #getExhaustion} is added if {@link #autoAddExhaustion} is true, and an
	 * {@link ActivateSkillPacket} is sent to the client if required.
	 * 
	 * Finally, {@link #onActivated} is called, allowing the skill to initialize its
	 * active state.
	 * 
	 * @param wasTriggered	Flag for {@link ActivateSkillPacket} when received on the client: 
	 * 						true to call {@link #trigger}, false to call {@link #activate}.
	 * @return	Returns {@link #onActivated}, signaling whether or not to add the skill to the
	 * 			list of currently active skills.
	 */
	public final boolean trigger(World world, EntityPlayer player, boolean wasTriggered) {
		if (canUse(player)) {
			if (autoAddExhaustion() && !player.capabilities.isCreativeMode) {
				player.addExhaustion(getExhaustion());
			}
			if (!world.isRemote) {
				if (sendClientUpdate()) {
					PacketDispatcher.sendTo(new ActivateSkillPacket(this, wasTriggered), (EntityPlayerMP) player);
				}
			}
			return onActivated(world, player);
		} else {
			if (level > 0) {
				PlayerUtils.sendFormattedChat(player, "chat.zss.skill.use.fail", getDisplayName());
			}
			return false;
		}
	}

	/**
	 * Return true to flag this skill as requiring animation, in which case interactions
	 * via mouse or keyboard are disabled while {@link #isAnimating} returns true
	 * @return Default is TRUE - override for skills that do not have animations
	 */
	public boolean hasAnimation() {
		return true;
	}

	/**
	 * Whether this skill's animation is currently in progress, in which case {@link #onRenderTick}
	 * will be called each render tick and mouse/keyboard interactions are disabled.
	 * @return Default implementation returns {@link #isActive()}
	 */
	@SideOnly(Side.CLIENT)
	public boolean isAnimating() {
		return isActive();
	}

	/**
	 * This method is called each render tick that {@link #isAnimating} returns true
	 * @param partialTickTime	The current render tick time
	 * @return Return true to prevent the targeting camera from auto-updating the player's view
	 */
	@SideOnly(Side.CLIENT)
	public boolean onRenderTick(EntityPlayer player, float partialTickTime) {
		return false;
	}

	/**
	 * Called from LivingAttackEvent only if the skill is currently {@link #isActive() active}
	 * @param player	The skill-using player under attack
	 * @param source	The source of damage; source#getEntity() is the entity that will strike the player,
	 * 					source.getSourceOfDamage() is either the same, or the entity responsible for
	 * 					unleashing the other entity (such as the shooter of an arrow)
	 * @return			Return true to cancel the attack event
	 */
	public boolean onBeingAttacked(EntityPlayer player, DamageSource source) {
		return false;
	}

	/**
	 * Called from LivingHurtEvent when a player using this skill first damages an entity,
	 * before any other modifiers are applied.
	 * The skill should currently be {@link #isActive() active}. Setting the event damage
	 * to zero or canceling the event will prevent any further processing of the LivingHurtEvent.
	 * @param player	The skill-using player inflicting damage (i.e. event.source.getEntity() is the player)
	 * @param event		The hurt event may be canceled, damage amount modified, etc.
	 */
	//public void onImpact(EntityPlayer player, LivingHurtEvent event) {}

	/**
	 * Called from LivingHurtEvent only if the skill is currently {@link #isActive() active}
	 * for the player that inflicted the damage, after all damage modifiers have been taken
	 * into account, providing a final chance to modify the damage or perform other actions.
	 * 
	 * @param player	The skill-using player inflicting damage (i.e. event.source.getEntity() is the player)
	 * @param entity	The entity damaged, i.e. LivingHurtEvent's entityLiving
	 * @param amount	The current damage amount from {@link LivingHurtEvent#ammount}
	 * @return			The final damage amount to inflict
	 */
	public float postImpact(EntityPlayer player, EntityLivingBase entity, float amount) {
		return amount;
	}

	@Override
	public final void writeToNBT(NBTTagCompound compound) {
		compound.setByte("id", getId());
		compound.setByte("level", level);
	}

	@Override
	public final void readFromNBT(NBTTagCompound compound) {
		level = compound.getByte("level");
	}

	@Override
	public final SkillActive loadFromNBT(NBTTagCompound compound) {
		SkillActive skill = (SkillActive) getNewSkillInstance(compound.getByte("id"));
		skill.readFromNBT(compound);
		return skill;
	}
}
