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

package zeldaswordskills.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Like normal doors, these always come in a two-block pair, but may only be removed by
 * using the matching Big Key.
 * 
 * Metadata 0x0 to 0x7 are the key type required to open the door, and 0x8 flags top or bottom.
 *
 */
public class BlockDoorBoss extends BlockDoorLocked
{
	@SideOnly(Side.CLIENT)
	private Icon[] iconsTop;
	@SideOnly(Side.CLIENT)
	private Icon[] iconsUpper;
	@SideOnly(Side.CLIENT)
	private Icon[] iconsLower;

	public BlockDoorBoss(int id, Material material) {
		super(id, material);
	}

	@Override
	protected boolean canUnlock(EntityPlayer player, int meta) {
		return PlayerUtils.consumeHeldItem(player, ZSSItems.keyBig, meta & 0x7, 1) || PlayerUtils.consumeHeldItem(player, ZSSItems.keySkeleton, 0, 1);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return new ItemStack(ZSSItems.doorLocked, 1, world.getBlockMetadata(x, y, z) & 0x7);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		if ((side == 0 && meta > 0x7) || (side == 1 && meta < 0x8)) {
			return iconEmpty;
		}
		return (meta > 0x7 ? (side == 1 ? iconsTop[meta % 8] : iconsUpper[meta % 8]) : (side == 0 ? iconsTop[meta % 8] : iconsLower[meta % 8]));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconEmpty = register.registerIcon(ModInfo.ID + ":empty");
		iconsTop = new Icon[8];
		iconsUpper = new Icon[8];
		iconsLower = new Icon[8];
		for (int i = 0; i < 8; ++i) {
			iconsTop[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_top" + i);
			iconsUpper[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_upper" + i);
			iconsLower[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_lower" + i);
		}
	}
}
