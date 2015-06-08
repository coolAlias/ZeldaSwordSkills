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

package zeldaswordskills.client.render.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import zeldaswordskills.block.tileentity.TileEntityCeramicJar;

public class RenderTileEntityCeramicJar extends TileEntitySpecialRenderer
{
	private final RenderItem renderItem;

	public RenderTileEntityCeramicJar() {
		this.renderItem = Minecraft.getMinecraft().getRenderItem();
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double dx, double dy, double dz, float partialTick, int blockDamageProgress) {
		renderPedestal((TileEntityCeramicJar) te, dx, dy, dz, partialTick);
	}

	private void renderPedestal(TileEntityCeramicJar jar, double dx, double dy, double dz, float partialTick) {
		ItemStack stack = jar.getStackInSlot(0);
		if (stack != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(dx + 0.45D, dy + 0.2D, dz + 0.45D);
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(0.425F, 0.425F, 0.425F);
			GlStateManager.rotate(45F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(30F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate((stack.getItem().isFull3D() ? 225.0F : 45.0F), 0.0F, 0.0F, 1.0F);
			bindTexture(TextureMap.locationBlocksTexture);
			renderItem.renderItemModel(stack);
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}
	}
}
