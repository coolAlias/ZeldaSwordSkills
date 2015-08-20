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

package zeldaswordskills.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.client.render.block.RenderAncientTablet;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.EntityInvulnerableItem;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAncientTablet extends Block implements IWhipBlock
{
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom, iconTop, iconSide, iconBottomRotated, iconTopRotated;

	public BlockAncientTablet(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setBlockBounds(0.125F, 0.0F, 0.375F, 0.875F, 0.9375F, 0.625F);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return side > 1; // not top or bottom
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, int x, int y, int z, int ticksInGround) {
		return Result.DEFAULT;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return RenderAncientTablet.renderId;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public int damageDropped(int meta) {
		// item version has damage values 0-2 based on variant ordinal
		return BlockAncientTablet.EnumType.byMetadata(meta).ordinal();
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		BlockAncientTablet.EnumType type = BlockAncientTablet.EnumType.byMetadata(world.getBlockMetadata(x, y, z));
		if (world.getBlockLightValue(x, y, z) < 8) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.dark");
		} else if (player.getHeldItem() == null || player.getHeldItem().getItem() != ZSSItems.bookMudora) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.unknown");
		} else if (!PlayerUtils.hasMasterSword(player)) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.text." + type.name);
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.text." + type.name);
			for (int i = 1; i < 6; ++i) {
				if (!world.isAirBlock(x, y + i, z)) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.confined");
					return false; // must be at least 5 blocks clear for lightning and item spawn
				}
			}
			world.addWeatherEffect(new EntityLightningBolt(world, x, y, z));
			world.scheduleBlockUpdateWithPriority(x, y, z, this, 5, 1);
			return true;
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		int face = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int meta = ((face & 1) == 0 ? 1 : 0);
		meta |= (stack.getItemDamage() < 4 ? (stack.getItemDamage() << 2) : stack.getItemDamage()); // item damage uses type ordinal, i.e. 0-2
		world.setBlockMetadataWithNotify(x, y, z, meta, 2);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		int i = BlockAncientTablet.EnumType.byMetadata(world.getBlockMetadata(x, y, z)).ordinal();
		EntityItem item = new EntityInvulnerableItem(world, x + 0.5D, y + 5.5D, z + 0.5D, new ItemStack(ZSSItems.medallion, 1, i));
		item.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(item);
		world.func_147480_a(x, y, z, false);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		if ((world.getBlockMetadata(x, y, z) & 1) == 0) {
			setBlockBounds(0.375F, 0.0F, 0.125F, 0.625F, 0.9375F, 0.875F);
		} else {
			setBlockBounds(0.125F, 0.0F, 0.375F, 0.875F, 0.9375F, 0.625F);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.125F, 0.0F, 0.375F, 0.875F, 0.9375F, 0.625F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (BlockAncientTablet.EnumType variant : BlockAncientTablet.EnumType.values()) {
			list.add(new ItemStack(item, 1, variant.ordinal()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		switch (meta & 1) {
		case 0: // east/west
			return (side == 0 ? iconBottomRotated : (side == 1 ? iconTopRotated : (side == 4 || side == 5 ? blockIcon : iconSide)));
		case 1: // north/south
			return (side == 0 ? iconBottom : (side == 1 ? iconTop : (side == 2 || side == 3 ? blockIcon : iconSide)));
		}
		return blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		String s = ModInfo.ID + ":" + getUnlocalizedName().substring(9);
		blockIcon = register.registerIcon(s + "_face");
		iconBottom = register.registerIcon(s + "_bottom");
		iconBottomRotated = register.registerIcon(s + "_bottom_rotated");
		iconSide = register.registerIcon(s + "_side");
		iconTop = register.registerIcon(s + "_top");
		iconTopRotated = register.registerIcon(s + "_top_rotated");
	}

	public static enum EnumType {
		BOMBOS(0, "bombos", -1),
		ETHER(4, "ether", 30),
		QUAKE(8, "quake", 30);
		private final int meta;
		private final String name;
		private final int itemUseDuration;

		private EnumType(int meta, String name, int maxUseDuration) {
			this.meta = meta;
			this.name = name;
			this.itemUseDuration = maxUseDuration;
		}

		public String getName() {
			return name;
		}

		public int getMetadata() {
			return this.meta;
		}

		/**
		 * Number of ticks medallion must be used before effect occurs
		 * @return -1 if the medallion may not be used, 0 if the effect is instant, or the duration required
		 */
		public int getItemUseDuration() {
			return itemUseDuration;
		}

		/**
		 * Return block variant by metadata (0, 4, 8) or damage (0, 1, 2) value
		 */
		public static EnumType byMetadata(int meta) {
			return EnumType.values()[(meta > 3 ? (meta >> 2) : meta) % EnumType.values().length];
		}
	}
}
