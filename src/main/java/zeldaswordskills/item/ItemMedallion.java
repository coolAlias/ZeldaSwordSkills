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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.IQuakeBlock;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceQuakeIndirect;
import zeldaswordskills.block.BlockAncientTablet;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.EntityEtherLightning;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.PacketISpawnParticles;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;

public class ItemMedallion extends BaseModItem implements ISpawnParticles
{
	public ItemMedallion() {
		super();
		setFull3D();
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "_" + BlockAncientTablet.EnumType.byMetadata(stack.getItemDamage()).getName();
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return BlockAncientTablet.EnumType.byMetadata(stack.getItemDamage()).getItemUseDuration();
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		int requiredTicks = getMaxItemUseDuration(stack);
		// check magic requirements here?
		if (requiredTicks > 0) {
			player.setItemInUse(stack, requiredTicks);
		}
		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		if (count % 4 == 0) {
			// TODO will other players see these? they should - it's called on both sides
			for (int i = 0; i < 4; ++i) {
				player.worldObj.spawnParticle(EnumParticleTypes.SPELL_WITCH, player.posX + player.worldObj.rand.nextGaussian() * 0.13D, player.posY + (player.height / 2.0F) + player.worldObj.rand.nextGaussian() * 0.13D, player.posZ + player.worldObj.rand.nextGaussian() * 0.13D, 0.0D, 0.0D, 0.0D);
			}
		}
		if (count % 10 == 0) {
			// TODO play rumbling sound, change % ticks to match sound file length
		}
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		if (!ZSSPlayerInfo.get(player).useMagic(40.0F)) {
			player.playSound(Sounds.MAGIC_FAIL, 1.0F, 1.0F);
			return stack;
		} else if (world.isRemote) {
			return stack;
		}
		BlockAncientTablet.EnumType type = BlockAncientTablet.EnumType.byMetadata(stack.getItemDamage());
		switch (type) {
		case ETHER:
			// TODO is Ether only available for use in certain dimensions?
			world.playSoundEffect(player.posX, player.posY, player.posZ, "ambient.weather.thunder", 10000.0F, 0.8F + world.rand.nextFloat() * 0.2F);
			world.playSoundEffect(player.posX, player.posY, player.posZ, "random.explode", 2.0F, 0.5F + world.rand.nextFloat() * 0.2F);
			affectEntities(world, player, type, 8.0F);
			if (world instanceof WorldServer && (world.isRaining() || world.isThundering())) {
				WorldInfo worldinfo = ((WorldServer) world).getWorldInfo();
				worldinfo.setRaining(false);
				worldinfo.setRainTime(0);
				worldinfo.setThundering(false);
				worldinfo.setThunderTime(0);
			}
			break;
		case QUAKE:
			world.playSoundEffect(player.posX, player.posY, player.posZ, Sounds.ROCK_FALL, 1.0F, 1.0F);
			PacketDispatcher.sendToAllAround(new PacketISpawnParticles(player, 8.0F), player, 64.0D);
			affectBlocks(world, player, type, 8.0F);
			affectEntities(world, player, type, 8.0F);
			break;
		default: // do nothing
		}
		return stack;
	}

