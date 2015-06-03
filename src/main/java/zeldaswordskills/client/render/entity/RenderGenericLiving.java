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

package zeldaswordskills.client.render.entity;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.client.model.IModelBiped;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Renderer for generic {@link EntityLivingBase} entities with a single texture and set scale.
 * 
 * Animations are handled via the model methods:
 * {@link ModelBase#setRotationAngles(float, float, float, float, float, float, Entity) setRotationAngles}
 * {@link ModelBase#setLivingAnimations(EntityLivingBase, float, float, float) setLivingAnimations}.
 * 
 * Child versions of the entity are rendered at half scale.
 *
 */
@SideOnly(Side.CLIENT)
public class RenderGenericLiving extends RenderLiving
{
	private final ResourceLocation texture;
	private final float scale;

	/**
	 * @param model			Any animations need to be handled in the model class directly
	 * @param scale			Scale of the full size model; child versions will render at half this scale
	 * @param texturePath	Be sure to prefix with the Mod ID if needed, otherwise it will use the Minecraft texture path
	 */
	public RenderGenericLiving(ModelBase model, float shadowSize, float scale, String texturePath) {
		super(model, shadowSize);
		this.texture = new ResourceLocation(texturePath);
		this.scale = scale;
	}

	@Override
	public void doRender(EntityLiving entity, double dx, double dy, double dz, float yaw, float partialTick) {
		if (entity instanceof IBossDisplayData) {
			BossStatus.setBossStatus((IBossDisplayData) entity, true);
		}
		super.doRender(entity, dx, dy, dz, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entity, float partialTick) {
		float f = scale;
		if (entity.isChild()) {
			f = (float)((double) f * 0.5D);
		}
		GL11.glScalef(f, f, f);
	}

	@Override
	protected void renderEquippedItems(EntityLivingBase entity, float partialTick) {
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		ItemStack itemstack = entity.getHeldItem();
		ItemStack helm = entity.getEquipmentInSlot(ArmorIndex.EQUIPPED_HELM);
		Item item;
		float f1;

		if (mainModel instanceof ModelBiped) {
			((ModelBiped) mainModel).heldItemRight = (itemstack == null ? 0 : 1);
		} else if (mainModel instanceof IModelBiped) {
			((IModelBiped) mainModel).setHeldItemValue(true, (itemstack == null ? 0 : 1));
		}
		if (helm != null) {
			GL11.glPushMatrix();
			if (mainModel instanceof ModelBiped) {
				((ModelBiped) mainModel).bipedHead.postRender(0.0625F);
			} else if (mainModel instanceof IModelBiped) {
				((IModelBiped) mainModel).postRenderHead(0.0625F);
			}

			item = helm.getItem();
			net.minecraftforge.client.IItemRenderer customRenderer = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(helm, net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
			boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED, helm, net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D));

			if (item instanceof ItemBlock) {
				if (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).getRenderType())) {
					f1 = 0.625F;
					GL11.glTranslatef(0.0F, -0.25F, 0.0F);
					GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
					GL11.glScalef(f1, -f1, -f1);
				}

				renderManager.itemRenderer.renderItem(entity, helm, 0);
			} else if (item == Items.skull) {
				f1 = 1.0625F;
				GL11.glScalef(f1, -f1, -f1);
				GameProfile gameprofile = null;
				if (helm.hasTagCompound()) {
					NBTTagCompound nbttagcompound = helm.getTagCompound();
					if (nbttagcompound.hasKey("SkullOwner", 10)) {
						gameprofile = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("SkullOwner"));
					} else if (nbttagcompound.hasKey("SkullOwner", 8) && !StringUtils.isNullOrEmpty(nbttagcompound.getString("SkullOwner"))) {
						gameprofile = new GameProfile((UUID)null, nbttagcompound.getString("SkullOwner"));
					}
				}

				TileEntitySkullRenderer.field_147536_b.func_152674_a(-0.5F, 0.0F, -0.5F, 1, 180.0F, helm.getItemDamage(), gameprofile);
			}

			GL11.glPopMatrix();
		}

		if (itemstack != null && itemstack.getItem() != null) {
			item = itemstack.getItem();
			GL11.glPushMatrix();

			if (mainModel.isChild) {
				f1 = 0.5F;
				GL11.glTranslatef(0.0F, 0.625F, 0.0F);
				GL11.glRotatef(-20.0F, -1.0F, 0.0F, 0.0F);
				GL11.glScalef(f1, f1, f1);
			}

			if (mainModel instanceof ModelBiped) {
				((ModelBiped) mainModel).bipedRightArm.postRender(0.0625F);
			} else if (mainModel instanceof IModelBiped) {
				((IModelBiped) mainModel).postRenderArm(true, 0.0625F);
			}
			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

			net.minecraftforge.client.IItemRenderer customRenderer = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(itemstack, net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
			boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED, itemstack, net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D));

			if (item instanceof ItemBlock && (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).getRenderType()))) {
				f1 = 0.5F;
				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
				f1 *= 0.75F;
				GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(-f1, -f1, f1);
			} else if (item == Items.bow) {
				f1 = 0.625F;
				GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
				GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(f1, -f1, f1);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else if (item.isFull3D()) {
				f1 = 0.625F;
				if (item.shouldRotateAroundWhenRendering()) {
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
				}
				GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
				GL11.glScalef(f1, -f1, f1);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else {
				f1 = 0.375F;
				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
				GL11.glScalef(f1, f1, f1);
				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
			}

			float f2;
			int i;
			float f5;

			if (itemstack.getItem().requiresMultipleRenderPasses()) {
				for (i = 0; i < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); ++i) {
					int j = itemstack.getItem().getColorFromItemStack(itemstack, i);
					f5 = (float)(j >> 16 & 255) / 255.0F;
					f2 = (float)(j >> 8 & 255) / 255.0F;
					float f3 = (float)(j & 255) / 255.0F;
					GL11.glColor4f(f5, f2, f3, 1.0F);
					renderManager.itemRenderer.renderItem(entity, itemstack, i);
				}
			} else {
				i = itemstack.getItem().getColorFromItemStack(itemstack, 0);
				float f4 = (float)(i >> 16 & 255) / 255.0F;
				f5 = (float)(i >> 8 & 255) / 255.0F;
				f2 = (float)(i & 255) / 255.0F;
				GL11.glColor4f(f4, f5, f2, 1.0F);
				renderManager.itemRenderer.renderItem(entity, itemstack, 0);
			}

			GL11.glPopMatrix();
		}
	}
}
