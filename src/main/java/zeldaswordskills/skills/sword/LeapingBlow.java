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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.projectile.EntityLeapingBlow;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.AddExhaustionPacket;
import zeldaswordskills.network.SpawnLeapingBlowPacket;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * LEAPING BLOW
 * Activation: Jump while holding block
 * Damage: Regular sword damage (without enchantment bonuses), +1 extra damage per skill level
 * Effect: Adds Weakness I for (50 + (10 * level)) ticks
 * Range: Technique travels roughly 3 blocks + 1/2 block per level
 * Area: Approximately (0.5F + (0.25F * level)) radius in a straight line
 * Exhaustion: 2.0F minus 0.1F per level (1.5F at level 5)
 * Special: Wielding a Master Sword causes double bonus damage and weakness time,
 * 			but only while at full health
 * 
 * Upon landing, all targets directly in front of the player take damage and
 * are weakened temporarily.
 * 
 */
public class LeapingBlow extends SkillActive
{
	/** Set to true when jumping and 'attack' key pressed; set to false upon landing */
	private boolean isActive = false;
	
	/** Whether the player is wielding a Master Sword */
	private boolean isMaster;

	public LeapingBlow(String name, byte id) {
		super(name, id);
		setDisablesLMB();
		addDescription(Arrays.asList("leapingblow.desc.0","leapingblow.desc.1","leapingblow.desc.2"));
	}

	private LeapingBlow(LeapingBlow skill) { super(skill); }

	@Override
	public LeapingBlow newInstance() { return new LeapingBlow(this); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getDescription(EntityPlayer player) {
		List<String> desc = new ArrayList<String>(4);
		desc.add(StatCollector.translateToLocal(tooltip.get(0)));
		desc.add(StatCollector.translateToLocalFormatted("skill.zss.leapingblow.desc.3",(PlayerUtils.isHoldingMasterSword(player) ? level * 2 : level)));
		desc.add(StatCollector.translateToLocalFormatted("skill.zss.leapingblow.desc.4",(getPotionDuration(player) / 20)));
		desc.add(StatCollector.translateToLocalFormatted("skill.zss.leapingblow.desc.5",String.format("%.2f", getExhaustion())));
		return desc;
	}
	
	@Override
	public boolean isActive() { return isActive; }
	
	@Override
	protected float getExhaustion() { return 2.0F - (0.1F * level); }
	
	/** Returns player's base damage (which includes all attribute bonuses) plus 1.0F per level */
	private float getDamage(EntityPlayer player) {
		return (float)((PlayerUtils.isHoldingMasterSword(player) ? level * 2 : level) + player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
	}
	
	/** Duration of weakness effect; used for tooltip display only */
	private int getPotionDuration(EntityPlayer player) { return ((PlayerUtils.isHoldingMasterSword(player) ? 110 : 50) + (level * 10)); }
	
	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && !isActive() && !player.onGround && ZSSPlayerInfo.get(player).isSkillActive(swordBasic) && PlayerUtils.isHoldingSword(player);
	}
	
	@Override
	public boolean activate(World world, EntityPlayer player) {
		// LeapingBlow is handled all client side, so super.activate is not used
		if (canUse(player)) {
			ZSSPlayerInfo.get(player).setCurrentActiveSkill(this);
			isActive = true;
			isMaster = (PlayerUtils.isHoldingMasterSword(player) && PlayerUtils.getHealthMissing(player) == 0.0F);
		}
		return isActive();
	}
	
	/**
	 * Called from Forge fall Events
	 * @param distance distance fallen, passed from Forge fall Event
	 */
	@SideOnly(Side.CLIENT)
	public void onImpact(EntityPlayer player, float distance) {
		SwordBasic swordSkill = (SwordBasic) ZSSPlayerInfo.get(player).getPlayerSkill(swordBasic);
		if (isActive() && swordSkill != null && swordSkill.isActive() && PlayerUtils.isHoldingSword(player))
		{
			player.swingItem();
			isActive = false;

			if (distance < 1.0F) {
				if (swordSkill.onAttack(player)) {
					Minecraft.getMinecraft().playerController.attackEntity(player, swordSkill.getCurrentTarget());
				}
			} else {
				PacketDispatcher.sendPacketToServer(new AddExhaustionPacket(getExhaustion()).makePacket());
				PacketDispatcher.sendPacketToServer(new SpawnLeapingBlowPacket(isMaster).makePacket());
			}
		}
	}
	
	/**
	 * Called upon receipt of SpawnLeapingBlowPacket on server; spawns the entity
	 */
	public void spawnLeapingBlowEntity(World world, EntityPlayer player, boolean isMaster) {
		this.isMaster = isMaster;
		Entity entity = new EntityLeapingBlow(world, player).setDamage(getDamage(player)).setLevel(level, isMaster);
		world.spawnEntityInWorld(entity);
		//world.playSoundAtEntity(player, ModInfo.SOUND_YELL, 1.0F, 1.0F / (world.rand.nextFloat() * 0.2F + 0.95F));
		world.playSoundAtEntity(player, ModInfo.SOUND_LEAPINGBLOW, (world.rand.nextFloat() * 0.4F + 0.5F), 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
	}
}
