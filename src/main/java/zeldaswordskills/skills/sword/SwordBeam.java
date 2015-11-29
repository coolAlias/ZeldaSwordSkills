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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntitySwordBeam;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * SWORD BEAM
 * Description: Shoot a beam of energy from the sword tip
 * Activation: Attack while sneaking and at near full health
 * Effect: Shoots a ranged beam capable of damaging one or possibly more targets
 * Damage: 30 + (level * 10) percent of the base sword damage (without other bonuses)
 * Range: Approximately 12 blocks, plus one block per level
 * Exhaustion: 2.0F - (0.1F * level)
 * Special:
 * 	- May only be used while locked on to a target
 *  - Amount of health required decreases with skill level, down to 1-1/2 hearts below max
 *  - Hitting a target with the beam counts as a direct strike for combos
 *  - Using the Master Sword will shoot a beam that can penetrate multiple targets
 *  - Each additional target receives 20% less damage than the previous
 * 
 * Sword beam shot from Link's sword when at full health. Inflicts the sword's full
 * base damage, not including enchantment or other bonuses, to the first entity struck.
 * 
 * If using the Master Sword, the beam will shoot through enemies, hitting all targets
 * in its direct path.
 * 
 */
public class SwordBeam extends SkillActive
{
	/** Used to end combo if the sword beam fails to strike a target */
	private int missTimer;

	public SwordBeam(String name) {
		super(name);
	}

	private SwordBeam(SwordBeam skill) {
		super(skill);
	}

	@Override
	public SwordBeam newInstance() {
		return new SwordBeam(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(List<String> desc, EntityPlayer player) {
		desc.add(getDamageDisplay(getDamageFactor(player), false) + "%");
		desc.add(getRangeDisplay(12 + level));
		desc.add(StatCollector.translateToLocalFormatted(getInfoString("info", 1),
				String.format("%.1f", Config.getHealthAllowance(level) / 2.0F)));
		desc.add(getExhaustionDisplay(getExhaustion()));
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean hasAnimation() {
		return false;
	}

	@Override
	protected float getExhaustion() {
		return 2.0F - (0.1F * level);
	}

	/** Returns true if players current health is within the allowed limit */
	private boolean checkHealth(EntityPlayer player) {
		return player.capabilities.isCreativeMode || PlayerUtils.getHealthMissing(player) <= Config.getHealthAllowance(level);
	}

	/** The percent of base sword damage that should be inflicted, as an integer */
	private int getDamageFactor(EntityPlayer player) {
		ItemStack mask = player.getEquipmentInSlot(ArmorIndex.EQUIPPED_HELM);
		int base = (mask != null && mask.getItem() == ZSSItems.maskFierce) ? 55 : 30;
		return base + (level * 10);
	}

	/** Returns player's base damage (with sword) plus 1.0F per level */
	private float getDamage(EntityPlayer player) {
		return (float)((double)(getDamageFactor(player)) * 0.01D * player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return super.canUse(player) && checkHealth(player) && ZSSPlayerInfo.get(player).canAttack() && PlayerUtils.isSword(player.getHeldItem());
	}

	/**
	 * Player must be on ground to prevent conflict with RisingCut
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canExecute(EntityPlayer player) {
		return player.onGround && player.isSneaking() && canUse(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isKeyListener(Minecraft mc, KeyBinding key) {
		return (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK] || (Config.allowVanillaControls && key == mc.gameSettings.keyBindAttack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean keyPressed(Minecraft mc, KeyBinding key, EntityPlayer player) {
		if (canExecute(player)) {
			PacketDispatcher.sendToServer(new ActivateSkillPacket(this));
			return true;
		}
		return false;
	}

	@Override
	protected boolean onActivated(World world, EntityPlayer player) {
		if (!world.isRemote) {
			missTimer = 12 + level;
			WorldUtils.playSoundAtEntity(player, Sounds.WHOOSH, 0.4F, 0.5F);
			Vec3 vec3 = player.getLookVec();
			EntitySwordBeam beam = new EntitySwordBeam(world, player).setLevel(level);
			beam.setDamage(getDamage(player));
			beam.setMasterSword(PlayerUtils.isHoldingMasterSword(player));
			beam.setPosition(beam.posX + vec3.xCoord * 2, beam.posY + vec3.yCoord * 2, beam.posZ + vec3.zCoord * 2);
			world.spawnEntityInWorld(beam);
		} else {
			player.swingItem();
			ZSSPlayerInfo.get(player).setAttackTime((player.capabilities.isCreativeMode ? 0 : 20 - level));
		}
		return true;
	}

	@Override
	protected void onDeactivated(World world, EntityPlayer player) {
		missTimer = 0;
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (missTimer > 0) {
			--missTimer;
			if (missTimer == 0 && !player.worldObj.isRemote) {
				ICombo combo = ZSSPlayerSkills.get(player).getComboSkill();
				if (combo != null && combo.isComboInProgress()) {
					combo.getCombo().endCombo(player);
				}
			}
		}
	}

	/**
	 * Call from {@link EntitySwordBeam#onImpact} to allow handling of ICombo;
	 * striking an entity sets the missTimer to zero
	 * @param hitBlock true if sword beam hit a block rather than an entity
	 */
	public void onImpact(EntityPlayer player, boolean hitBlock) {
		missTimer = (hitBlock && missTimer > 0 ? 1 : 0);
	}
}
