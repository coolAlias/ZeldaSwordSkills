/**
    Copyright (C) <2018> <coolAlias>

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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.entity.player.ZSSPlayerWallet.EnumWallet;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

public class ItemWalletUpgrade extends Item
{
	/** Wallet texture locations suitable for use with TextureManager#bindTexture */
	public static final List<ResourceLocation> TEXTURES = new ArrayList<ResourceLocation>(EnumWallet.values().length);

	@SideOnly(Side.CLIENT)
	private List<IIcon> ICONS;

	public ItemWalletUpgrade() {
		setMaxDamage(0);
		setUnlocalizedName("zss.wallet_upgrade");
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		ZSSPlayerWallet wallet = ZSSPlayerWallet.get(player);
		if (wallet.upgrade()) {
			world.playSoundAtEntity(player, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.wallet_upgrade.fail");
			// TODO remove:
			wallet.reset();
		}
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player != null) {
			// Display the icon of the upgraded wallet rather than the current one
			int icon_index = ZSSPlayerWallet.get(player).getWallet().next().icon_index;
			return ICONS.get(icon_index);
		}
		return ICONS.get(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		ICONS = Lists.<IIcon>newArrayList();
		for (EnumWallet wallet : EnumWallet.values()) {
			if (wallet == EnumWallet.NONE) {
				continue; // uses icon_index 0, just like Child's Wallet
			}
			String name = "wallet_" + wallet.unlocalized_name;
			ResourceLocation location = new ResourceLocation(ModInfo.ID, "textures/items/" + name + ".png");
			if (TEXTURES.size() < EnumWallet.values().length - 1) { // less 1 since NONE is not added
				TEXTURES.add(location);
			}
			ICONS.add(register.registerIcon(ModInfo.ID + ":" + name));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		if (player != null) {
			EnumWallet wallet = ZSSPlayerWallet.get(player).getWallet();
			if (wallet.canUpgrade()) {
				list.add(StatCollector.translateToLocal("tooltip.zss.wallet_upgrade.desc"));
				list.add(StatCollector.translateToLocalFormatted("tooltip.zss.wallet_upgrade.desc.next", StatCollector.translateToLocal(wallet.next().getUnlocalizedName())));
			} else {
				list.add(StatCollector.translateToLocal("tooltip.zss.wallet_upgrade.desc.max"));
			}
		} else {
			list.add(StatCollector.translateToLocal("tooltip.zss.wallet_upgrade.desc"));
		}
	}
}
