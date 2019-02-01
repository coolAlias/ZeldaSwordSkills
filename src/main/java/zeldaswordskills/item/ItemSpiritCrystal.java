/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFireIndirect;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.PacketISpawnParticles;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WarpPoint;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * The three Spirit Crystals, i.e. spells, from Ocarina of Time
 * 
 * Each use consumes part of the spirit; when used up entirely, only the base crystal remains.
 * 
 * Din's Fire: AoE centered on player engulfs all nearby targets in flames
 * Farore's Wind: Sneak + right-click to mark a location, use to teleport to a marked location in same dimension
 * Nayru's Love: Become impervious to damage until magic is depleted or certain amount of time passes;
 * 		constantly drains magic points and prevents the use of other magic skills
 *
 */
public class ItemSpiritCrystal extends BaseModItem implements ISacredFlame, ISpawnParticles
{
	/** The spirit's id, from BlockSacredFlame */
	private final BlockSacredFlame.EnumType spiritType;

	/** Cost (in damage) for each use of this item*/
	private final int costToUse;

	/** Amount of time required before the crystal's effects activate */
	private final int timeToUse;

	public ItemSpiritCrystal(BlockSacredFlame.EnumType flame, int cost, int time) {
		super();
		spiritType = flame;
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
		return EnumAction.BLOCK;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!ZSSPlayerInfo.get(player).canUseMagic()) {
			player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
			return stack;
		}
		int cost = 0;
		if (spiritType == BlockSacredFlame.EnumType.FARORE && player.isSneaking()) {
			if (ZSSPlayerInfo.get(player).useMagic(2.0F)) { // magic_cost / 5
				mark(stack, world, player);
				cost = 1;
			} else {
				player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
			}
		} else if (spiritType == BlockSacredFlame.EnumType.NAYRU) {
			cost = handleNayru(stack, world, player);
		} else {
			player.setItemInUse(stack, getMaxItemUseDuration(stack));
			String sound = (spiritType == BlockSacredFlame.EnumType.DIN ? Sounds.SUCCESS_MAGIC : Sounds.FLAME_ABSORB);
			player.playSound(sound, 1.0F, 1.0F);
		}
		if (damageStack(stack, player, cost)) {
			stack = new ItemStack(ZSSItems.crystalSpirit);
		}
		return stack;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		int cost = 0;
		switch(spiritType) {
		case DIN: cost = handleDin(stack, world, player); break;
		case FARORE: cost = handleFarore(stack, world, player); break;
		case NAYRU: break;
		default: ZSSMain.logger.warn("Invalid spirit type " + spiritType + " while using spirit crystal");
		}
		if (damageStack(stack, player, cost)) {
			return new ItemStack(ZSSItems.crystalSpirit);
		}
		return stack;
	}

	@Override
	public boolean onActivatedSacredFlame(ItemStack stack, World world, EntityPlayer player, BlockSacredFlame.EnumType flame, boolean isActive) {
		return false;
	}

	@Override
	public boolean onClickedSacredFlame(ItemStack stack, World world, EntityPlayer player, BlockSacredFlame.EnumType flame, boolean isActive) {
		if (world.isRemote) {
			return false;
		} else if (stack.getItemDamage() == 0) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.spirit_crystal.sacred_flame.full");
		} else if (isActive) {
			if (spiritType == flame) {
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
		if (!ZSSPlayerInfo.get(player).useMagic(40.0F)) {
			player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
			return 0;
		}
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
		List<BlockPos> affectedBlockPositions = new ArrayList<BlockPos>(WorldUtils.getAffectedBlocksList(world, world.rand, radius, player.posX, player.posY, player.posZ, null));
		Block block;
		BlockPos pos;
		Iterator<BlockPos> iterator = affectedBlockPositions.iterator();
		while (iterator.hasNext()) {
			pos = iterator.next();
			block = world.getBlockState(pos).getBlock();
			if (block.getMaterial() == Material.air && Config.isDinIgniteEnabled()) {
				Block block1 = world.getBlockState(pos.down()).getBlock();
				if (block1.isFullBlock() && world.rand.nextInt(8) == 0) {
					world.setBlockState(pos, Blocks.fire.getDefaultState());
				}
			} else if (WorldUtils.canMeltBlock(world, block, pos)) {
				world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
						Sounds.FIRE_FIZZ, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				world.setBlockToAir(pos);
			}
		}
	}

	/**
	 * Affects all entities within the radius with the effects of Din's Fire
	 */
	private void affectDinEntities(World world, EntityPlayer player, float radius) {
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(radius, radius / 2F, radius));
		Vec3 vec3 = new Vec3(player.posX, player.posY, player.posZ);
		for (int k2 = 0; k2 < list.size(); ++k2) {
			Entity entity = list.get(k2);
			double d0 = entity.posX - player.posX;
			double d1 = entity.posY + (double) entity.getEyeHeight() - player.posY;
			double d2 = entity.posZ - player.posZ;
			double d8 = (double) MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
			if (d8 != 0.0D) {
				d0 /= d8;
				d1 /= d8;
				d2 /= d8;
				double d10 = (double) world.getBlockDensity(vec3, entity.getEntityBoundingBox());
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
					world.spawnParticle(EnumParticleTypes.FLAME, (d0 + posX * 1.0D) / 2.0D, (d1 + posY * 1.0D) / 2.0D, (d2 + posZ * 1.0D) / 2.0D, d3, d4, d5);
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
				}
			}
		}
	}

	/**
	 * Processes right-click for Farore's Wind; returns amount of damage to apply to stack
	 */
	private int handleFarore(ItemStack stack, World world, EntityPlayer player) {
		String fail = null;
		if (canUse(stack)) {
			WarpPoint warp = recall(stack);
			if (warp == null) {
				fail = "chat.zss.spirit_crystal.farore.fail.mark";
			} else if (warp.dimensionId != player.worldObj.provider.getDimensionId()) {
				fail = "chat.zss.spirit_crystal.farore.fail.dimension";
			} else if (ZSSPlayerInfo.get(player).useMagic(10.0F)) {
				player.setPositionAndUpdate(warp.pos.getX() + 0.5D, warp.pos.getY() + 0.5D, warp.pos.getZ() + 0.5D);
				player.playSound(Sounds.SUCCESS_MAGIC, 1.0F, 1.0F);
				return costToUse;
			} // else 'magic fail' sound played below
		}
		player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
		if (fail != null && !world.isRemote) {
			PlayerUtils.sendTranslatedChat(player, fail);
		}
		return 0;
	}

	/**
	 * Processes right-click for Nayru's Love; returns amount of damage to apply to stack
	 */
	private int handleNayru(ItemStack stack, World world, EntityPlayer player) {
		if (!Config.allowUnlimitedNayru() && ZSSEntityInfo.get(player).isBuffActive(Buff.UNLIMITED_MAGIC)) {
			player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
			if (!world.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.spirit_crystal.nayru.unlimited.fail");
			}
		} else if (canUse(stack) && ZSSPlayerInfo.get(player).useMagic(25.0F)) {
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
		WarpPoint warp = new WarpPoint(world.provider.getDimensionId(), new BlockPos(player));
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
			return new WarpPoint(dimension, new BlockPos(x, y, z));
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
		list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.1"));
	}
}
