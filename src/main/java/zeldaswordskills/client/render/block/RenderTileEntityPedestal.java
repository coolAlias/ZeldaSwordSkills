/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.client.render.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.block.tileentity.TileEntityPedestal;

@SideOnly(Side.CLIENT)
public class RenderTileEntityPedestal extends TileEntitySpecialRenderer<TileEntityPedestal>
{
	public RenderTileEntityPedestal() {}

	@Override
	public void renderTileEntityAt(TileEntityPedestal pedestal, double dx, double dy, double dz, float partialTick, int blockDamageProgress) {
		ItemStack sword = pedestal.getSword();
		if (sword != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(dx + 0.5D, dy + 0.9D, dz + 0.5D);
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(1F, 1F, 1F);
			GlStateManager.rotate(pedestal.getOrientation() == 0 ? 0F : 90F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(225.0F, 0.0F, 0.0F, 1.0F);
			Minecraft.getMinecraft().getRenderItem().renderItem(sword, ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}
	}
}
