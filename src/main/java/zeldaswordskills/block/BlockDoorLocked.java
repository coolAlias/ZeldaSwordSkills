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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
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
public class BlockDoorLocked extends Block implements IDungeonBlock
{
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon[] iconsUpper;
	@SideOnly(Side.CLIENT)
	private IIcon[] iconsLower;

	public BlockDoorLocked(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeMetal);
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return y >= 255 ? false : World.doesBlockHaveSolidTopSurface(world, x, y - 1, z) && super.canPlaceBlockAt(world, x, y, z) && super.canPlaceBlockAt(world, x, y + 1, z);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			if (PlayerUtils.consumeHeldItem(player, ZSSItems.keySkeleton, 0, 1)) {
				world.playSoundAtEntity(player, Sounds.LOCK_DOOR, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
				world.setBlockToAir(x, y, z);
			} else {
				world.playSoundAtEntity(player, Sounds.LOCK_RATTLE, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
			}
		}
		return false; // returning true here prevents ItemBigKey from processing onItemUse
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMeta) {
		y += (oldMeta > 0x7 ? -1 : 1);
		if (world.getBlock(x, y, z) == this) {
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return new ItemStack(ZSSItems.doorLocked, 1, world.getBlockMetadata(x, y, z) & 0x7);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side == 1 ? iconTop : meta > 0x7 ? iconsUpper[meta % 8] : iconsLower[meta % 8]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		iconTop = register.registerIcon(ModInfo.ID + ":door_locked_top");
		iconsUpper = new IIcon[8];
		iconsLower = new IIcon[8];
		for (int i = 0; i < 8; ++i) {
			iconsUpper[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_upper" + i);
			iconsLower[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + "_lower" + i);
		}
	}
}
