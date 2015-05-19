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
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHeavy extends Block implements IDungeonBlock, ILiftable, ISmashable
{
	/** The weight of this block, i.e. the difficulty of lifting this block */
	private final BlockWeight weight;

	/**
	 * An indestructible block that can only be moved with special items
	 * @param strengthRequired The strength level required to lift this block
	 */
	public BlockHeavy(Material material, BlockWeight strengthRequired) {
		super(material);
		weight = strengthRequired;
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, int meta, int side) {
		return weight;
	}

	@Override
	public void onLifted(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int meta) {
		if (this == ZSSBlocks.barrierHeavy) {
			player.triggerAchievement(ZSSAchievements.heavyLifter);
		} else if (this == ZSSBlocks.barrierLight) {
			player.triggerAchievement(ZSSAchievements.movingBlocks);
		}
	}

	@Override
	public void onHeldBlockPlaced(World world, ItemStack stack, int x, int y, int z, int meta) {}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta, int side) {
		return (stack.getItem() == ZSSItems.hammerMegaton && PlayerUtils.hasItem(player, ZSSItems.gauntletsGolden) ? weight : weight.next());
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		return Result.DEFAULT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		blockIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}
}
