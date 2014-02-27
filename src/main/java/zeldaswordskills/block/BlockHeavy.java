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

package zeldaswordskills.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHeavy extends Block implements ILiftable, ISmashable
{
	/** The weight of this block, i.e. the difficulty of lifting this block */
	private final BlockWeight weight;

	/**
	 * An indestructible block that can only be moved with special items
	 * @param strengthRequired The strength level required to lift this block
	 */
	public BlockHeavy(int id, Material material, BlockWeight strengthRequired) {
		super(id, material);
		weight = strengthRequired;
		disableStats();
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundStoneFootstep);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public BlockWeight getLiftWeight() {
		return weight;
	}
	
	@Override
	public BlockWeight getSmashWeight() {
		return weight != BlockWeight.IMPOSSIBLE ? BlockWeight.values()[weight.ordinal() + 1] : weight;
	}
	
	@Override
	public boolean onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		world.playSoundAtEntity(player, ModInfo.SOUND_ROCK_FALL, 1.0F, 1.0F);
		return true;
	}
	
	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		// TODO change this to hammers
		if (PlayerUtils.isHoldingMasterSword(player) && player.getHeldItem().getItem() == ZSSItems.swordMasterTrue) {
			if ((weight == BlockWeight.MEDIUM && PlayerUtils.hasItem(player, ZSSItems.gauntletsSilver))
				|| (weight == BlockWeight.VERY_HEAVY && PlayerUtils.hasItem(player, ZSSItems.gauntletsGolden)))
			{
				world.playSoundAtEntity(player, ModInfo.SOUND_ROCK_FALL, 1.0F, 1.0F);
				world.destroyBlock(x, y, z, false);
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		blockIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}
}
