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
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTarget extends Block implements IHookable
{
	public BlockTarget(int id, Material material) {
		super(id, material);
		setHardness(5.0F);
		setResistance(15.0F);
		setStepSound(soundStoneFootstep);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public Result canDestroyBlock(HookshotType type, World world, int x, int y, int z, int side) {
		return Result.DENY;
	}

	@Override
	public Result canGrabBlock(HookshotType type, World world, int x, int y, int z, int side) {
		return Result.ALLOW;
	}

	@Override
	public Material getHookableMaterial(HookshotType type, World world, int x, int y, int z) {
		return blockMaterial;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		blockIcon = register.registerIcon(ModInfo.ID + ":hook_target_face");
	}
}
