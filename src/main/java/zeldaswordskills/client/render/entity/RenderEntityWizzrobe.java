/**
    Copyright (C) <2018> <coolAlias>

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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.client.model.ModelCube;
import zeldaswordskills.client.model.ModelWizzrobe;
import zeldaswordskills.entity.mobs.EntityWizzrobe;
import zeldaswordskills.entity.projectile.EntityMagicSpell;

@SideOnly(Side.CLIENT)
public class RenderEntityWizzrobe extends RenderLiving<EntityWizzrobe>
{
	/** Dummy entity for the model cube rendering */
	protected final EntityMagicSpell spell;

	/** Wizzrobe model provides easy means of knowing if spell should be rendered */
	protected final ModelWizzrobe model;

	/** Boxes for spell rendering */
	protected final ModelCube box1 = new ModelCube(4), box2 = new ModelCube(4);

	protected final float scale;

	protected final ResourceLocation texture;

	public RenderEntityWizzrobe(RenderManager manager, ModelWizzrobe model, float scale, ResourceLocation texture) {
		super(manager, model, 0.5F);
		this.model = model;
		this.scale = scale;
		this.texture = texture;
		this.spell = new EntityMagicSpell(Minecraft.getMinecraft().theWorld);
	}

	@Override
	protected void preRenderCallback(EntityWizzrobe entity, float partialTick) {
		GlStateManager.scale(scale, scale, scale);
	}

	@Override
	public void doRender(EntityWizzrobe entity, double dx, double dy, double dz, float yaw, float partialTick) {
		if (entity instanceof IBossDisplayData) {
			BossStatus.setBossStatus((IBossDisplayData) entity, true);
		}
		super.doRender(entity, dx, dy, dz, yaw, partialTick);
		if (model.atPeak) {
			renderSpell(entity, dx, dy, dz, yaw, partialTick);
		}
	}

	private void renderSpell(EntityWizzrobe wizzrobe, double dx, double dy, double dz, float yaw, float partialTick) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		Vec3 vec3 = new Vec3(dx, dy, dz).normalize();
		GlStateManager.translate(dx - vec3.xCoord, dy + wizzrobe.getEyeHeight(), dz - vec3.zCoord);
		GlStateManager.scale(scale, scale, scale);
		float roll = ((float) wizzrobe.getCurrentCastingTime() + partialTick) * 40;
		while (roll > 360) roll -= 360;
		GlStateManager.rotate(yaw, 0, 1, 0);
		GlStateManager.rotate(roll, 0.8F, 0F, -0.6F);
		bindTexture(wizzrobe.getMagicType().getEntityTexture());
		box1.render(spell);
		GlStateManager.rotate(45, 1, 0, 1);
		box2.render(spell);
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWizzrobe entity) {
		return this.texture;
	}

	public static class Factory implements IRenderFactory<EntityWizzrobe>
	{
		protected final ModelWizzrobe model;
		protected final float shadowSize;
		protected final ResourceLocation texture;
		public Factory(ModelWizzrobe model, float shadowSize, ResourceLocation texture) {
			this.model = model;
			this.shadowSize = shadowSize;
			this.texture = texture;
		}

		@Override
		public Render<? super EntityWizzrobe> createRenderFor(RenderManager manager) {
			return new RenderEntityWizzrobe(manager, this.model, this.shadowSize, this.texture);
		}
	}
}
