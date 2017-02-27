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

package zeldaswordskills.client.gui;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StatCollector;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.EndComboPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.skills.Combo;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillBase;

/**
 * 
 * Displays current Combo information in upper-left corner 
 * 
 */
@SideOnly(Side.CLIENT)
public class ComboOverlay extends AbstractGuiOverlay
{
	/** Combo to display will update as combo updates, should fade after some time */
	private Combo combo = null;

	/** Current combo descriptors */
	private String label, comboSize, comboDamage;

	/** Used to detect changes in the combo size */
	private int lastComboSize = 0;

	/** Time at which the current combo first started displaying */
	private long displayStartTime;

	/** Length of time combo pop-up will display */
	private static final long DISPLAY_TIME = 5000;

	public ComboOverlay(Minecraft mc) {
		super(mc);
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return Config.comboHudHAlign;
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return Config.comboHudVAlign;
	}

	@Override
	public boolean allowMergeX(boolean rendered) {
		return false; // should always render on its own line
	}

	@Override
	public boolean shouldRender() {
		if (!Config.isComboHudEnabled) {
			return false;
		}
		ICombo iCombo = ZSSPlayerSkills.get(mc.thePlayer).getComboSkill();
		if (iCombo != null && iCombo.getCombo() != null) {
			if (this.combo != iCombo.getCombo()) {
				this.combo = iCombo.getCombo();
				this.lastComboSize = this.combo.getNumHits();
				this.displayStartTime = Minecraft.getSystemTime();
				if (this.combo.isFinished()) {
					iCombo.setCombo(null);
					PacketDispatcher.sendToServer(new EndComboPacket((SkillBase) iCombo));
				}
			}
		}
		if (this.combo != null && this.combo.getNumHits() > 0) {
			// If combo has changed, reset timer
			if (this.lastComboSize != this.combo.getNumHits()) {
				this.lastComboSize = this.combo.getNumHits();
				this.displayStartTime = Minecraft.getSystemTime();
			}
			return ((Minecraft.getSystemTime() - this.displayStartTime) < DISPLAY_TIME);
		}
		return false;
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		// Minimum display of 3 lines: combo name, size and damage
		int n = Math.min(this.combo.getDamageList().size(), Config.getHitsToDisplay());
		this.height = (n + 3) * this.mc.fontRenderer.FONT_HEIGHT;
		this.label = (this.combo.isFinished() ? StatCollector.translateToLocalFormatted("combo.finished", this.combo.getLabel()) : StatCollector.translateToLocalFormatted("combo.combo", this.combo.getLabel()));
		this.comboSize = StatCollector.translateToLocalFormatted("combo.size", this.combo.getNumHits(), this.combo.getMaxNumHits());
		this.comboDamage = StatCollector.translateToLocalFormatted("combo.damage", String.format("%.1f", this.combo.getDamage()));
		this.width = Math.max(this.mc.fontRenderer.getStringWidth(this.label), this.mc.fontRenderer.getStringWidth(this.comboDamage));
		this.setPosX(resolution, this.getOffsetX(DEFAULT_PADDING) + Config.comboHudOffsetX);
		this.setPosY(resolution, this.getOffsetY(DEFAULT_PADDING) + Config.comboHudOffsetY);
	}

	@Override
	protected void render(ScaledResolution resolution) {
		int xPos = this.getLeft();
		int yPos = this.getTop();
		this.mc.fontRenderer.drawString(this.label, xPos, yPos, this.combo.isFinished() ? 0x9400D3 : 0xEEEE00, true);
		yPos += this.mc.fontRenderer.FONT_HEIGHT;
		this.mc.fontRenderer.drawString(this.comboSize, xPos + this.getOffset(this.comboSize), yPos, 0xFFFFFF, true);
		yPos += this.mc.fontRenderer.FONT_HEIGHT;
		this.mc.fontRenderer.drawString(this.comboDamage, xPos + this.getOffset(this.comboDamage), yPos, 0xFFFFFF, true);
		List<Float> damageList = this.combo.getDamageList();
		int n = Math.min(damageList.size(), Config.getHitsToDisplay());
		for (int i = 0; i < n; ++i) {
			yPos += this.mc.fontRenderer.FONT_HEIGHT;
			String s = String.format("+%.1f", damageList.get(damageList.size() - i - 1));
			this.mc.fontRenderer.drawString(s, xPos + this.getOffset(s), yPos, 0xFFFFFF, true);
		}
	}

	private int getOffset(String s) {
		switch (this.getHorizontalAlignment()) {
		case LEFT: return DEFAULT_PADDING;
		case CENTER: return (this.getWidth() / 2) - (this.mc.fontRenderer.getStringWidth(s) / 2);
		case RIGHT: return this.getWidth() - this.mc.fontRenderer.getStringWidth(s) - DEFAULT_PADDING;
		default: return 0;
		}
	}
}
