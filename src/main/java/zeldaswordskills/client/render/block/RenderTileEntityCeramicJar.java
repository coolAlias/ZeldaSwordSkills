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

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zeldaswordskills.block.tileentity.TileEntityCeramicJar;
import zeldaswordskills.client.model.ModelCeramicJar;
import zeldaswordskills.lib.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Because I'm lazy and can't figure out how to render a model from ISimpleBlockRenderingHandler... derp.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderTileEntityCeramicJar extends TileEntitySpecialRenderer
{
	/** Ceramic jar model texture */
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID + ":textures/entity/ceramic_jar.png");

	private ModelCeramicJar model = new ModelCeramicJar();

	public RenderTileEntityCeramicJar() {}

	@Override
	public void renderTileEntityAt(TileEntity te, double dx, double dy, double dz, float partialTick) {
		renderTileEntityJarAt((TileEntityCeramicJar) te, dx, dy, dz, partialTick);
	}

	/**
	 * Renders the TileEntity for the jar at a position.
	 */
	public void renderTileEntityJarAt(TileEntityCeramicJar jar, double dx, double dy, double dz, float partialTick) {
		bindTexture(texture);
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glTranslated(dx + 0.5D, dy + 1.485D, dz + 0.5D);
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		model.renderAll();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
