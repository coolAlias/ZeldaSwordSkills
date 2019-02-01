/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.item;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

public class ItemBoomerang extends BaseModItem implements IFairyUpgrade, IUnenchantable
{
	/** The amount of damage this boomerang will cause */
	private final float damage;

	/** The distance that this boomerang can fly */
	private final int range;

	/** Whether this boomerang will capture all drops */
	private boolean captureAll;

	public ItemBoomerang(float damage, int range) {
		super();
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
			world.playSoundAtEntity(player, Sounds.WHOOSH, 0.5F, 1.0F);
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
	public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip.zss.boomerang.desc.0"));
		list.add("");
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("tooltip.zss.damage", "+", (int) damage));
		list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.zss.range", range));
	}

	@Override
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core) {
		BlockPos pos = core.getPos();
		if (ZSSPlayerSkills.get(player).getSkillLevel(SkillBase.bonusHeart) >= Config.getMaxBonusHearts() / 2) {
			item.setDead();
			player.triggerAchievement(ZSSAchievements.fairyBoomerang);
			WorldUtils.spawnItemWithRandom(core.getWorld(), new ItemStack(ZSSItems.boomerangMagic), pos.getX(), pos.getY() + 2, pos.getZ());
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		} else {
			core.getWorld().playSoundEffect(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D, Sounds.FAIRY_LAUGH, 1.0F, 1.0F);
			PlayerUtils.sendTranslatedChat(player, "chat.zss.fairy.laugh.unworthy");
		}
	}

	@Override
	public boolean hasFairyUpgrade(ItemStack stack) {
		return this == ZSSItems.boomerang;
	}
}
