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

package zeldaswordskills.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zeldaswordskills.client.model.ModelCube;
import zeldaswordskills.client.model.ModelWizzrobe;
import zeldaswordskills.entity.mobs.EntityGrandWizzrobe;
import zeldaswordskills.entity.mobs.EntityWizzrobe;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityWizzrobe extends RenderLiving
{
	private static final ResourceLocation fireWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_fire.png");
	private static final ResourceLocation iceWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_ice.png");
	private static final ResourceLocation lightningWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_lightning.png");
	private static final ResourceLocation windWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_wind.png");
	private static final ResourceLocation grandFireWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_fire_grand.png");
	private static final ResourceLocation grandIceWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_ice_grand.png");
	private static final ResourceLocation grandLightningWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_lightning_grand.png");
	private static final ResourceLocation grandWindWizTexture = new ResourceLocation(ModInfo.ID, "textures/entity/wizzrobe_wind_grand.png");

	/** Dummy entity for the model cube rendering */
	private final EntityMagicSpell spell;

	/** Wizzrobe model provides easy means of knowing if spell should be rendered */
	private final ModelWizzrobe model;

	/** Boxes for spell rendering */
	private final ModelCube box1 = new ModelCube(4), box2 = new ModelCube(4);

	private final float scale;

	public RenderEntityWizzrobe(ModelWizzrobe model, float scale) {
		super(model, 0.5F);
		this.model = model;
		this.scale = scale;
		this.spell = new EntityMagicSpell(Minecraft.getMinecraft().theWorld);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
		GL11.glScalef(scale, scale, scale);
	}

	@Override
	public void doRender(EntityLiving entity, double dx, double dy, double dz, float yaw, float partialTick) {
		if (entity instanceof IBossDisplayData) {
			BossStatus.setBossStatus((IBossDisplayData) entity, true);
		}
		super.doRender(entity, dx, dy, dz, yaw, partialTick);
		if (model.atPeak) {
			renderSpell((EntityWizzrobe) entity, dx, dy, dz, yaw, partialTick);
		}
	}

	private void renderSpell(EntityWizzrobe wizzrobe, double dx, double dy, double dz, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		Vec3 vec3 = Vec3.createVectorHelper(dx, dy, dz).normalize();
		GL11.glTranslated(dx - vec3.xCoord, dy + wizzrobe.getEyeHeight(), dz - vec3.zCoord);
		GL11.glScalef(scale, scale, scale);
		float roll = ((float) wizzrobe.getCurrentCastingTime() + partialTick) * 40;
		while (roll > 360) roll -= 360;
		GL11.glRotatef(yaw, 0, 1, 0);
		GL11.glRotatef(roll, 0.8F, 0F, -0.6F);
		bindTexture(wizzrobe.getMagicType().getEntityTexture());
		Tessellator.instance.setBrightness(0xf000f0);
		box1.render(spell);
		GL11.glRotatef(45, 1, 0, 1);
		box2.render(spell);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		boolean grand = (entity instanceof EntityGrandWizzrobe);
		switch(((EntityWizzrobe) entity).getMagicType()) {
		case FIRE: return (grand ? grandFireWizTexture : fireWizTexture);
		case ICE: return (grand ? grandIceWizTexture : iceWizTexture);
		case LIGHTNING: return (grand ? grandLightningWizTexture : lightningWizTexture);
		default: return (grand ? grandWindWizTexture : windWizTexture);
		}
	}
}
