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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.client.model.ModelBomb;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * Renders flashing bomb entity.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderEntityBomb extends Render
{
	/** Base bomb textures */
	public static final ResourceLocation[] bombTextures = new ResourceLocation[BombType.values().length];
	/** Flashing bomb textures */
	public static final ResourceLocation[] flashTextures = new ResourceLocation[BombType.values().length];

	static {
		for (BombType type : BombType.values()) {
			bombTextures[type.ordinal()] = new ResourceLocation(ModInfo.ID, "textures/entity/bomb_" + type.unlocalizedName + ".png");
			flashTextures[type.ordinal()] = new ResourceLocation(ModInfo.ID, "textures/entity/bomb_" + type.unlocalizedName + "_flash.png");
		}
	}

	protected ModelBase model;

	public RenderEntityBomb(RenderManager renderManager) {
		super(renderManager);
		model = new ModelBomb();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity bomb) {
		int i = ((EntityBomb) bomb).getType().ordinal();
		return (bomb.ticksExisted % 13 > 10) ? flashTextures[i] : bombTextures[i];
	}

	@Override
	public void doRender(Entity bomb, double x, double y, double z, float yaw, float partialTick) {
		renderEntityModel(bomb, x, y, z, yaw, partialTick);
	}

	public void renderEntityModel(Entity bomb, double x, double y, double z, float yaw, float partialTick) {
		GlStateManager.pushMatrix();
		float scale = bomb.ticksExisted % 13 > 10 ? 1.65F : 1.25F;
		bindTexture(getEntityTexture(bomb));
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
		GlStateManager.rotate(150, 1F, 1F, 300F);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0.0F, -1.0F, 0.0F);
		model.render(bomb, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0475F);
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}
}
