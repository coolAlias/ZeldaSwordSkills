/**
    Copyright (C) <2014> <coolAlias>

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

import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderGiantLever implements ISimpleBlockRenderingHandler
{
	public static final int renderId = RenderingRegistry.getNextAvailableRenderId();

	public RenderGiantLever() {}

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
		IIcon icon = block.getIcon(0, meta);
		ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int l = world.getBlockMetadata(x, y, z);
		int i1 = l & 7;
		boolean flag = (l & 8) > 0;
		Tessellator tessellator = Tessellator.instance;
		boolean flag1 = renderer.hasOverrideBlockTexture();
		if (!flag1) {
			renderer.setOverrideBlockTexture(renderer.getBlockIcon(Blocks.obsidian));
		}

		switch(i1) {
		case 0: renderer.setRenderBounds(0.125F, 0.625F, 0.2F, 0.875F, 1.0F, 0.8F); break;
		case 1: renderer.setRenderBounds(0.0F, 0.125F, 0.2F, 0.375F, 0.875F, 0.8F); break;
		case 2: renderer.setRenderBounds(0.625F, 0.125F, 0.2F, 1.0F, 0.875F, 0.8F); break;
		case 3: renderer.setRenderBounds(0.2F, 0.125F, 0.0F, 0.8F, 0.875F, 0.375F); break;
		case 4: renderer.setRenderBounds(0.2F, 0.125F, 0.625F, 0.8F, 0.875F, 1.0F); break;
		case 5: renderer.setRenderBounds(0.2F, 0.0F, 0.125F, 0.8F, 0.375F, 0.875F); break;
		case 6: renderer.setRenderBounds(0.125F, 0.0F, 0.2F, 0.875F, 0.375F, 0.8F); break;
		case 7: renderer.setRenderBounds(0.2F, 0.625F, 0.125F, 0.8F, 1.0F, 0.875F); break;
		}
		// renders the cobblestone base
		renderer.renderStandardBlock(block, x, y, z);

		if (!flag1) {
			renderer.clearOverrideBlockTexture();
		}

		tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		IIcon iicon = renderer.getBlockIconFromSide(block, 0);

		if (renderer.hasOverrideBlockTexture()) {
			iicon = renderer.overrideBlockTexture;
		}

		double d0 = (double) iicon.getMinU();
		double d1 = (double) iicon.getMinV();
		double d2 = (double) iicon.getMaxU();
		double d3 = (double) iicon.getMaxV();
		Vec3[] avec3 = new Vec3[8];
		float f3 = 0.125F; // determines thickness of lever
		float f4 = 0.125F; // determines thickness of lever
		float f5 = 0.625F; // determines length of lever
		avec3[0] = Vec3.createVectorHelper((double)(-f3), 0.0D, (double)(-f4));
		avec3[1] = Vec3.createVectorHelper((double) f3, 0.0D, (double)(-f4));
		avec3[2] = Vec3.createVectorHelper((double) f3, 0.0D, (double) f4);
		avec3[3] = Vec3.createVectorHelper((double)(-f3), 0.0D, (double) f4);
		avec3[4] = Vec3.createVectorHelper((double)(-f3), (double) f5, (double)(-f4));
		avec3[5] = Vec3.createVectorHelper((double) f3, (double) f5, (double)(-f4));
		avec3[6] = Vec3.createVectorHelper((double) f3, (double) f5, (double) f4);
		avec3[7] = Vec3.createVectorHelper((double)(-f3), (double) f5, (double) f4);

		for (int j1 = 0; j1 < 8; ++j1) {
			if (flag) {
				avec3[j1].zCoord -= f3;
				avec3[j1].rotateAroundX(((float) Math.PI * 2F / 9F));
			} else {
				avec3[j1].zCoord += f3;
				avec3[j1].rotateAroundX(-((float) Math.PI * 2F / 9F));
			}

			if (i1 == 0 || i1 == 7) {
				avec3[j1].rotateAroundZ((float) Math.PI);
			}
			if (i1 == 6 || i1 == 0) {
				avec3[j1].rotateAroundY(((float) Math.PI / 2F));
			}
			if (i1 > 0 && i1 < 5) {
				avec3[j1].yCoord -= (f5 / 2.0D);
				avec3[j1].rotateAroundX(((float) Math.PI / 2F));
				switch(i1) {
				case 1: avec3[j1].rotateAroundY(-((float) Math.PI / 2F)); break;
				case 2: avec3[j1].rotateAroundY(((float) Math.PI / 2F)); break;
				case 3: avec3[j1].rotateAroundY((float) Math.PI); break;
				case 4: avec3[j1].rotateAroundY(0.0F); break;
				default: break;
				}
				avec3[j1].xCoord += (double) x + (i1 == 1 ? 0.625D : i1 == 2 ? 0.375D : 0.5D);
				avec3[j1].yCoord += (double)((float) y + 0.5F);
				avec3[j1].zCoord += (double) z + (i1 == 3 ? 0.625D : i1 == 4 ? 0.375D : 0.5D);
			} else if (i1 != 0 && i1 != 7) {
				avec3[j1].xCoord += (double) x + 0.5D;
				avec3[j1].yCoord += (double)((float) y + 0.375F);
				avec3[j1].zCoord += (double) z + 0.5D;
			} else {
				avec3[j1].xCoord += (double) x + 0.5D;
				avec3[j1].yCoord += (double)((float) y + 0.625F);
				avec3[j1].zCoord += (double) z + 0.5D;
			}
		}

		Vec3 vec33 = null;
		Vec3 vec3 = null;
		Vec3 vec31 = null;
		Vec3 vec32 = null;

		for (int k1 = 0; k1 < 6; ++k1) {
			if (k1 == 0) {
				d0 = (double)iicon.getInterpolatedU(6.0D);
				d1 = (double)iicon.getInterpolatedV(6.0D);
				d2 = (double)iicon.getInterpolatedU(10.0D);
				d3 = (double)iicon.getInterpolatedV(8.0D);
			} else if (k1 == 2) {
				d0 = (double)iicon.getInterpolatedU(6.0D);
				d1 = (double)iicon.getInterpolatedV(6.0D);
				d2 = (double)iicon.getInterpolatedU(10.0D);
				d3 = (double)iicon.getMaxV();
			}
			switch(k1) {
			case 0:
				vec33 = avec3[0];
				vec3 = avec3[1];
				vec31 = avec3[2];
				vec32 = avec3[3];
				break;
			case 1:
				vec33 = avec3[7];
				vec3 = avec3[6];
				vec31 = avec3[5];
				vec32 = avec3[4];
				break;
			case 2:
				vec33 = avec3[1];
				vec3 = avec3[0];
				vec31 = avec3[4];
				vec32 = avec3[5];
				break;
			case 3:
				vec33 = avec3[2];
				vec3 = avec3[1];
				vec31 = avec3[5];
				vec32 = avec3[6];
				break;
			case 4:
				vec33 = avec3[3];
				vec3 = avec3[2];
				vec31 = avec3[6];
				vec32 = avec3[7];
				break;
			case 5:
				vec33 = avec3[0];
				vec3 = avec3[3];
				vec31 = avec3[7];
				vec32 = avec3[4];
				break;
			}

			tessellator.addVertexWithUV(vec33.xCoord, vec33.yCoord, vec33.zCoord, d0, d3);
			tessellator.addVertexWithUV(vec3.xCoord, vec3.yCoord, vec3.zCoord, d2, d3);
			tessellator.addVertexWithUV(vec31.xCoord, vec31.yCoord, vec31.zCoord, d2, d1);
			tessellator.addVertexWithUV(vec32.xCoord, vec32.yCoord, vec32.zCoord, d0, d1);
		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return renderId;
	}
}
