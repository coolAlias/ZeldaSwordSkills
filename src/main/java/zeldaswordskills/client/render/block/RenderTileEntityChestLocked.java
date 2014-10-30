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

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import zeldaswordskills.block.BlockChestInvisible.TileEntityChestInvisible;
import zeldaswordskills.block.tileentity.TileEntityChestLocked;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Simple class to render the chest model
 *
 */
@SideOnly(Side.CLIENT)
public class RenderTileEntityChestLocked extends TileEntitySpecialRenderer
{
	/** Chest model texture */
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID + ":textures/entity/chest_locked.png");

	/** The normal small chest model. */
	private ModelChest chestModel = new ModelChest();

	public RenderTileEntityChestLocked() {}

	/**
	 * Renders the TileEntity for the chest at a position.
	 */
	public void renderTileEntityChestAt(TileEntityChestLocked chest, double dx, double dy, double dz, float partialTick) {
		if (chest instanceof TileEntityChestInvisible) {
			return;
		}
		int meta = (chest.hasWorldObj() ? chest.getBlockMetadata() : 0);
		bindTexture(texture);
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glTranslated(dx, dy + 1.0D, dz + 1.0D);
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		float rotation = (meta == 5 ? -90F : meta == 4 ? 90F : meta == 2 ? 180F : 0F);
		GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		chestModel.chestLid.rotateAngleX = 0F;
		chestModel.renderAll();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void renderTileEntityAt(TileEntity te, double dx, double dy, double dz, float partialTick) {
		renderTileEntityChestAt((TileEntityChestLocked) te, dx, dy, dz, partialTick);
	}
}
