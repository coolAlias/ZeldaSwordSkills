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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.entity.VanillaRupeeMerchant;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.inventory.ContainerRupeeMerchant;
import zeldaswordskills.inventory.ContainerRupeeMerchant.SlotRupeeWares;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.RupeeMerchantIndexPacket;
import zeldaswordskills.network.server.RupeeMerchantTogglePacket;
import zeldaswordskills.network.server.RupeeMerchantTransactionPacket;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public abstract class GuiRupeeMerchant extends GuiContainer
{
	protected static final ResourceLocation TEXTURE = new ResourceLocation(ModInfo.ID, "textures/gui/gui_rupee_shop.png");

	protected final IRupeeMerchant merchant;

	/** Usually the {@link #getMerchant()}, but can be set separately for e.g. vanilla villagers */
	protected EntityLivingBase entityToRender;

	/** Current shop mode - i.e. whether merchant is selling (player is buying) or buying (player is selling) items */
	protected final boolean getItemsToSell;

	/** Button used to purchase or sell selected item(s) */
	protected GuiButton trade;

	/** Translation string for trade button text */
	protected final String tradeText;

	/** Buttons for previous and next row of trades in the list */
	private GuiButton prev, next;

	/** Buttons for first and last row of trades in the list */
	private GuiButton first, last;

	/** Button for changing the shop mode from buying to selling or vice versa */
	private ButtonToggleMode toggleMode;

	/** Button for switching to vanilla IMerchant interface */
	private GuiButton toggleShop;

	/** Index of currently selected RupeeTrade slot used for rendering the selector box */
	protected int selectedIndex = -1;

	/** Index of the first trade to be displayed; see {@link ContainerRupeeMerchant#setSlotIndex} */
	private int currentIndex;

	/** Tracks current mouseX, used for rendering player model rotation. Defined as float, passed as int */
	private float xSize_lo;

	/** Tracks current mouseY, used for rendering player model rotation. Defined as float, passed as int. */
	private float ySize_lo;

	/** Whether to allow mouse wheel to scroll inventory or not */
	private boolean canScroll;

	public GuiRupeeMerchant(ContainerRupeeMerchant container, EntityPlayer player, IRupeeMerchant merchant, boolean getItemsToSell) {
		super(container);
		this.merchant = merchant;
		if (merchant instanceof EntityLivingBase) {
			this.entityToRender = (EntityLivingBase) merchant;
		}
		this.getItemsToSell = getItemsToSell;
		this.ySize = 166;
		this.tradeText = (getItemsToSell ? "gui.zss.button.purchase.text" : "gui.zss.button.sell.text");
	}

	public IRupeeMerchant getMerchant() {
		return this.merchant;
	}

	/**
	 * Sets the entity to render if it is an EntityLivingBase
	 */
	public GuiRupeeMerchant setRenderEntity(Entity entity) {
		this.entityToRender = (entity instanceof EntityLivingBase ? (EntityLivingBase) entity : this.entityToRender);
		return this;
	}

	/**
	 * Returns the current RupeeTrade index per the Container
	 */
	public int getCurrentIndex() {
		return this.currentIndex;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		String label = I18n.format(this.tradeText);
		int stringWidth = 10 + this.mc.fontRenderer.getStringWidth(label);
		this.trade = new GuiButton(0, (this.width - stringWidth) / 2, this.guiTop + 61, stringWidth, 20, label);
		this.buttonList.add(this.trade);
		this.first = new GuiRupeeMerchant.ButtonNextPrev(1, this.guiLeft + 62, this.guiTop + 46, 14, 13, 26, true);
		this.buttonList.add(this.first);
		this.last = new GuiRupeeMerchant.ButtonNextPrev(2, this.guiLeft + 153, this.guiTop + 46, 14, 13, 26, false);
		this.buttonList.add(this.last);
		this.prev = new GuiRupeeMerchant.ButtonNextPrev(3, this.guiLeft + 78, this.guiTop + 46, true);
		this.buttonList.add(this.prev);
		this.next = new GuiRupeeMerchant.ButtonNextPrev(4, this.guiLeft + 141, this.guiTop + 46, false);
		this.buttonList.add(this.next);
		this.toggleMode = new GuiRupeeMerchant.ButtonToggleMode(5, this.guiLeft + this.xSize, this.guiTop, this.getItemsToSell);
		this.buttonList.add(this.toggleMode);
		if (this.merchant instanceof IMerchant || this.merchant instanceof VanillaRupeeMerchant) {
			// No way to determine if IMerchant has any trades from here, so just add the button
			this.toggleShop = new GuiButtonToggleTradeInterface(6, true, this.guiLeft + this.xSize, this.guiTop + 20);
			this.buttonList.add(this.toggleShop);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialRenderTick){
		super.drawScreen(mouseX, mouseY, partialRenderTick);
		this.xSize_lo = mouseX;
		this.ySize_lo = mouseY;
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
		if (trades != null && trades.size() > 4) {
			this.canScroll = this.isMouseInRegion(mouseX, mouseY, this.guiLeft + 61, this.guiLeft + 168, this.guiTop + 8, this.guiTop + 44);
		} else {
			this.canScroll = false;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.trade.enabled = this.isTradeEnabled();
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
		if (trades != null && trades.size() > 4) {
			this.first.enabled = this.currentIndex > 0;
			this.last.enabled = this.currentIndex < (trades.size() - 4);
			this.prev.enabled = this.currentIndex > 0;
			this.next.enabled = this.currentIndex < (trades.size() - 4);
		}
		this.toggleMode.visible = ((ContainerRupeeMerchant) this.inventorySlots).showTab;
	}

	/**
	 * Called from {@link #updateScreen()} to determine trade button status
	 * @return whether or not the trade button should be enabled
	 */
	protected abstract boolean isTradeEnabled();

	/**
	 * Called when the trade button is clicked
	 */
	protected abstract void useTrade();

	/**
	 * Switch shop mode from buying to selling or vice versa
	 */
	protected void switchShopMode() {
		RupeeTradeList<RupeeTrade> list = this.merchant.getRupeeTrades(!this.getItemsToSell);
		if (list != null && !list.isEmpty()) {
			PacketDispatcher.sendToServer(new RupeeMerchantTogglePacket(this.getItemsToSell));
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == this.trade) {
			this.useTrade();
		}
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
		int size = (trades == null ? 0 : trades.size());
		int prevIndex = this.currentIndex;
		if (button == this.prev) {
			this.currentIndex = ((ContainerRupeeMerchant) this.inventorySlots).setCurrentIndex(this.currentIndex - 4);
		} else if (button == this.next) {
			this.currentIndex = ((ContainerRupeeMerchant) this.inventorySlots).setCurrentIndex(this.currentIndex + 4);
		} else if (button == this.first) {
			this.currentIndex = ((ContainerRupeeMerchant) this.inventorySlots).setCurrentIndex(0);
		} else if (button == this.last) {
			this.currentIndex = ((ContainerRupeeMerchant) this.inventorySlots).setCurrentIndex(size);
		} else if (button == this.toggleMode) {
			this.switchShopMode();
		}
		if (this.currentIndex != prevIndex) {
			this.selectedIndex = -1;
			PacketDispatcher.sendToServer(new RupeeMerchantIndexPacket(this.currentIndex));
		}
	}

	/** Returns true if the mouse is within the real-screen coordinates specified */
	private boolean isMouseInRegion(int mouseX, int mouseY, int x1, int x2, int y1, int y2) {
		return (mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2);
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotIndex, int keyCode, int action) {
		super.handleMouseClick(slot, slotIndex, keyCode, action);
		if (slotIndex > 0 && slot instanceof SlotRupeeWares && slot.getHasStack()) {
			int index = ((ContainerRupeeMerchant) this.inventorySlots).convertSlotIndex(slotIndex);
			RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
			if (trades != null && index > -1 && index < trades.size()) {
				RupeeTrade trade = trades.get(index);
				if (trade != null && !trade.isDisabled()) {
					this.selectedIndex = slotIndex;
				}
			}
		}
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		if (this.canScroll) {
			int i = Mouse.getEventDWheel();
			boolean indexChanged = false;
			RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
			if (trades == null || trades.isEmpty()) {
				// nothing to do
			} else if (i < 0 && this.currentIndex < trades.size() - 4) {
				++this.currentIndex;
				indexChanged = true;
			} else if (i > 0 && this.currentIndex > 0) {
				--this.currentIndex;
				indexChanged = true;
			}
			if (indexChanged) {
				this.selectedIndex = -1;
				((ContainerRupeeMerchant) this.inventorySlots).setCurrentIndex(this.currentIndex);
				PacketDispatcher.sendToServer(new RupeeMerchantIndexPacket(this.currentIndex));
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
		if (trades == null || trades.isEmpty()) {
			return;
		}
		for (int i = ContainerRupeeMerchant.WARES_START; i <= ContainerRupeeMerchant.WARES_END; ++i) {
			int index = this.getCurrentIndex() + i - ContainerRupeeMerchant.WARES_START;
			RupeeTrade trade = (index > -1 && index < trades.size() ? trades.get(index) : null);
			if (trade != null) {
				// Need to re-bind each iteration due to fontRenderer call
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.mc.getTextureManager().bindTexture(TEXTURE);
				this.drawTexturedModalRect(67 + (26 * (i - 1)), 31, 0, 185, 18, 1);
				int price = trade.getPrice();
				if (price > 0) {
					String s = String.valueOf(price);
					if (trade.isDisabled()) {
						s = EnumChatFormatting.DARK_GRAY + s + EnumChatFormatting.RESET;
					}
					int x = 76 + (26 * (i - 1)) - (this.mc.fontRenderer.getStringWidth(s) / 2);
					int y = 34;
					this.mc.fontRenderer.drawStringWithShadow(s, x, y, 0xFFFFFF);
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialRenderTick, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		if (this.selectedIndex > 0) {
			Slot slot = this.inventorySlots.getSlot(this.selectedIndex);
			if (slot instanceof SlotRupeeWares && slot.getHasStack()) {
				int x = this.guiLeft + slot.xDisplayPosition - 1;
				int y = this.guiTop + slot.yDisplayPosition - 1;
				this.drawTexturedModalRect(x, y, 0, 166, 20, 19);
			}
		}
		// func_147046_a is drawEntityOnScreen
		if (this.entityToRender != null) {
			GuiInventory.func_147046_a(this.guiLeft + 32, this.guiTop + 75, 30, this.guiLeft + 32 - this.xSize_lo, this.guiTop + 25 - this.ySize_lo, this.entityToRender);
		} else {
			GuiInventory.func_147046_a(this.guiLeft + 32, this.guiTop + 75, 30, this.guiLeft + 32 - this.xSize_lo, this.guiTop + 25 - this.ySize_lo, this.mc.thePlayer);
		}
	}

	public static class Shop extends GuiRupeeMerchant
	{
		public Shop(EntityPlayer player, IRupeeMerchant merchant) {
			super(new ContainerRupeeMerchant.Shop(player, merchant), player, merchant, true);
		}

		private RupeeTrade getCurrentTrade() {
			int index = this.getCurrentIndex() + this.selectedIndex - ContainerRupeeMerchant.WARES_START;
			RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
			if (trades != null && index > -1 && index < trades.size()) {
				return trades.get(index);
			}
			return null;
		}

		@Override
		protected boolean isTradeEnabled() {
			if (this.selectedIndex < ContainerRupeeMerchant.WARES_START) {
				return false;
			}
			RupeeTrade trade = this.getCurrentTrade();
			return trade != null && !trade.isDisabled() && ZSSPlayerWallet.get(this.mc.thePlayer).getRupees() >= trade.getPrice();
		}

		@Override
		protected void useTrade() {
			RupeeTrade trade = this.getCurrentTrade();
			if (trade != null && this.selectedIndex > 0) {
				this.merchant.useRupeeTrade(trade, this.getItemsToSell);
				PacketDispatcher.sendToServer(new RupeeMerchantTransactionPacket(this.selectedIndex));
			}
		}
	}

	public static class Sales extends GuiRupeeMerchant
	{
		public Sales(EntityPlayer player, IRupeeMerchant merchant) {
			super(new ContainerRupeeMerchant.Sales(player, merchant), player, merchant, false);
		}

		@Override
		protected boolean isTradeEnabled() {
			RupeeTrade trade = ((ContainerRupeeMerchant.Sales) this.inventorySlots).findMatchingTrade(this.selectedIndex);
			return trade != null;
		}

		@Override
		protected void useTrade() {
			RupeeTrade trade = ((ContainerRupeeMerchant.Sales) this.inventorySlots).findMatchingTrade(this.selectedIndex);
			if (trade != null) {
				this.merchant.useRupeeTrade(trade, this.getItemsToSell);
				PacketDispatcher.sendToServer(new RupeeMerchantTransactionPacket(this.selectedIndex));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	static class ButtonNextPrev extends GuiButton
	{
		private final boolean isPrevious;

		/** Texture coordinate offset on the Y axis */
		private final int textureY;

		public ButtonNextPrev(int index, int posX, int posY, boolean isPrevious) {
			this(index, posX, posY, 10, 13, 0, isPrevious);
		}

		public ButtonNextPrev(int index, int posX, int posY, int width, int height, int textureY, boolean isPrevious) {
			super(index, posX, posY, width, height, "");
			this.isPrevious = isPrevious;
			this.textureY = textureY;
			this.enabled = false;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(GuiRupeeMerchant.TEXTURE);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				int u = 176 + (!this.enabled ? this.width * 2 : flag ? this.width : 0);
				int v = this.textureY + (this.isPrevious ? this.height : 0);
				this.drawTexturedModalRect(this.xPosition, this.yPosition, u, v, this.width, this.height);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	static class ButtonToggleMode extends GuiButton
	{
		private final boolean isBuying;

		public ButtonToggleMode(int index, int posX, int posY, boolean isBuying) {
			super(index, posX, posY, 17, 18, "");
			this.isBuying = isBuying;
			this.visible = false;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(GuiRupeeMerchant.TEXTURE);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				int u = (this.isBuying ? 175 : 175 + this.width);
				this.drawTexturedModalRect(this.xPosition, this.yPosition, u, 52, this.width, this.height);
			}
		}
	}
}
