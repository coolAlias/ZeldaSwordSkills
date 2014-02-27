/**
    Copyright (C) <2014> <coolAlias>

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
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntitySacredFlame;
import zeldaswordskills.client.render.block.RenderSacredFlame;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ItemSpiritCrystal;
import zeldaswordskills.item.ItemZeldaSword;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Colored flames representing Din (0x1), Farore (0x2), and Nayru (0x4)
 * 
 * Bit 0x8 determines whether the flame has been extinguished or is still active
 * 
 * When struck with the Golden Master Sword, they will imbue it with their powers,
 * unleashing the True Master Sword when all three (0x7) have been collected.
 *
 */
public class BlockSacredFlame extends BlockContainer
{
	public static final int DIN = 0x1, FARORE = 0x2, NAYRU = 0x4;
	
	public static final Material materialSacredFlame = new MaterialSacredFlame(MapColor.airColor);
	
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	public BlockSacredFlame(int id) {
		super(id, materialSacredFlame);
		disableStats();
		setBlockUnbreakable();
		setResistance(5000.0F);
		setLightValue(1.0F);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntitySacredFlame();
	}

	@Override
	public int damageDropped(int meta) { return meta; }
	
	@Override
	public boolean isCollidable() { return true; }

	@Override
	public boolean isOpaqueCube() { return false; }

	@Override
	public boolean renderAsNormalBlock() { return false; }

	@Override
	public int getRenderType() { return RenderSacredFlame.renderId; }

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean canEntityDestroy(World world, int x, int y, int z, Entity entity) {
		return false;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!world.isRemote && (meta & 0x8) == 0 && player.getHeldItem() != null) {
			ItemStack stack = player.getHeldItem();
			if (stack.getItem() instanceof ItemSword && ItemZeldaSword.onClickedSacredFlame(world, x, y, z, player)) {
				extinguishFlame(world, x, y, z);
			} else if (stack.getItem() == Item.arrow) {
				int n = stack.stackSize;
				player.setCurrentItemOrArmor(0, new ItemStack(meta == DIN ? ZSSItems.arrowFire :
					meta == NAYRU ? ZSSItems.arrowIce : ZSSItems.arrowLight, n));
				world.playSoundAtEntity(player, ModInfo.SOUND_SUCCESS, 1.0F, 1.0F);
				if (Config.getArrowsConsumeFlame() && world.rand.nextInt(80) < n) {
					extinguishFlame(world, x, y, z);
				}
			} else if (stack.getItem() == ZSSItems.crystalSpirit) {
				switch(meta) {
				case DIN: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalDin)); break;
				case FARORE: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalFarore)); break;
				case NAYRU: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalNayru)); break;
				}
				world.playSoundAtEntity(player, ModInfo.SOUND_FLAME_ABSORB, 1.0F, 1.0F);
				extinguishFlame(world, x, y, z);
			} else if (stack.getItem() instanceof ItemSpiritCrystal) {
				if (stack.getItemDamage() > 0) {
					int damage = stack.getItemDamage();
					switch(meta) {
					case DIN: if (stack.getItem() == ZSSItems.crystalDin) { stack.setItemDamage(0); } break;
					case FARORE: if (stack.getItem() == ZSSItems.crystalFarore) { stack.setItemDamage(0); } break;
					case NAYRU: if (stack.getItem() == ZSSItems.crystalNayru) { stack.setItemDamage(0); } break;
					}

					if (stack.getItemDamage() == 0) {
						world.playSoundAtEntity(player, ModInfo.SOUND_SUCCESS, 1.0F, 1.0F);
						if (world.rand.nextInt(stack.getMaxDamage()) < damage) {
							extinguishFlame(world, x, y, z);
						}
					} else {
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.spirit_crystal.sacred_flame.mismatch"));
					}
				} else {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.spirit_crystal.sacred_flame.full"));
				}
			} else {
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.sword.sacred_flame.random"));
			}
		} else if (!world.isRemote) {
			player.addChatMessage(StatCollector.translateToLocal("chat.zss.sword.sacred_flame.inactive"));
		}
	}
	
	/**
	 * Extinguishes the flames at this location, setting the block to air if flames are not renewable
	 */
	protected void extinguishFlame(World world, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
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
		return world.doesBlockHaveSolidTopSurface(x, y - 1, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int id, CreativeTabs tab, List list) {
		list.add(new ItemStack(id, 1, DIN));
		list.add(new ItemStack(id, 1, FARORE));
		list.add(new ItemStack(id, 1, NAYRU));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[8];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":fire" + (i / 2) + "_layer_" + (i % 2));
		}
	}

	@SideOnly(Side.CLIENT)
	public Icon getFireIcon(int layer, int meta) {
		return iconArray[((meta & 0x8) == 0x8 ? 6 : (meta == DIN ? 0 : meta)) + layer];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
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
