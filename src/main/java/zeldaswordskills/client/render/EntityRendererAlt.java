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

package zeldaswordskills.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author Thanks to Noppes for the original code
 *
 */
@SideOnly(Side.CLIENT)
public class EntityRendererAlt extends EntityRenderer
{
	private final Minecraft mc;
	private float ySize = 3.0F;
	private float offsetY = ySize;

	public EntityRendererAlt(Minecraft mc) {
		super(mc);
		this.mc = mc;
	}

	@Override
	public void updateCameraAndRender(float partialTick) {
		if (mc.thePlayer == null || mc.thePlayer.isPlayerSleeping()) {
			super.updateCameraAndRender(partialTick);
			return;
		}
		mc.thePlayer.yOffset -= ySize;
		super.updateCameraAndRender(partialTick);
		mc.thePlayer.yOffset = 1.62F;
	}

	@Override
	public void getMouseOver(float partialTick) {
		if (mc.thePlayer == null || mc.thePlayer.isPlayerSleeping()) {
			super.getMouseOver(partialTick);
			return;
		}
		/* 
		 * Need to adjust the y position to get a mouseover at eye-level
		 * Clicking blocks that appear to be close (i.e. at eye level when
		 * big) does not work sometimes, since the server still checks
		 * distance apparently based on posY, rather than eye level
		 */
		mc.thePlayer.posY += offsetY;
		mc.thePlayer.prevPosY += offsetY;
		mc.thePlayer.lastTickPosY += offsetY;
		super.getMouseOver(partialTick);
		mc.thePlayer.posY -= offsetY;
		mc.thePlayer.prevPosY -= offsetY;
		mc.thePlayer.lastTickPosY -= offsetY;
	}
}
