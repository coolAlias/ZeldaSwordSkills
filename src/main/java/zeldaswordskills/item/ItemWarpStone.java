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

import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.block.BlockWarpStone;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.songs.AbstractZeldaSong;

public class ItemWarpStone extends ItemMetadataBlock {

	public ItemWarpStone(Block block) {
		super(block);
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[BlockWarpStone.EnumWarpSong.values().length];
		for (BlockWarpStone.EnumWarpSong warpSong : BlockWarpStone.EnumWarpSong.values()) {
			// all of the variants use the exact same texture
			variants[warpSong.getMetadata()] = ModInfo.ID + ":warp_stone";
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerRenderers(ItemModelMesher mesher) {
		ModelResourceLocation resource = new ModelResourceLocation(ModInfo.ID + ":warp_stone", "inventory");
		for (int i = 0; i < BlockWarpStone.EnumWarpSong.values().length; ++i) {
			mesher.register(this, i, resource); // all have the same texture
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		AbstractZeldaSong song = BlockWarpStone.EnumWarpSong.byMetadata(stack.getItemDamage()).getWarpSong();
		if (song != null) {
			list.add(StatCollector.translateToLocalFormatted("tooltip.zss.block.warp_stone.desc", EnumChatFormatting.GOLD + song.getDisplayName()));
		}
	}
}
