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

package zeldaswordskills.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * 
 * @authors Hunternif, coolAlias
 *
 */
public class RenderHelperQ {

	public static void drawTexturedRect(ResourceLocation texture, double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight, double scale) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		double minU = (double)u / (double)imageWidth;
		double maxU = (double)(u + width) / (double)imageWidth;
		double minV = (double)v / (double)imageHeight;
		double maxV = (double)(v + height) / (double)imageHeight;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x + scale*(double)width, y + scale*(double)height, 0, maxU, maxV);
		tessellator.addVertexWithUV(x + scale*(double)width, y, 0, maxU, minV);
		tessellator.addVertexWithUV(x, y, 0, minU, minV);
		tessellator.addVertexWithUV(x, y + scale*(double)height, 0, minU, maxV);
		tessellator.draw();
	}

	public static void drawTexturedRect(ResourceLocation texture, double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight) {
		drawTexturedRect(texture, x, y, u, v, width, height, imageWidth, imageHeight, 1);
	}

	public static void drawFullTexture(ResourceLocation texture, double x, double y, int width, int height, double scale) {
		drawTexturedRect(texture, x, y, 0, 0, width, height, width, height, scale);
	}

	public static void drawFullTexture(ResourceLocation texture, double x, double y, int width, int height) {
		drawFullTexture(texture, x, y, width, height, 1);
	}

	/**
	 * Draws textured rectangle for texture already bound to Minecraft render engine
	 */
	public static void drawTexturedRect(double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight, double scale) {
		double minU = (double)u / (double)imageWidth;
		double maxU = (double)(u + width) / (double)imageWidth;
		double minV = (double)v / (double)imageHeight;
		double maxV = (double)(v + height) / (double)imageHeight;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x + scale*(double)width, y + scale*(double)height, 0, maxU, maxV);
		tessellator.addVertexWithUV(x + scale*(double)width, y, 0, maxU, minV);
		tessellator.addVertexWithUV(x, y, 0, minU, minV);
		tessellator.addVertexWithUV(x, y + scale*(double)height, 0, minU, maxV);
		tessellator.draw();
	}

	/**
	 * Draws textured rectangle for texture already bound to Minecraft render engine
	 * @param x				Starting x position on the screen at which to draw
	 * @param u				Starting x position in the texture file from which to draw
	 * @param width			Width of the section of texture to draw
	 * @param imageWidth	Full width of the texture file
	 */
	public static void drawTexturedRect(double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight) {
		drawTexturedRect(x, y, u, v, width, height, imageWidth, imageHeight, 1);
	}

	/** Draws textured rectangle for texture already bound to Minecraft render engine */
	public static void drawFullTexture(double x, double y, int width, int height, double scale) {
		drawTexturedRect(x, y, 0, 0, width, height, width, height, scale);
	}

	/** Draws textured rectangle for texture already bound to Minecraft render engine */
	public static void drawFullTexture(double x, double y, int width, int height) {
		drawFullTexture(x, y, width, height, 1);
	}

	public static void setGLColor(int color, float alpha) {
		float r = (float)(color >> 16 & 0xff)/256f;
		float g = (float)(color >> 8 & 0xff)/256f;
		float b = (float)(color & 0xff)/256f;
		GL11.glColor4f(r, g, b, alpha);
	}
}
