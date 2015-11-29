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

package zeldaswordskills.item;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.CustomEntityList;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Spawn eggs for custom mod entities, using {@link CustomEntityList#addMapping(Class, String, Integer...) CustomEntityList.addMapping}
 * to add the entity ID mapping to the custom egg list. Entities that implement {@link IEntityVariant}
 * should use {@link ItemCustomVariantEgg} instead. 
 *
 */
public class ItemCustomEgg extends Item implements IUnenchantable
{
	/** The egg's spotted and colored overlay icon */
	@SideOnly(Side.CLIENT)
	private IIcon overlay;

	public ItemCustomEgg() {
		super();
		setHasSubtypes(true);
		setTextureName("spawn_egg"); // Item.monsterPlacer.getIconString() is protected >.<
		setCreativeTab(ZSSCreativeTabs.tabEggs);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String s = ("" + StatCollector.translateToLocal("item.zss.spawn_egg.name")).trim();
		String entityName = CustomEntityList.getStringFromID(stack.getItemDamage());
		if (entityName != null) {
			s = s + " " + StatCollector.translateToLocal("entity." + ModInfo.ID + "." + entityName + ".name");
		}
		return s;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		List<Integer> colors = CustomEntityList.entityEggs.get(CustomEntityList.getClassFromID(stack.getItemDamage()));
		return colors != null && colors.size() > 1 ? colors.get((renderPass == 0 ? 0 : 1)) : 16777215;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		} else {
			Block block = world.getBlock(x, y, z);
			x += Facing.offsetsXForSide[side];
			y += Facing.offsetsYForSide[side];
			z += Facing.offsetsZForSide[side];
			double d0 = 0.0D;
			if (side == 1 && block != null && block.getRenderType() == 11) {
				d0 = 0.5D;
			}
			Entity entity = spawnCreature(world, stack.getItemDamage(), x + 0.5D, y + d0, z + 0.5D);
			if (entity != null) {
				if (entity instanceof EntityLiving && stack.hasDisplayName()) {
					((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
				}
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
				}
			}
			return true;
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			MovingObjectPosition mop = getMovingObjectPositionFromPlayer(world, player, true);
			if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
				int i = mop.blockX;
				int j = mop.blockY;
				int k = mop.blockZ;
				if (!world.canMineBlock(player, i, j, k)) {
					return stack;
				}
				if (!player.canPlayerEdit(i, j, k, mop.sideHit, stack)) {
					return stack;
				}
				if (world.getBlock(i, j, k).getMaterial() == Material.water) {
					Entity entity = spawnCreature(world, stack.getItemDamage(), i, j, k);
					if (entity != null) {
						if (entity instanceof EntityLiving && stack.hasDisplayName()) {
							((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
						}
						if (!player.capabilities.isCreativeMode) {
							--stack.stackSize;
						}
					}
				}
			}
		}
		return stack;
	}

	/**
	 * Spawns the creature specified by the egg's type in the location specified by the last three parameters.
	 */
	public Entity spawnCreature(World world, int entityID, double x, double y, double z) {
		Entity entity = null;
		Class<? extends Entity> oclass = CustomEntityList.getClassFromID(entityID);
		if (CustomEntityList.entityEggs.containsKey(oclass)) {
			entity = CustomEntityList.createEntity(oclass, world);
			if (entity instanceof EntityLiving) {
				EntityLiving entityliving = (EntityLiving) entity;
				entity.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(world.rand.nextFloat() * 360.0F), 0.0F);
				entityliving.rotationYawHead = entityliving.rotationYaw;
				entityliving.renderYawOffset = entityliving.rotationYaw;
				entityliving.onSpawnWithEgg((IEntityLivingData) null);
				world.spawnEntityInWorld(entity);
				entityliving.playLivingSound();
			}
		}
		return entity;
	}

	/**
	 * Attempts to spawn a child when the player interacts with an entity using a custom spawn egg
	 * @param stack a stack containing an ItemCustomEgg item
	 * @param player the player interacting with the entity
	 * @param entity the entity that will spawn the child
	 * @return true if a child was spawned and the EntityInteractEvent should be canceled
	 */
	public static boolean spawnChild(World world, ItemStack stack, EntityPlayer player, EntityAgeable entity) {
		Class oclass = CustomEntityList.getClassFromID(stack.getItemDamage());
		if (oclass != null && oclass == entity.getClass()) {
			EntityAgeable child = entity.createChild(entity);
			if (child != null) {
				child.setGrowingAge(-24000);
				child.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, 0.0F, 0.0F);
				if (!world.isRemote) {
					world.spawnEntityInWorld(child);
				}
				if (stack.hasDisplayName()) {
					child.setCustomNameTag(stack.getDisplayName());
				}
				if (!player.capabilities.isCreativeMode) {
					--stack.stackSize;
					if (stack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List itemList) {
		Iterator<Class<? extends Entity>> iterator = CustomEntityList.entityEggs.keySet().iterator();
		while (iterator.hasNext()) {
			Class<? extends Entity> oclass = iterator.next();
			List<Integer> colors = CustomEntityList.entityEggs.get(oclass);
			if (colors != null && colors.size() == 2) {
				itemList.add(new ItemStack(item, 1, CustomEntityList.getEntityId(oclass)));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
		return pass > 0 ? overlay : super.getIconFromDamageForRenderPass(damage, pass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		super.registerIcons(register);
		overlay = register.registerIcon(getIconString() + "_overlay");
	}
}
