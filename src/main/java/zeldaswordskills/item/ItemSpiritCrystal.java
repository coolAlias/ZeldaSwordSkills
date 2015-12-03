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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFireIndirect;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.PacketISpawnParticles;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WarpPoint;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * The three Spirit Crystals, i.e. spells, from Ocarina of Time
 * 
 * Each use consumes part of the spirit; when used up entirely, only the base crystal remains.
 * 
 * Din's Fire: AoE centered on player engulfs all nearby targets in flames
 * Farore's Wind: Sneak + right-click to mark a location, use to teleport to a marked location in same dimension
 * Nayru's Love: Become impervious to damage until hunger reaches zero or certain amount of time passes;
 * 		constantly drains hunger and prevents the use of other magic skills
 *
 */
public class ItemSpiritCrystal extends Item implements ISacredFlame, ISpawnParticles
{
	/** The spirit's id, from BlockSacredFlame */
	private final int spiritType;

	/** Cost (in damage) for each use of this item*/
	private final int costToUse;

	/** Amount of time required before the crystal's effects activate */
	private final int timeToUse;

	public ItemSpiritCrystal(int type, int cost, int time) {
		super();
		spiritType = type;
		costToUse = cost;
		timeToUse = time;
		setMaxDamage(128);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return timeToUse;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.block;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		int cost = 0;
		if (!ZSSPlayerInfo.get(player).isNayruActive()) {
			if (spiritType == BlockSacredFlame.FARORE && player.isSneaking()) {
				mark(stack, world, player);
				cost = 1;
			} else if (spiritType == BlockSacredFlame.NAYRU) {
				cost = handleNayru(stack, world, player);
			} else {
				player.setItemInUse(stack, getMaxItemUseDuration(stack));
				String sound = (spiritType == BlockSacredFlame.DIN ? Sounds.SUCCESS_MAGIC : Sounds.FLAME_ABSORB);
				player.playSound(sound, 1.0F, 1.0F);
			}

			if (damageStack(stack, player, cost)) {
				stack = new ItemStack(ZSSItems.crystalSpirit);
			}
		} else {
			player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
		}
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		if (count == 1) {
			int cost = 0;
			switch(spiritType) {
			case BlockSacredFlame.DIN: cost = handleDin(stack, player.worldObj, player); break;
			case BlockSacredFlame.FARORE: cost = handleFarore(stack, player.worldObj, player); break;
			case BlockSacredFlame.NAYRU: break;
			default: ZSSMain.logger.warn("Invalid spirit type " + spiritType + " while using spirit crystal");
			}
			if (damageStack(stack, player, cost)) {
				player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalSpirit));
			}
		}
	}

	@Override
	public boolean onActivatedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive) {
		return false;
	}

	@Override
	public boolean onClickedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive) {
		if (world.isRemote) {
			return false;
		} else if (stack.getItemDamage() == 0) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.spirit_crystal.sacred_flame.full");
		} else if (isActive) {
			if (spiritType == type) {
				int originalDamage = stack.getItemDamage();
				stack.setItemDamage(0);
				world.playSoundAtEntity(player, Sounds.SUCCESS_MAGIC, 1.0F, 1.0F);
				return (world.rand.nextInt(stack.getMaxDamage()) < originalDamage);
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.spirit_crystal.sacred_flame.mismatch");
			}
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.inactive");
		}
		return false;
	}

	/**
	 * Damages the stack for amount, returning true if the stack size is zero
	 */
	private boolean damageStack(ItemStack stack, EntityPlayer player, int amount) {
		if (amount > 0) {
			stack.damageItem(amount, player);
			return stack.stackSize == 0;
		}
		return false;
	}

	/**
	 * Returns true if there is enough charge remaining to use the item
	 */
	private boolean canUse(ItemStack stack) {
		return (stack.getMaxDamage() - stack.getItemDamage() >= costToUse);
	}

	/**
	 * Processes right-click for Din's Fire; returns amount of damage to apply to stack
	 */
	private int handleDin(ItemStack stack, World world, EntityPlayer player) {
		float radius = 5.0F;
		if (!world.isRemote) {
			PacketDispatcher.sendToAllAround(new PacketISpawnParticles(player, radius), player, 64.0D);
			affectDinBlocks(world, player, radius);
		}
		affectDinEntities(world, player, radius);
		world.playSoundAtEntity(player, Sounds.EXPLOSION, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
		return costToUse;
	}

	/**
	 * Affects all blocks in the radius with the effects of Din's Fire
	 */
	private void affectDinBlocks(World world, EntityPlayer player, float radius) {
		List<ChunkPosition> affectedBlockPositions = new ArrayList(WorldUtils.getAffectedBlocksList(world, world.rand, radius, player.posX, player.posY, player.posZ, null));
		int i, j, k;
		Block block;
		ChunkPosition chunkposition;
		Iterator<ChunkPosition> iterator = affectedBlockPositions.iterator();
		while (iterator.hasNext()) {
			chunkposition = iterator.next();
			i = chunkposition.chunkPosX;
			j = chunkposition.chunkPosY;
			k = chunkposition.chunkPosZ;
			block = world.getBlock(i, j, k);

			if (block.getMaterial() == Material.air && Config.isDinIgniteEnabled()) {
				Block block1 = world.getBlock(i, j - 1, k);
				if (block1.func_149730_j() && world.rand.nextInt(8) == 0) {
					world.setBlock(i, j, k, Blocks.fire);
				}
			} else if (WorldUtils.canMeltBlock(world, block, i, j, k)) {
				world.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				world.setBlockToAir(i, j, k);
			}
		}
	}

	/**
	 * Affects all entities within the radius with the effects of Din's Fire
	 */
	private void affectDinEntities(World world, EntityPlayer player, float radius) {
		int i = MathHelper.floor_double(player.posX - (double) radius - 1.0D);
		int j = MathHelper.floor_double(player.posX + (double) radius + 1.0D);
		int k = MathHelper.floor_double(player.posY - (double) radius - 1.0D);
		int l1 = MathHelper.floor_double(player.posY + (double) radius + 1.0D);
		int i2 = MathHelper.floor_double(player.posZ - (double) radius - 1.0D);
		int j2 = MathHelper.floor_double(player.posZ + (double) radius + 1.0D);
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox((double) i, (double) k, (double) i2, (double) j, (double) l1, (double) j2));
		Vec3 vec3 = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);

		for (int k2 = 0; k2 < list.size(); ++k2) {
			Entity entity = list.get(k2);
			double d0 = entity.posX - player.posX;
			double d1 = entity.posY + (double) entity.getEyeHeight() - player.posY;
			double d2 = entity.posZ - player.posZ;
			double d8 = (double) MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);

			if (d8 != 0.0D)
			{
				d0 /= d8;
				d1 /= d8;
				d2 /= d8;
				double d10 = (double) world.getBlockDensity(vec3, entity.boundingBox);
				float amount = 32.0F * (float) d10;
				if (entity.isImmuneToFire()) {
					amount *= 0.25F;
				}

				if (entity.attackEntityFrom(new DamageSourceFireIndirect("magic.din", player, player, true).setMagicDamage(), amount) && !entity.isImmuneToFire()) {
					if (world.rand.nextFloat() < d10) {
						entity.setFire(10);
					}
				}

				double d11 = EnchantmentProtection.func_92092_a(entity, d10);
				entity.motionX += d0 * d11;
				entity.motionY += d1 * d11;
				entity.motionZ += d2 * d11;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void spawnParticles(World world, EntityPlayer player, ItemStack stack, double posX, double posY, double posZ, float radius) {
		int i1 = MathHelper.floor_double(posX + radius);
		int j1 = MathHelper.floor_double(posY + radius);
		int k1 = MathHelper.floor_double(posZ + radius);
		for (int i = MathHelper.floor_double(posX - radius); i < i1; ++i) {
			for (int j = MathHelper.floor_double(posY - radius + 1.0D); j < j1; ++j) {
				for (int k = MathHelper.floor_double(posZ - radius); k < k1; ++k) {
					double d0 = (double)((float) i + world.rand.nextFloat());
					double d1 = (double)((float) j + world.rand.nextFloat());
					double d2 = (double)((float) k + world.rand.nextFloat());
					double d3 = d0 - posX;
					double d4 = d1 - posY;
					double d5 = d2 - posZ;
					double d6 = (double) MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
					d3 /= d6;
					d4 /= d6;
					d5 /= d6;
					double d7 = 0.5D / (d6 / (double) radius + 0.1D);
					d7 *= (double)(world.rand.nextFloat() * world.rand.nextFloat() + 0.3F);
					d3 *= d7;
					d4 *= d7;
					d5 *= d7;
					world.spawnParticle("flame", (d0 + posX * 1.0D) / 2.0D, (d1 + posY * 1.0D) / 2.0D, (d2 + posZ * 1.0D) / 2.0D, d3, d4, d5);
					world.spawnParticle("smoke", d0, d1, d2, d3, d4, d5);
				}
			}
		}
	}

	/**
	 * Processes right-click for Farore's Wind; returns amount of damage to apply to stack
	 */
	private int handleFarore(ItemStack stack, World world, EntityPlayer player) {
		if (canUse(stack)) {
			WarpPoint warp = recall(stack);
			if (warp != null) {
				if (warp.dimensionId == player.worldObj.provider.dimensionId) {
					player.setPositionAndUpdate(warp.x + 0.5D, warp.y + 0.5D, warp.z + 0.5D);
					player.playSound(Sounds.SUCCESS_MAGIC, 1.0F, 1.0F);
					return costToUse;
				} else {
					player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
					if (world.isRemote) {
						PlayerUtils.sendFormattedChat(player, "chat.zss.spirit_crystal.farore.fail.dimension",
								StatCollector.translateToLocal(getUnlocalizedName() + ".name"));
					}
				}
			} else {
				player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
				if (world.isRemote) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.spirit_crystal.farore.fail.mark");
				}
			}
		}
		return 0;
	}

	/**
	 * Processes right-click for Nayru's Love; returns amount of damage to apply to stack
	 */
	private int handleNayru(ItemStack stack, World world, EntityPlayer player) {
		if (canUse(stack)) {
			ZSSPlayerInfo.get(player).activateNayru();
			world.playSoundAtEntity(player, Sounds.SUCCESS_MAGIC, 1.0F, 1.0F);
			return costToUse;
		}
		return 0;
	}

	/**
	 * Saves the player's current position and dimension for Farore's Wind
	 */
	private void mark(ItemStack stack, World world, EntityPlayer player) {
		if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
		WarpPoint warp = new WarpPoint(world.provider.dimensionId, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		stack.getTagCompound().setTag("zssRecallPoint", warp.writeToNBT());
		world.playSoundAtEntity(player, Sounds.SUCCESS_MAGIC, 1.0F, 1.0F);
	}

	/**
	 * Returns the saved warp coordinates for Farore's Wind
	 */
	private WarpPoint recall(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("zssRecallPoint", Constants.NBT.TAG_COMPOUND)) {
			return WarpPoint.readFromNBT(stack.getTagCompound().getCompoundTag("zssRecallPoint"));
		}
		// for backwards compatibility:
		else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("zssFWdimension")) {
			int dimension = stack.getTagCompound().getInteger("zssFWdimension");
			double x = stack.getTagCompound().getDouble("zssFWposX");
			double y = stack.getTagCompound().getDouble("zssFWposY");
			double z = stack.getTagCompound().getDouble("zssFWposZ");
			return new WarpPoint(dimension, MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack, int pass) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.1"));
	}
}
