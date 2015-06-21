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

package zeldaswordskills.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.item.ICyclableItem;
import zeldaswordskills.ref.Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiItemModeOverlay extends Gui
{
	private final Minecraft mc;
	private final RenderItem itemRender;

	public GuiItemModeOverlay() {
		mc = Minecraft.getMinecraft();
		itemRender = new RenderItem();
	}

	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent.Post event) {
		ItemStack stack = mc.thePlayer.getHeldItem();
		if (event.type != ElementType.EXPERIENCE || stack == null || !(stack.getItem() instanceof ICyclableItem)) {
			return;
		}
		stack = ((ICyclableItem) stack.getItem()).getRenderStackForMode(stack, mc.thePlayer);
		if (stack != null) {
			int xPos = (Config.isItemModeLeft() ? 2 : event.resolution.getScaledWidth() - 18);
			int yPos = (Config.isItemModeTop() ? 2 : event.resolution.getScaledHeight() - 18);
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			// alpha test and blend needed due to vanilla or Forge rendering bug
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glTranslatef(0.0F, 0.0F, 32.0F);
			FontRenderer font = stack.getItem().getFontRenderer(stack);
			if (font == null) font = mc.fontRenderer;
			itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, xPos, yPos);
			String text = (stack.stackSize == 1 ? null : String.valueOf(stack.stackSize));
			itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), stack, xPos, yPos, text);
			GL11.glPopAttrib();
		}
	}
}
