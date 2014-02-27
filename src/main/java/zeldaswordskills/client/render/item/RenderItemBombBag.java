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

package zeldaswordskills.client.render.item;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Used only to get the numbers rendering on the item
 *
 */
@SideOnly(Side.CLIENT)
public class RenderItemBombBag implements IItemRenderer
{
	/** Defines the zLevel of rendering of item on GUI. */
	public float zLevel;

	public RenderItemBombBag() {}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type == ItemRenderType.INVENTORY;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return type == ItemRenderType.ENTITY && (helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION);
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (type == ItemRenderType.INVENTORY) {
			Tessellator tessellator = Tessellator.instance;
			for (int i = 0; i < 3; ++i) {
				Icon icon = item.getItem().getIcon(item, i);
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(0, 16, zLevel, icon.getMinU(), icon.getMaxV());
				tessellator.addVertexWithUV(16, 16, zLevel, icon.getMaxU(), icon.getMaxV());
				tessellator.addVertexWithUV(16, 0, zLevel, icon.getMaxU(), icon.getMinV());
				tessellator.addVertexWithUV(0, 0, zLevel, icon.getMinU(), icon.getMinV());
				tessellator.draw();
			}
		}
	}
}
