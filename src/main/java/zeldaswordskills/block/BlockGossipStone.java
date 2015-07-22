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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.StringUtils;
import zeldaswordskills.util.TimedChatDialogue;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGossipStone extends Block implements IHookable, ILiftable, ISmashable, ISongBlock
{
	@SideOnly(Side.CLIENT)
	private IIcon topIcon;

	public BlockGossipStone() {
		super(ZSSBlockMaterials.adventureStone);
		setHardness(10.0F);
		setHarvestLevel("pickaxe", 2);
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileEntityGossipStone();
	}

	@Override
	public Result canDestroyBlock(HookshotType type, World world, int x, int y, int z, int side) {
		return Result.DENY;
	}

	@Override
	public Result canGrabBlock(HookshotType type, World world, int x, int y, int z, int side) {
		return Result.DEFAULT;
	}

	@Override
	public Material getHookableMaterial(HookshotType type, World world, int x, int y, int z) {
		return Material.rock;
	}

	@Override
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, int meta, int side) {
		return (meta == 0) ? BlockWeight.MEDIUM : BlockWeight.IMPOSSIBLE;
	}

	@Override
	public void onLifted(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int meta) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityGossipStone) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			String msg = ((TileEntityGossipStone) te).getMessage();
			stack.getTagCompound().setString("TegsMessage", msg);
		}
	}

	@Override
	public void onHeldBlockPlaced(World world, ItemStack stack, int x, int y, int z, int meta) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("TegsMessage")) {
			ZSSMain.logger.warn("Held GossipBlock stack had an invalid NBT tag: unable to set message.");
			return;
		}
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityGossipStone) {
			String msg = stack.getTagCompound().getString("TegsMessage");
			((TileEntityGossipStone) te).setMessage(msg);
		}
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta, int side) {
		return (meta == 0) ? BlockWeight.VERY_HEAVY : BlockWeight.IMPOSSIBLE;
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		return (world.getBlockMetadata(x, y, z) == 0) ? Result.DEFAULT : Result.DENY;
	}

	@Override
	public boolean onSongPlayed(World world, int x, int y, int z, EntityPlayer player, AbstractZeldaSong song, int power, int affected) {
		TileEntity te = world.getTileEntity(x, y, z);
		return (te instanceof TileEntityGossipStone && ((TileEntityGossipStone) te).onSongPlayed(player, song, power, affected));
	}

	@Override
	public int damageDropped(int meta) {
		return 0; // only drop breakable version
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
		return world.getBlockMetadata(x, y, z) == 0;
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		return (world.getBlockMetadata(x, y, z) == 0) ? getExplosionResistance(entity) : BlockWeight.getMaxResistance();
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return (world.getBlockMetadata(x, y, z) == 0) ? blockHardness : -1.0F;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (!world.isRemote && te instanceof TileEntityGossipStone) {
			ItemStack helm = player.getEquipmentInSlot(ArmorIndex.EQUIPPED_HELM);
			if (helm != null && helm.getItem() == ZSSItems.maskTruth) {
				String msg = ((TileEntityGossipStone) te).getMessage();
				String messages[] = StringUtils.wrapString(msg, TileEntityGossipStone.LINE_LENGTH, 5);
				new TimedChatDialogue(player, messages);
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.block.gossip_stone.silent");
			}
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (world.isRemote) {
			return;
		}
		if (PlayerUtils.isHoldingSword(player)) {
			long time = world.getWorldTime();
			long days = (time / 24000L);
			long current = (time + 6000L) % 24000L; // 0 is 6:00 am, 18000 is midnight, so add 6000
			int h = (int)(current / 1000L);
			int m = (int)((current % 1000L) * 3 / 50); // 1000 ticks divided by 60 minutes = 16 and 2/3
			PlayerUtils.sendChat(player, String.format("Current time is %s:%s of day %s", String.format("%02d", h), String.format("%02d", m), days));
		} else if (world.getBlockMetadata(x, y, z) > 0 && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 8));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side < 2 ? topIcon : blockIcon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		String s = ModInfo.ID + ":" + getUnlocalizedName().substring(9);
		blockIcon = register.registerIcon(s);
		topIcon = register.registerIcon(s + "_top");
	}
}
