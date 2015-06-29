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

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.client.render.EntityRendererAlt;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.item.ItemMagicRod;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.SkillActive;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Calls {@link SkillActive#onRenderTick} for currently animating skills and the current
 * ILockOnTarget skill, if necessary to update the player's view.
 * 
 * Updates the current player renderer for transformations (e.g. Giant's Mask).
 * 
 * Also handles vanilla movement key presses, since KeyHandler only fires for custom key bindings.
 *
 */
@SideOnly(Side.CLIENT)
public class TargetingTickHandler implements ITickHandler
{
	private final Minecraft mc;

	/** Allows swapping entity renderer for camera viewpoint when transformed */
	private EntityRenderer renderer, prevRenderer;

	public TargetingTickHandler() {
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (mc.thePlayer != null && ZSSPlayerSkills.get(mc.thePlayer) != null) {
			updateRenderer();
			ZSSPlayerSkills.get(mc.thePlayer).onRenderTick((Float) tickData[0]);
			// Hack for magic rods, since the item's update tick isn't called frequently enough
			if (mc.thePlayer.getItemInUse() != null && mc.thePlayer.getItemInUse().getItem() instanceof ItemMagicRod) {
				mc.thePlayer.swingProgress = 0.5F;
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {}

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
