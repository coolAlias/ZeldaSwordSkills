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

package zeldaswordskills.item;

import java.util.List;

import mods.battlegear2.api.PlayerEventChild.OffhandAttackEvent;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface="mods.battlegear2.api.weapons.IBattlegearWeapon", modid="battlegear2", striprefs=true)
public class ItemBoomerang extends Item implements IFairyUpgrade, IBattlegearWeapon
{
	/** The amount of damage this boomerang will cause */
	private final float damage;

	/** The distance that this boomerang can fly */
	private final int range;

	/** Whether this boomerang will capture all drops */
	private boolean captureAll;

	public ItemBoomerang(int id, float damage, int range) {
		super(id);
		this.damage = damage;
		this.range = range;
		this.captureAll = false;
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabCombat);
	}

	/**
	 * Sets this boomerang to capture all item drops
	 */
	public ItemBoomerang setCaptureAll() {
		captureAll = true;
		return this;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.swingItem();
		player.addExhaustion(0.3F);
		if (!world.isRemote) {
			world.playSoundAtEntity(player, Sounds.WHOOSH, 1.0F, 1.0F);
			world.spawnEntityInWorld(new EntityBoomerang(world, player).setCaptureAll(captureAll).
					setRange(range).setInvStack(stack, player.inventory.currentItem).setDamage(damage));
			player.setCurrentItemOrArmor(0, null);
		}

		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.boomerang.desc.0"));
		list.add("");
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("tooltip.zss.damage", "+", (int) damage));
		list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.range", range));
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		if (ZSSPlayerSkills.get(player).getSkillLevel(SkillBase.bonusHeart) >= Config.getMaxBonusHearts() / 2) {
			item.setDead();
			player.triggerAchievement(ZSSAchievements.fairyBoomerang);
			WorldUtils.spawnItemWithRandom(core.getWorldObj(), new ItemStack(ZSSItems.boomerangMagic), core.xCoord, core.yCoord + 2, core.zCoord);
			core.getWorldObj().playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		} else {
			core.worldObj.playSoundEffect(core.xCoord + 0.5D, core.yCoord + 1, core.zCoord + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return this == ZSSItems.boomerang;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean sheatheOnBack(ItemStack stack) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean isOffhandHandDual(ItemStack stack) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandAttackEntity(OffhandAttackEvent event, ItemStack main, ItemStack offhand) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickAir(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public boolean offhandClickBlock(PlayerInteractEvent event, ItemStack main, ItemStack offhand) {
		return false;
	}

	@Method(modid="battlegear2")
	@Override
	public void performPassiveEffects(Side side, ItemStack main, ItemStack offhand) {}

	@Method(modid="battlegear2")
	@Override
	public boolean allowOffhand(ItemStack main, ItemStack offhand) {
		return false;
	}
}
