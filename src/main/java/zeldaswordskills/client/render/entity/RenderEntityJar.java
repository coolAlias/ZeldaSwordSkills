/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.entity.projectile.EntityCeramicJar;

@SideOnly(Side.CLIENT)
public class RenderEntityJar extends Render<EntityCeramicJar>
{
	private final RenderItem renderItem;
	private static final ItemStack jar = new ItemStack(ZSSBlocks.ceramicJar);

	public RenderEntityJar(RenderManager renderManager) {
		super(renderManager);
		this.renderItem = Minecraft.getMinecraft().getRenderItem();
	}

	@Override
	public void doRender(EntityCeramicJar entity, double dx, double dy, double dz, float yaw, float partialTick) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(dx, dy, dz);
		GlStateManager.rotate(yaw, 0, 1, 0);
		float roll = ((float) entity.ticksExisted + partialTick) * 7;
		while (roll > 360) { roll -= 360; }
		GlStateManager.rotate(roll, -0.25F, 0.1F, 0);
		GlStateManager.scale(1.5F, 1.5F, 1.5F);
		renderItem.renderItem(jar, renderItem.getItemModelMesher().getItemModel(jar));
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCeramicJar entity) {
		return TextureMap.locationBlocksTexture;
	}

	public static class Factory implements IRenderFactory<EntityCeramicJar> {
		@Override
		public Render<? super EntityCeramicJar> createRenderFor(RenderManager manager) {
			return new RenderEntityJar(manager);
		}
	}
}
