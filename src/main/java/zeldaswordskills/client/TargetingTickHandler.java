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

package zeldaswordskills.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.client.render.EntityRendererAlt;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.SkillActive;

/**
 * 
 * Calls {@link SkillActive#onRenderTick} for currently animating skills and the current
 * ILockOnTarget skill, if necessary to update the player's view.
 * 
 * Updates the current player renderer for transformations (e.g. Giant's Mask).
 *
 */
@SideOnly(Side.CLIENT)
public class TargetingTickHandler
{
	private final Minecraft mc;

	/** Allows swapping entity renderer for camera viewpoint when transformed */
	private EntityRenderer renderer, prevRenderer;

	public TargetingTickHandler() {
		this.mc = Minecraft.getMinecraft();
	}

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.START) {
			if (mc.thePlayer != null && ZSSPlayerSkills.get(mc.thePlayer) != null) {
				// TODO updateRenderer();
				ZSSPlayerSkills.get(mc.thePlayer).onRenderTick(event.renderTickTime);
				float swing = ZSSPlayerInfo.get(mc.thePlayer).armSwing;
				if (swing > 0.0F) {
					mc.thePlayer.swingProgress = swing;
					mc.thePlayer.prevSwingProgress = swing;
				}
			}
		}
	}

	/**
	 * Updates the camera entity renderer for Giant's Mask or other transformations
	 */
	private void updateRenderer() {
		ItemStack mask = mc.thePlayer.getCurrentArmor(ArmorIndex.WORN_HELM);
		if (mask != null && mask.getItem() == ZSSItems.maskGiants) {
			if (renderer == null) {
				renderer = new EntityRendererAlt(mc);
			}
			if (mc.entityRenderer != renderer) {
				prevRenderer = mc.entityRenderer;
				mc.entityRenderer = renderer;
			}
		} else if (prevRenderer != null && mc.entityRenderer != prevRenderer) {
			mc.entityRenderer = prevRenderer;
		}
	}
}
