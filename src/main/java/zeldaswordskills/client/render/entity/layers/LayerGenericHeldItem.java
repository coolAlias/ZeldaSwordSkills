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

package zeldaswordskills.client.render.entity.layers;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.client.model.IModelBiped;

@SideOnly(Side.CLIENT)
public class LayerGenericHeldItem implements LayerRenderer<EntityLivingBase>
{
	private final RendererLivingEntity<?> modelBase;

	/**
	 * Renderer's getMainModel() must return a model implementing IModelBiped
	 */
	public LayerGenericHeldItem(RendererLivingEntity<?> modelBase) {
		this.modelBase = modelBase;
		if (!(this.modelBase.getMainModel() instanceof IModelBiped)) {
			throw new IllegalArgumentException("Model must implement IModelBiped!");
		}
	}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_) {
		ItemStack itemstack = entity.getHeldItem();
		if (itemstack != null) {
			GlStateManager.pushMatrix();
			if (modelBase.getMainModel().isChild) {
				float f7 = 0.5F;
				GlStateManager.translate(0.0F, 0.625F, 0.0F);
				GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
				GlStateManager.scale(f7, f7, f7);
			}
			((IModelBiped) modelBase.getMainModel()).postRenderArm(true, 0.0625F);
			GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);
			if (entity instanceof EntityPlayer && ((EntityPlayer)entity).fishEntity != null) {
				itemstack = new ItemStack(Items.fishing_rod, 0);
			}
			Item item = itemstack.getItem();
			Minecraft minecraft = Minecraft.getMinecraft();
			if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
				GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
				GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
				float f8 = 0.375F;
				GlStateManager.scale(-f8, -f8, f8);
			}
			minecraft.getItemRenderer().renderItem(entity, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
