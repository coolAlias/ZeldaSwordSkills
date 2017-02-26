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

package zeldaswordskills.client.gui;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.entity.player.ZSSPlayerWallet.EnumWallet;
import zeldaswordskills.item.ItemRupee;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;

@SideOnly(Side.CLIENT)
public class GuiWalletOverlay extends AbstractGuiOverlay
{
	private static final int ICON_SIZE = 16;
	private final RenderItem itemRender;
	private final ItemStack icon_small, icon_large, icon_max;
	private ZSSPlayerWallet wallet;

	public GuiWalletOverlay(Minecraft mc) {
		super(mc);
		this.itemRender = new RenderItem();
		this.icon_small = new ItemStack(ZSSItems.rupee, 1, ItemRupee.Rupee.GREEN_RUPEE.ordinal());
		this.icon_large = new ItemStack(ZSSItems.rupee, 1, ItemRupee.Rupee.SILVER_RUPEE.ordinal());
		this.icon_max = new ItemStack(ZSSItems.rupee, 1, ItemRupee.Rupee.GOLD_RUPEE.ordinal());
	}

	@Override
	public boolean shouldRender() {
		if (!Config.isWalletHudEnabled) {
			return false;
		}
		ZSSPlayerWallet wallet = ZSSPlayerWallet.get(this.mc.thePlayer);
		return wallet.getRupees() > 0 || (Config.isWalletHudAlwaysOn && wallet.getCapacity() > 0);
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return Config.walletHudHAlign;
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return Config.walletHudVAlign;
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		this.wallet = ZSSPlayerWallet.get(this.mc.thePlayer);
		String text = String.valueOf(this.wallet.getRupees());
		this.width = Math.max(ICON_SIZE, this.mc.fontRenderer.getStringWidth(text));
		this.height = ICON_SIZE + (this.mc.fontRenderer.FONT_HEIGHT / 2); // ICON_SIZE because FONT_HEIGHT seems to include some padding
		// Adjustments so rupee icon stays in one spot no matter how many rupees are in the wallet
		int offsetX = 0;
		if (this.getHorizontalAlignment() == HALIGN.RIGHT) {
			offsetX += DEFAULT_PADDING + (ICON_SIZE - this.getWidth());
		} else if (this.getHorizontalAlignment() == HALIGN.CENTER) {
			offsetX += DEFAULT_PADDING + this.getWidth() / 2;
		}
		this.setPosX(resolution, this.getOffsetX(offsetX) + Config.walletHudOffsetX);
		this.setPosY(resolution, this.getOffsetY(DEFAULT_PADDING) + Config.walletHudOffsetY);
	}

	@Override
	public void render(ScaledResolution resolution) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);
		// alpha test and blend needed due to vanilla or Forge rendering bug
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		ItemStack icon = (this.wallet.getCapacity() < EnumWallet.BIG.capacity ? this.icon_small : (this.wallet.getCapacity() < EnumWallet.TYCOON.capacity ? this.icon_large : this.icon_max));
		FontRenderer font = icon.getItem().getFontRenderer(icon);
		if (font == null) font = this.mc.fontRenderer;
		int xPos = this.getLeft();
		int yPos = this.getTop();
		this.itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), icon, xPos, yPos);
		String text = String.valueOf(this.wallet.getRupees());
		// Adjust offset for string rendition of rupees
		int xMod = DEFAULT_PADDING;
		if (this.getHorizontalAlignment() == HALIGN.LEFT) {
			xMod += Math.max(0, font.getStringWidth(text) - ICON_SIZE - DEFAULT_PADDING);
		} else if (this.getHorizontalAlignment() == HALIGN.RIGHT) {
			xMod += Math.max(0, font.getStringWidth(text) - this.getWidth());
		}
		this.itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), icon, xPos + xMod, yPos + 4, text);
		GL11.glPopAttrib();
	}
}
