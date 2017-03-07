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

package zeldaswordskills.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.projectile.EntityBoomerang;

@SideOnly(Side.CLIENT)
public class RenderEntityBoomerang extends Render<EntityBoomerang>
{
	private final RenderItem renderItem;

	public RenderEntityBoomerang(RenderManager renderManager) {
		super(renderManager);
		this.renderItem = Minecraft.getMinecraft().getRenderItem();
	}

	@Override
	public void doRender(EntityBoomerang entity, double x, double y, double z, float yaw, float partialTick) {
		ItemStack boomerang = entity.getBoomerang();
		if (boomerang != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			float rotation = ((float) entity.ticksExisted + partialTick) * 50;
			while (rotation > 360) rotation -= 360;
			GlStateManager.rotate(entity.rotationYaw + (entity.prevRotationYaw - entity.rotationYaw) * partialTick - 60.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(Math.abs(entity.rotationPitch + (entity.prevRotationPitch - entity.rotationPitch)) * partialTick - rotation, 0.0F, 0.0F, 1.0F);
			bindTexture(TextureMap.locationBlocksTexture);
			renderItem.renderItem(boomerang, renderItem.getItemModelMesher().getItemModel(boomerang));
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}
		super.doRender(entity, x, y, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBoomerang entity) {
		return null;
	}

	public static class Factory implements IRenderFactory<EntityBoomerang> {
		@Override
		public Render<? super EntityBoomerang> createRenderFor(RenderManager manager) {
			return new RenderEntityBoomerang(manager);
		}
	}
}
