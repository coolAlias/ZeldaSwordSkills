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
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.network.MortalDrawPacket;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * MORTAL DRAW
 * Activation: While empty-handed and locked on, hold the block key and attack
 * Effect: The art of drawing the sword, or Battoujutsu, is a risky but deadly move, capable
 * of inflicting mortal wounds on unsuspecting opponents with a lightning-fast blade strike
 * Exhaustion: 3.0F - (0.2 * level)
 * Damage: If successful, inflicts double damage
 * Duration: Window of attack opportunity is (level + 3) ticks
 * Notes: The first sword found in the action bar will be used for the strike; plan accordingly
 *
 */
public class MortalDraw extends SkillActive
{
	/** Used to track when RMB is held down, since this cannot be checked reliably during LMB MouseEvent */
	@SideOnly(Side.CLIENT)
	private boolean isRMBDown;
	/** The time remaining during which the skill will succeed */
	private int attackTimer;
	/** Nearest sword slot index */
	private int swordSlot;

	public MortalDraw(String name, byte id) {
		super(name, id);
		setDisablesLMB();
		addDescription(Arrays.asList("mortaldraw.desc.0","mortaldraw.desc.1"));
	}
	
	private int getAttackTime() { return level + 2; }

	private MortalDraw(MortalDraw skill) { super(skill); }
	
	@Override
	public MortalDraw newInstance() { return new MortalDraw(this); }
	
	@Override
	public List<String> getDescription(EntityPlayer player) {
		List<String> desc = new ArrayList<String>(tooltip);
		desc.add(StatCollector.translateToLocalFormatted("skill.zss.mortaldraw.desc.2", getAttackTime()));
		desc.add(StatCollector.translateToLocalFormatted("skill.zss.mortaldraw.desc.3", String.format("%.2f", getExhaustion())));
		return desc;
	}

	@Override
	public boolean isActive() { return attackTimer > 0; }
	
	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && swordSlot > -1 && ZSSPlayerInfo.get(player).isSkillActive(swordBasic) && player.getHeldItem() == null;
	}

	@Override
	protected float getExhaustion() { return 3.0F - (0.2F * level); }
	
	@Override
	public boolean activate(World world, EntityPlayer player) {
		if (!isActive()) {
			swordSlot = -1;
			for (int i = 0; i < 9; ++i) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null && stack.getItem() instanceof ItemSword) {
					swordSlot = i;
					break;
				}
			}
		}
		if (super.activate(world, player)) {
			attackTimer = getAttackTime();
		}
		return isActive();
	}
	
	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive()) {
			--attackTimer;
			if (attackTimer == 0 && !player.worldObj.isRemote) {
				drawSword(player, null);
				if (player.getHeldItem() != null) {
					PacketDispatcher.sendPacketToPlayer(new MortalDrawPacket().makePacket(), (Player) player);
				}
			}
		}
	}
	
	/**
	 * Returns true if the player was able to draw a sword
	 */
	public boolean drawSword(EntityPlayer player, Entity attacker) {
		if (swordSlot > -1 && swordSlot != player.inventory.currentItem) {
			player.setCurrentItemOrArmor(0, player.inventory.getStackInSlot(swordSlot));
			player.inventory.setInventorySlotContents(swordSlot, null);
			swordSlot = -1;
			if (player.worldObj.isRemote) {
				isRMBDown = false; // prevents some accidental activation on next target
			} else { // only care about this on server:
				ILockOnTarget skill = ZSSPlayerInfo.get(player).getTargetingSkill();
				return (skill != null && skill.getCurrentTarget() == attacker);
			}
		}
		return false;
	}
	
	/**
	 * Call upon landing a mortal draw blow
	 */
	public void onImpact(EntityPlayer player, LivingHurtEvent event) {
		attackTimer = 0;
		event.ammount *= 2.0F;
	}
	
	/** Call whenever RMB changes */
	@SideOnly(Side.CLIENT)
	public void keyPressed(boolean pressed) { isRMBDown = pressed; }
	
	@SideOnly(Side.CLIENT)
	public boolean isRMBDown() { return isRMBDown; }

}
