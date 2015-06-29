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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.gen.ISeedStructure;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A seed capable of spawning any {@link ISeedStructure} when right-clicking on a block.
 * 
 */
public class ItemBuilderSeed extends Item
{
	/** The structure that will generate upon right-clicking on a block */
	private final Class<? extends ISeedStructure> structure;

	/** The chat message to send to the player upon failing to build a structure */
	private String failMessage;

	/**
	 * @param structure		The structure class that will be generated
	 * @param failMessage	The message to display (via chat) when the structure fails to generate; may be null
	 * @param texture		Sets the texture string to: "{modid}:texture"
	 */
	public ItemBuilderSeed(int id, Class<? extends ISeedStructure> structure, String failMessage, String texture) {
		super(id);
		this.structure = structure;
		this.failMessage = failMessage;
		setTextureName(ModInfo.ID + ":" + texture);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			ISeedStructure seed = getSeedStructure(structure);
			if (seed != null) {
				if (seed.generate(world, player, x, y, z, side)) {
					if (!player.capabilities.isCreativeMode) {
						--stack.stackSize;
					}
					return true;
				} else if (failMessage != null && failMessage.length() > 0) {
					PlayerUtils.sendChat(player, failMessage);
				}
			}
			return false;
		}
		return true; // always swing item on client
	}

	/**
	 * Returns a new ISeedStructure instance of this structure, or null if not possible
	 */
	public static final ISeedStructure getSeedStructure(Class<? extends ISeedStructure> structure) {
		ISeedStructure seed = null;
		try {
			seed = structure.newInstance();
		} catch (Exception e) {
			;
		}
		return seed;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.1"));
	}
}
