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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IQuakeBlock;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuakeStone extends Block implements IDungeonBlock, IQuakeBlock, ISmashable
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public BlockQuakeStone() {
		super(Material.rock);
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public void handleQuakeEffect(World world, int x, int y, int z, EntityPlayer player) {
		world.func_147480_a(x, y, z, true);
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta, int side) {
		return (Config.allowMegaSmashQuakeStone() && PlayerUtils.hasItem(player, ZSSItems.gauntletsGolden) ? BlockWeight.VERY_HEAVY : BlockWeight.IMPOSSIBLE);
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		return Result.DEFAULT;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(BlockQuakeStone.EnumType.byMetadata(meta).getDroppedBlock());
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			if (!world.isRemote) {
				world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
			} else if (Config.showSecretMessage) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.block.secret");
			}
		}
	}

	@Override
	public boolean isSameVariant(World world, int x, int y, int z, int expected) {
		return (world.getBlockMetadata(x, y, z) == expected);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (BlockQuakeStone.EnumType type : BlockQuakeStone.EnumType.values()) {
			list.add(new ItemStack(item, 1, type.getMetadata()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return iconArray[meta % BlockQuakeStone.EnumType.values().length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		String s = ModInfo.ID + ":quake_stone_";
		iconArray = new IIcon[BlockQuakeStone.EnumType.values().length];
		for (BlockQuakeStone.EnumType type : BlockQuakeStone.EnumType.values()) {
			iconArray[type.ordinal()] = register.registerIcon(s + type.getName());
		}
	}

	public static enum EnumType
	{
		COBBLE(0, "cobble"),
		MOSSY(1, "mossy");
		private final int meta;
		private final String unlocalizedName;

		private EnumType(int meta, String unlocalizedName) {
			this.meta = meta;
			this.unlocalizedName = unlocalizedName;
		}

		@Override
		public String toString() {
			return this.unlocalizedName;
		}

		public String getName() {
			return this.unlocalizedName;
		}

		public int getMetadata() {
			return this.meta;
		}

		/**
		 * Returns the block dropped when this type is broken
		 */
		public Block getDroppedBlock() {
			switch (this) {
			case MOSSY: return Blocks.mossy_cobblestone;
			default: return Blocks.cobblestone;
			}
		}

		public static BlockQuakeStone.EnumType byMetadata(int meta) {
			return EnumType.values()[Math.max(0, meta) % EnumType.values().length];
		}
	}
}
