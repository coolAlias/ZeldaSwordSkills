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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Tap 'down' arrow to parry an incoming attack with chance to disarm opponent. Only works on
 * opponents wielding an item, not against raw physical attacks such as a zombie touch.
 * 
 * Once activated, their is a short window (4 ticks at level 1) during which all incoming
 * attacks will be parried, followed by another short period during which parry cannot be
 * activated again (to prevent spamming). The 'cooldown' time decreases with level, whereas
 * the 'window' time increases.
 * 
 * Chance to Disarm: 0.1F per level + a time bonus of up to 0.2F
 * Exhaustion: 0.3F minus 0.02F per level (0.2F at level 5)
 * Notes: For players of equal parry skill, chance to disarm is based solely on timing
 * 
 * TODO continue testing on Zombies and other players, if possible; works on Pigmen
 * TODO parry animation - currently just a swing of the sword
 * 
 * Using vanilla controls, Parry is activated just like the Dodge skill, requiring either a
 * single tap and release, or a double-tap based on the Config settings. Parry never requires
 * a double tap when using the arrow key.
 * 
 */
public class Parry extends SkillActive
{
	/** Timer during which player is considered actively parrying */
	private int parryTimer = 0;
	
	/** Only for vanilla activation: Current number of ticks remaining before dodge will not activate */
	@SideOnly(Side.CLIENT)
	private int ticksTilFail;
	
	/** Notification to play miss sound; set to true when activated and false when attack parried */
	private boolean playMissSound;

	public Parry(String name, byte id) {
		super(name, id);
		setDisablesLMB();
		addDescription("parry.desc.0");
	}

	private Parry(Parry skill) { super(skill); }

	@Override
	public Parry newInstance() { return new Parry(this); }
	
	@Override
	public List<String> getDescription(EntityPlayer player) {
		List<String> desc = new ArrayList<String>(tooltip);
		desc.add(StatCollector.translateToLocalFormatted("skill.zss.parry.desc.1",(int)(getDisarmChance(player, null) * 100)));
		desc.add(StatCollector.translateToLocalFormatted("skill.zss.parry.desc.2",String.format("%.2f", getExhaustion())));
		return desc;
	}

	@Override
	public boolean isActive() { return (parryTimer > 0); }

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && ZSSPlayerInfo.get(player).isSkillActive(swordBasic) && PlayerUtils.isHoldingSword(player);
	}

	@Override
	protected float getExhaustion() { return 0.3F - (0.02F * level); }
	
	@Override
	public boolean activate(World world, EntityPlayer player) {
		if (super.activate(world, player)) {
			parryTimer = (6 + level); // 2 tick window to parry at level 1
			player.swingItem();
			playMissSound = true;
		}

		return isActive();
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			if (--parryTimer <= getParryDelay() && playMissSound) {
				playMissSound = false;
				player.playSound(ModInfo.SOUND_SWORDMISS, (player.worldObj.rand.nextFloat() * 0.4F + 0.5F), 1.0F / (player.worldObj.rand.nextFloat() * 0.4F + 0.5F));
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

	/** Number of ticks before player may attempt to parry again */
	private int getParryDelay() { return (5 - (level / 2)); }

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
	 * Attempts to parry the incoming attack and possibly disarms the attacker
	 * @return true if the attack was parried and the attack event should be canceled
	 */
	public boolean parryAttack(EntityPlayer player, EntityLivingBase attacker) {
		if (parryTimer > getParryDelay() && attacker.getHeldItem() != null) {
			parryTimer = getParryDelay(); // fix probable bug where player can disarm multiple opponents at once
			player.worldObj.playSoundAtEntity(player, ModInfo.SOUND_SWORDSTRIKE, (player.worldObj.rand.nextFloat() * 0.4F + 0.5F), 1.0F / (player.worldObj.rand.nextFloat() * 0.4F + 0.5F));
			playMissSound = false;
			
			if (player.worldObj.rand.nextFloat() < getDisarmChance(player, attacker)) {
				disarm(attacker);
			}

			return true;
		}

		return false;
	}

	/**
	 * Returns player's chance to disarm an attacker
	 * @param attacker if the attacker is an EntityPlayer, their Parry score will decrease their chance
	 * of being disarmed
	 */
	private float getDisarmChance(EntityPlayer player, EntityLivingBase attacker) {
		float penalty = 0.0F;
		float bonus = 0.025F * (parryTimer > 0 ? (parryTimer - getParryDelay()) : 0);
		if (attacker instanceof EntityPlayer) {
			penalty = 0.1F * ZSSPlayerInfo.get((EntityPlayer) attacker).getSkillLevel(this);
		}

		return ((level * 0.1F) - penalty + bonus);
	}

	/**
	 * Drops attacker's held item into the world
	 */
	private void disarm(EntityLivingBase attacker) {
		if (attacker.getHeldItem() != null) {
			EntityItem drop = new EntityItem(attacker.worldObj, attacker.posX,
					attacker.posY - 0.30000001192092896D + (double) attacker.getEyeHeight(),
					attacker.posZ, attacker.getHeldItem().copy());
			float f = 0.3F;
			float f1 = attacker.worldObj.rand.nextFloat() * (float) Math.PI * 2.0F;
			drop.motionX = (double)(-MathHelper.sin(attacker.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(attacker.rotationPitch / 180.0F * (float) Math.PI) * f);
			drop.motionZ = (double)(MathHelper.cos(attacker.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(attacker.rotationPitch / 180.0F * (float) Math.PI) * f);
			drop.motionY = (double)(-MathHelper.sin(attacker.rotationPitch / 180.0F * (float) Math.PI) * f + 0.1F);
			f = 0.02F * attacker.worldObj.rand.nextFloat();
			drop.motionX += Math.cos((double) f1) * (double) f;
			drop.motionY += (double)((attacker.worldObj.rand.nextFloat() - attacker.worldObj.rand.nextFloat()) * 0.1F);
			drop.motionZ += Math.sin((double) f1) * (double) f;
			drop.delayBeforeCanPickup = 40;
			attacker.worldObj.spawnEntityInWorld(drop);
			attacker.setCurrentItemOrArmor(0, (ItemStack) null);
		}
	}
}
