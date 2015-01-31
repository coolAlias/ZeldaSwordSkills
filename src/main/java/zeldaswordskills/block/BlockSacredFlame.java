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

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.block.tileentity.TileEntitySacredFlame;
import zeldaswordskills.client.render.block.RenderSacredFlame;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Colored flames representing Din (0x1), Farore (0x2), and Nayru (0x4).
 * 
 * Bit 0x8 determines whether the flame has been extinguished or is still active.
 *
 */
public class BlockSacredFlame extends BlockContainer
{
	public static final int DIN = 0x1, FARORE = 0x2, NAYRU = 0x4;

	public static final Material materialSacredFlame = new MaterialSacredFlame(MapColor.airColor);

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public BlockSacredFlame() {
		super(materialSacredFlame);
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setLightLevel(1.0F);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySacredFlame();
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public boolean isCollidable() {
		return true;
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
		return RenderSacredFlame.renderId;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		int meta = world.getBlockMetadata(x, y, z);
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ISacredFlame) {
			if (((ISacredFlame) stack.getItem()).onActivatedSacredFlame(stack, world, player, (meta & ~8), (meta & 0x8) == 0)) {
				extinguishFlame(world, x, y, z);
				return true;
			}
		} else if (!world.isRemote) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.random");
		}
		return false;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		int meta = world.getBlockMetadata(x, y, z);
		ItemStack stack = player.getHeldItem();
		if (stack != null) {
			boolean isActive = (meta & 0x8) == 0;
			if (stack.getItem() instanceof ISacredFlame) {
				if (((ISacredFlame) stack.getItem()).onClickedSacredFlame(stack, world, player, (meta & ~8), isActive)) {
					extinguishFlame(world, x, y, z);
				}
			} else if (world.isRemote) {
				;
			} else if (stack.getItem() == Items.arrow && isActive) {
				int n = stack.stackSize;
				player.setCurrentItemOrArmor(0, new ItemStack(meta == DIN ? ZSSItems.arrowFire :
					meta == NAYRU ? ZSSItems.arrowIce : ZSSItems.arrowLight, n));
				world.playSoundAtEntity(player, Sounds.SUCCESS_MAGIC, 1.0F, 1.0F);
				if (Config.getArrowsConsumeFlame() && world.rand.nextInt(80) < n) {
					extinguishFlame(world, x, y, z);
				}
			} else if (stack.getItem() == ZSSItems.crystalSpirit && isActive) {
				switch(meta) {
				case DIN: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalDin)); break;
				case FARORE: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalFarore)); break;
				case NAYRU: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalNayru)); break;
				}
				world.playSoundAtEntity(player, Sounds.FLAME_ABSORB, 1.0F, 1.0F);
				extinguishFlame(world, x, y, z);
			} else if (isActive) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.random");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.inactive");
			}
		}
	}

	/**
	 * Extinguishes the flames at this location, setting the block to air if flames are not renewable
	 */
	protected void extinguishFlame(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (Config.getSacredFlameRefreshRate() > 0 && te instanceof TileEntitySacredFlame) {
			int meta = world.getBlockMetadata(x, y, z);
			world.setBlockMetadataWithNotify(x, y, z, meta | 0x8, 3);
			((TileEntitySacredFlame) te).extinguish();
		} else {
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return World.doesBlockHaveSolidTopSurface(world, x, y - 1, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, DIN));
		list.add(new ItemStack(item, 1, FARORE));
		list.add(new ItemStack(item, 1, NAYRU));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		iconArray = new IIcon[8];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":fire" + (i / 2) + "_layer_" + (i % 2));
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFireIcon(int layer, int meta) {
		return iconArray[((meta & 0x8) == 0x8 ? 6 : (meta == DIN ? 0 : meta)) + layer];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		meta &= ~0x8;
		return iconArray[(meta == DIN ? 0 : meta)];
	}
}

/**
 * 
 * Simply a way to access protected method setNoPushMobility().
 *
 */
class MaterialSacredFlame extends Material {

	public MaterialSacredFlame(MapColor color) {
		super(color);
		setNoPushMobility();
	}
}
