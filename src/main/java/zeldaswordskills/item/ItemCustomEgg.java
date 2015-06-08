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

import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.CustomEntityList;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.item.dispenser.BehaviorDispenseCustomMobEgg;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * Spawn eggs for custom mod entities, using {@link CustomEntityList#addMapping(Class, String, Integer...) CustomEntityList.addMapping}
 * to add the entity ID mapping to the custom egg list. Entities that implement {@link IEntityVariant}
 * should use {@link ItemCustomVariantEgg} instead. 
 *
 */
public class ItemCustomEgg extends BaseModItem implements ICustomDispenserBehavior, IUnenchantable
{
	public ItemCustomEgg() {
		super();
		setHasSubtypes(true);
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
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		} else {
			IBlockState state = world.getBlockState(pos);
			pos = pos.offset(side);
			double dy = 0.0D;
			if (side == EnumFacing.UP && state.getBlock() instanceof BlockFence) {
				dy = 0.5D;
			}
			Entity entity = spawnCreature(world, stack.getItemDamage(), pos.getX() + 0.5D, pos.getY() + dy, pos.getZ() + 0.5D);
			if (entity != null) {
				if (entity instanceof EntityLivingBase && stack.hasDisplayName()) {
					entity.setCustomNameTag(stack.getDisplayName());
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
				BlockPos pos = mop.getBlockPos();
				if (!world.isBlockModifiable(player, pos)) {
					return stack;
				}
				if (!player.canPlayerEdit(pos, mop.sideHit, stack)) {
					return stack;
				}
				if (world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
					Entity entity = spawnCreature(world, stack.getItemDamage(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
					if (entity != null) {
						if (entity instanceof EntityLivingBase && stack.hasDisplayName()) {
							entity.setCustomNameTag(stack.getDisplayName());
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
				entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityliving)), null);
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
		if (oclass != null && oclass.isAssignableFrom(entity.getClass())) {
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
	public String[] getVariants() {
		return new String[]{"minecraft:spawn_egg"}; // prevent 'missing location' error
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

	/**
	 * Register same base texture for each egg subtype
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerRenderers(ItemModelMesher mesher) {
		mesher.register(this, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return new ModelResourceLocation("minecraft:spawn_egg", "inventory");
			}
		});
	}

	@Override
	public IBehaviorDispenseItem getNewDispenserBehavior() {
		return new BehaviorDispenseCustomMobEgg();
	}
}
