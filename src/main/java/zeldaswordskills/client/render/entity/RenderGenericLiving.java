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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.client.model.IModelBiped;
import zeldaswordskills.client.render.entity.layers.LayerGenericHeldItem;

/**
 * 
 * Renderer for generic {@link EntityLivingBase} entities with a single texture and set scale.
 * 
 * Animations are handled via the model methods:
 * {@link ModelBase#setRotationAngles(float, float, float, float, float, float, Entity) setRotationAngles}
 * {@link ModelBase#setLivingAnimations(EntityLivingBase, float, float, float) setLivingAnimations}.
 * 
 * Child versions of the entity are rendered at half scale.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderGenericLiving extends RenderLiving<EntityLiving>
{
	private final ResourceLocation texture;
	private final float scale;

	/**
	 * @param model			May be either an IModelBiped or a ModelBiped; any animations need to be handled in the model class directly
	 * @param scale			Scale of the full size model; child versions will render at half this scale
	 * @param texturePath	Be sure to prefix with the Mod ID if needed, otherwise it will use the Minecraft texture path
	 */
	public RenderGenericLiving(RenderManager renderManager, ModelBase model, float shadowSize, float scale, String texturePath) {
		super(renderManager, model, shadowSize);
		this.texture = new ResourceLocation(texturePath);
		this.scale = scale;
		if (model instanceof IModelBiped) {
			this.addLayer(new LayerCustomHead(((IModelBiped) model).getHeadModel()));
			this.addLayer(new LayerGenericHeldItem(this));
		} else if (model instanceof ModelBiped) {
			this.addLayer(new LayerCustomHead(((ModelBiped) model).bipedHead));
			this.addLayer(new LayerHeldItem(this));
		}
	}

	@Override
	public void doRender(EntityLiving entity, double dx, double dy, double dz, float yaw, float partialTick) {
		if (entity instanceof IBossDisplayData) {
			BossStatus.setBossStatus((IBossDisplayData) entity, true);
		}
		super.doRender(entity, dx, dy, dz, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLiving entity) {
		return texture;
	}

	@Override
	protected void preRenderCallback(EntityLiving entity, float partialTick) {
		float f = scale;
		if (entity.isChild()) {
			f = (float)((double) f * 0.5D);
		}
		GlStateManager.scale(f, f, f);
	}

	public static class Factory implements IRenderFactory<EntityLiving>
	{
		protected final ModelBase model;
		protected final float shadowSize;
		protected final float scale;
		protected final String texturePath;
		public Factory(ModelBase model, float shadowSize, float scale, String texturePath) {
			this.model = model;
			this.shadowSize = shadowSize;
			this.scale = scale;
			this.texturePath = texturePath;
		}
		@Override
		public Render<? super EntityLiving> createRenderFor(RenderManager manager) {
			return new RenderGenericLiving(manager, this.model, this.shadowSize, this.scale, this.texturePath);
		}
	}
}
