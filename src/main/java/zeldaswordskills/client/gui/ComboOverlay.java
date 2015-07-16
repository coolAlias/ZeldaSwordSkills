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

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.EndComboPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.skills.Combo;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;

/**
 * 
 * Displays current Combo information in upper-left corner 
 * 
 */
@SideOnly(Side.CLIENT)
public class ComboOverlay extends Gui implements IGuiOverlay
{
	private final Minecraft mc;

	/** Texture location for the targeting overlay */
	//private static final ResourceLocation targetTexture = new ResourceLocation(ModInfo.ID, "textures/gui/targeting_overlay.png");

	/** Height and width of the targeting overlay texture */
	//private static final int HEIGHT = 15, WIDTH = 15;

	/** Rotation timer for targeting display */
	//private int rotation = 0;

	/** Combo to display will update as combo updates, should fade after some time */
	private Combo combo = null;

	/** Used to detect changes in the combo size */
	private int lastComboSize = 0;

	/** Time at which the current combo first started displaying */
	private long displayStartTime;

	/** Length of time combo pop-up will display */
	private static final long DISPLAY_TIME = 5000;

	/** Whether combo overlay should display */
	public static boolean shouldDisplay;

	public ComboOverlay(Minecraft mc) {
		super();
		this.mc = mc;
		shouldDisplay = Config.isComboHudEnabled();
	}

	@Override
	public boolean shouldRender() {
		return true;
	}

	@Override
	public void renderOverlay(ScaledResolution resolution) {
		ZSSPlayerSkills skills = ZSSPlayerSkills.get(mc.thePlayer);
		if (skills != null) {
			displayComboText(skills, resolution);
			ILockOnTarget skill = skills.getTargetingSkill();
			if (skill != null && skill.isLockedOn()) {
				//displayTargetingOverlay(event, skill.getCurrentTarget());
			}
		}
	}

	/**
	 * Displays current combo data if applicable
	 */
	private void displayComboText(ZSSPlayerSkills skills, ScaledResolution resolution) {
		ICombo iCombo = skills.getComboSkill();
		if (iCombo != null && iCombo.getCombo() != null) {
			if (combo != iCombo.getCombo()) {
				combo = iCombo.getCombo();
				lastComboSize = combo.getNumHits();
				displayStartTime = Minecraft.getSystemTime();
				if (iCombo.getCombo().isFinished()) {
					iCombo.setCombo(null);
					PacketDispatcher.sendToServer(new EndComboPacket((SkillBase) iCombo));
				}
			}
		}

		if (combo != null && combo.getNumHits() > 0) {
			// combo has changed, reset time
			if (lastComboSize != combo.getNumHits()) {
				lastComboSize = combo.getNumHits();
				displayStartTime = Minecraft.getSystemTime();
			}
			// TODO make display look nice
			if ((Minecraft.getSystemTime() - displayStartTime) < DISPLAY_TIME) {
				if (shouldDisplay) {
					String s = (combo.isFinished() ? (StatCollector.translateToLocal("combo.finished") + "! ") : (StatCollector.translateToLocal("combo.combo") + ": "));
					mc.fontRendererObj.drawString(s + combo.getLabel(), 10, 10, combo.isFinished() ? 0x9400D3 : 0xEEEE00, true);
					mc.fontRendererObj.drawString(StatCollector.translateToLocal("combo.size") + ": " + combo.getNumHits() + "/" + combo.getMaxNumHits(), 10, 20, 0xFFFFFF, true);
					mc.fontRendererObj.drawString(StatCollector.translateToLocal("combo.damage") + ": " + String.format("%.1f",combo.getDamage()), 10, 30, 0xFFFFFF, true);
					List<Float> damageList = combo.getDamageList();
					for (int i = 0; i < damageList.size() && i < Config.getHitsToDisplay(); ++i) {
						mc.fontRendererObj.drawString(" +" + String.format("%.1f",damageList.get(damageList.size() - i - 1)), 10, 40 + 10 * i, 0xFFFFFF, true);
					}
				}
				// for Ending Blow, use canUse instead of canExecute to determine whether notification should be displayed
				if (skills.getActiveSkill(SkillBase.endingBlow) != null && skills.getActiveSkill(SkillBase.endingBlow).canUse(mc.thePlayer)) {
					ICombo skill = skills.getComboSkill();
					ILockOnTarget target = skills.getTargetingSkill();
					if (skill != null && skill.isComboInProgress() && target != null && target.getCurrentTarget() == skill.getCombo().getLastEntityHit()) {
						mc.fontRendererObj.drawString(StatCollector.translateToLocal("combo.ending"), (resolution.getScaledWidth() / 2) - 15, 30, 0xFF0000, true);
					}
				}
			}
		}
	}

	/**
	 * Displays the targeting overlay with an appropriate scale for the distance given
	 */
	/*
	private void displayTargetingOverlay(RenderGameOverlayEvent event, Entity target) {
		//float distance = mc.thePlayer.getDistanceToEntity(target);
		//float scale = MathHelper.clamp_float(20.0F / (distance > 0.1F ? distance : 0.1F), 1.0F, 8.0F);
		int x = event.resolution.getScaledWidth() / 2 - 6;
		int y = event.resolution.getScaledHeight() / 2 - 6;
		//Vec3 vec3 = mc.thePlayer.getLookVec();
		rotation += 15;
		rotation %= 360;
		zLevel = -90.0F;
		mc.getTextureManager().bindTexture(targetTexture);
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		//GL11.glRotatef((float) rotation, 0.0F, 1.0F, 0.0F);
		//GL11.glScalef(scale, scale, scale);
		//GL11.glTranslated(target.posX, target.posY, target.posZ);
		drawTexturedModalRect(x, y, 0, 0, HEIGHT, WIDTH);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glPopMatrix();
	}
	 */

	public static void drawTexturedQuadFit(double x, double y, double width, double height, double zLevel){
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		renderer.startDrawingQuads();
		renderer.addVertexWithUV(x + 0, y + height, zLevel, 0,1);
		renderer.addVertexWithUV(x + width, y + height, zLevel, 1, 1);
		renderer.addVertexWithUV(x + width, y + 0, zLevel, 1,0);
		renderer.addVertexWithUV(x + 0, y + 0, zLevel, 0, 0);
		// renderer.finishDrawing(); // TODO can just call this instead of tessellator.draw() ???
		tessellator.draw();
	}
}
