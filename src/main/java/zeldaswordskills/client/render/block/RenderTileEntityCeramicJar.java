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
import zeldaswordskills.block.tileentity.TileEntityCeramicJar;

public class RenderTileEntityCeramicJar extends TileEntitySpecialRenderer<TileEntityCeramicJar>
{
	public RenderTileEntityCeramicJar() {}

	@Override
	public void renderTileEntityAt(TileEntityCeramicJar jar, double dx, double dy, double dz, float partialTick, int blockDamageProgress) {
		ItemStack stack = jar.getStackInSlot(0);
		if (stack != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(dx + 0.45D, dy + 0.2D, dz + 0.45D);
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(0.425F, 0.425F, 0.425F);
			GlStateManager.rotate(45F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(30F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate((stack.getItem().isFull3D() ? 225.0F : 45.0F), 0.0F, 0.0F, 1.0F);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}
	}
}