	/**
	 * Affects all applicable entities with the type's effect within the given radius
	 */
	private void affectEntities(World world, EntityPlayer player, BlockAncientTablet.EnumType type, float radius) {
		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().expand(radius, radius, radius));
		for (EntityLivingBase entity : list) {
			if (!canAffectEntity(player, entity)) {
				continue;
			}
			switch (type) {
			case ETHER:
				if (!world.isRemote) {
					world.addWeatherEffect(new EntityEtherLightning(world, player, entity, entity.posX, entity.posY, entity.posZ));
				}
				break;
			case QUAKE: // only affects entities on ground
				if (entity.onGround) {
					if (world.isRemote) {
						for (int i = 0; i < 4; ++i) {
							world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, entity.posX, entity.posY + (entity.height / 2.0F), entity.posZ, 0, 0, 0);
						}
					} else {
						int duration = world.rand.nextInt(20) + world.rand.nextInt(20) + 20;
						DamageSource quakeSource = new DamageSourceQuakeIndirect("quake", null, player, duration, 0).setMagicDamage().setDamageBypassesArmor();
						entity.attackEntityFrom(quakeSource, 10.0F);
						world.playSoundEffect(entity.posX, entity.posY, entity.posZ, Sounds.ROCK_FALL, 1.0F, 1.0F);
					}
				}
				break;
			default: // do nothing
			}
		}
	}

	private boolean canAffectEntity(EntityPlayer player, EntityLivingBase target) {
		if (target instanceof IMob || (target instanceof EntityPlayer && Config.doMedallionsDamagePlayers())) {
			return (target != player && !target.isOnSameTeam(player));
		}
		return false;
	}

	/**
	 * Affects blocks on both sides (only used by Quake Medallion)
	 */
	private void affectBlocks(World world, EntityPlayer player, BlockAncientTablet.EnumType type, float radius) {
		if (type != BlockAncientTablet.EnumType.QUAKE) {
			return; // only QUAKE medallion currently affects blocks
		}
		int r = MathHelper.ceiling_float_int(radius);
		int ry = Math.max(1, (r / 2));
		for (int i = 0; i <= r; ++i) {
			for (int j = -ry; j <= ry; ++j) {
				for (int k = 0; k <= r; ++k) {
					affectBlockAt(world, player, new BlockPos(player.posX + i, player.posY + j, player.posZ + k));
					affectBlockAt(world, player, new BlockPos(player.posX + i, player.posY + j, player.posZ - k));
					affectBlockAt(world, player, new BlockPos(player.posX - i, player.posY + j, player.posZ + k));
					affectBlockAt(world, player, new BlockPos(player.posX - i, player.posY + j, player.posZ - k));
				}
			}
		}
	}

	/**
	 * Affects a single block position (only used for Quake Medallion)
	 */
	private void affectBlockAt(World world, EntityPlayer player, BlockPos pos) {
		if (world.isRemote) {
			spawnParticlesAt(world, pos);
		} else { // check for special block interactions on server
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof IQuakeBlock) {
				((IQuakeBlock) state.getBlock()).handleQuakeEffect(world, pos, state, player);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void spawnParticles(World world, EntityPlayer player, ItemStack stack, double x, double y, double z, float r) {
		if (stack.getItemDamage() != BlockAncientTablet.EnumType.QUAKE.ordinal()) {
			return;
		}
		// affect all blocks and entities client side for particle effects
		affectBlocks(world, player, BlockAncientTablet.EnumType.QUAKE, r);
		affectEntities(world, player, BlockAncientTablet.EnumType.QUAKE, r);
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticlesAt(World world, BlockPos pos) {
		if (!world.isAirBlock(pos)) {
			return;
		}
		IBlockState state = world.getBlockState(pos.down());
		Block block = state.getBlock();
		if (block.getRenderType() != -1) {
			int stateId = Block.getStateId(state);
			for (int n = 0; n < 4; ++n) {
				double dx = pos.getX() + world.rand.nextFloat() - 0.5F;
				double dy = pos.getY() + world.rand.nextFloat() * 0.2F;
				double dz = pos.getZ() + world.rand.nextFloat() - 0.5F;
				world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, dx, dy, dz, world.rand.nextGaussian(), 0, world.rand.nextGaussian(), stateId);
			}
			if (world.rand.nextInt(8) == 0) {
				world.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), Sounds.ROCK_FALL, 1.0F, 1.0F);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (BlockAncientTablet.EnumType type : BlockAncientTablet.EnumType.values()) {
			list.add(new ItemStack(item, 1, type.ordinal()));
		}
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[BlockAncientTablet.EnumType.values().length];
		for (BlockAncientTablet.EnumType type : BlockAncientTablet.EnumType.values()) {
			variants[type.ordinal()] = ModInfo.ID + ":medallion_" + type.getName();
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		BlockAncientTablet.EnumType type = BlockAncientTablet.EnumType.byMetadata(stack.getItemDamage());
		list.add(StatCollector.translateToLocal("tooltip.zss.medallion." + type.getName() + ".desc.0"));
	}
}
