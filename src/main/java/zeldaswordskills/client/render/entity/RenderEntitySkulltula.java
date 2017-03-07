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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.mobs.EntitySkulltula;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class RenderEntitySkulltula extends RenderSpider<EntitySkulltula>
{
	private static final ResourceLocation base = new ResourceLocation(ModInfo.ID, "textures/entity/skulltula.png");
	private static final ResourceLocation golden = new ResourceLocation(ModInfo.ID, "textures/entity/skulltula_gold.png");

	public RenderEntitySkulltula(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected void rotateCorpse(EntitySkulltula entity, float f5, float f2, float partialTicks) {
		if (entity.deathTime > 0) {
			GlStateManager.rotate(180.0F - f2, 0.0F, 1.0F, 0.0F);
			float f3 = ((float) entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
			f3 = MathHelper.sqrt_float(f3);
			if (f3 > 1.0F) {
				f3 = 1.0F;
			}
			GlStateManager.rotate(f3 * this.getDeathMaxRotation(entity), 0.0F, 0.0F, 1.0F);
		} else if (entity.isPerched()) {
			BlockPos pos = new BlockPos(entity);
			EnumFacing face = EnumFacing.EAST;
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (entity.worldObj.isSideSolid(pos.offset(facing), facing.getOpposite())) {
					face = facing;
					break;
				}
			}
			switch (face) {
			case NORTH: GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F); break;
			case SOUTH: GlStateManager.rotate(270.0F, 1.0F, 0.0F, 0.0F); break;
			case EAST: GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F); break;
			case WEST: GlStateManager.rotate(270.0F, 0.0F, 0.0F, 1.0F); break;
			default:
			}
			GlStateManager.rotate(180.0F - f2, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(0.0F, -(entity.height * 0.8), 0.0F);
		} else {
			GlStateManager.rotate(180.0F - f2, 0.0F, 1.0F, 0.0F);
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySkulltula entity) {
		return entity.isGolden() ? golden : base;
	}

	public static class Factory implements IRenderFactory<EntitySkulltula> {
		@Override
		public Render<? super EntitySkulltula> createRenderFor(RenderManager manager) {
			return new RenderEntitySkulltula(manager);
		}
	}
}
