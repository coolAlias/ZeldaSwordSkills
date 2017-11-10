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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StatCollector;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.ref.Config;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;

public class GuiEndingBlowOverlay extends AbstractGuiOverlay
{
	/** Time at which the current combo first started displaying */
	protected long displayStartTime;

	/** Length of time combo pop-up will display */
	private static final long DISPLAY_TIME = 5000;

	public GuiEndingBlowOverlay(Minecraft mc) {
		super(mc);
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return Config.endingBlowHudHAlign;
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return Config.endingBlowHudVAlign;
	}

	@Override
	public boolean shouldRender() {
		if (!Config.isEndingBlowHudEnabled) {
			return false;
		}
		ZSSPlayerSkills skills = ZSSPlayerSkills.get(mc.thePlayer);
		// Call #canUse instead of #canExecute to determine whether notification should be displayed
		if (skills.getActiveSkill(SkillBase.endingBlow) != null && skills.getActiveSkill(SkillBase.endingBlow).canUse(this.mc.thePlayer)) {
			ICombo skill = skills.getComboSkill();
			ILockOnTarget target = skills.getTargetingSkill();
			if (skill != null && skill.isComboInProgress() && target != null && target.getCurrentTarget() == skill.getCombo().getLastEntityHit()) {
				this.displayStartTime = Minecraft.getSystemTime();
			}
		}
		return ((Minecraft.getSystemTime() - this.displayStartTime) < DISPLAY_TIME);
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		this.height = this.mc.fontRendererObj.FONT_HEIGHT;
		this.width = this.mc.fontRendererObj.getStringWidth(StatCollector.translateToLocal("combo.ending"));
		this.setPosX(resolution, Config.endingBlowHudOffsetX);
		this.setPosY(resolution, Config.endingBlowHudOffsetY);
	}

	@Override
	protected void render(ScaledResolution resolution) {
		this.mc.fontRendererObj.drawString(StatCollector.translateToLocal("combo.ending"), this.x, this.y, 0xFF0000, true);
	}
}
