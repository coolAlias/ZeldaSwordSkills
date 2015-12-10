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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.ISongTeacher;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.block.BlockSongInscription;
import zeldaswordskills.block.BlockWarpStone;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemInstrument extends Item implements IRightClickEntity
{
	public static enum Instrument {
		OCARINA_FAIRY("ocarina_fairy", GuiHandler.GUI_OCARINA, 1),
		OCARINA_TIME("ocarina_time", GuiHandler.GUI_OCARINA, 5);

		private final String unlocalizedName;

		private final int guiId;

		/** Power level of songs performed with this instrument, 0 having no effect and 5 having maximum effect */
		private final int power;

		private Instrument(String name, int guiId, int power) {
			this.unlocalizedName = name;
			this.guiId = guiId;
			this.power = Math.min(power, 5);
		}

		public String getUnlocalizedName() {
			return unlocalizedName;
		}

		public int getGuiId() {
			return guiId;
		}

		/** Returns power level of songs performed with this instrument, from 0 to 10 */
		public int getPower() {
			return power;
		}
	}

	/** Map of teacher name->song taught, retrieved by the teacher's class */
	private static final Map<Class<? extends EntityLiving>, Map<String, AbstractZeldaSong>> teachersForClass = new HashMap<Class<? extends EntityLiving>, Map<String, AbstractZeldaSong>>();

	@SideOnly(Side.CLIENT)
	private List<IIcon> icons;

	public ItemInstrument() {
		super();
		setMaxDamage(0);
		setHasSubtypes(true);
		setMaxStackSize(1);
		setUnlocalizedName("zss.instrument");
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	public Instrument getInstrument(ItemStack stack) {
		return Instrument.values()[stack.getItemDamage() % Instrument.values().length];
	}

	/**
	 * Returns {@link Instrument#getPower} for determining effects of the {@link AbstractZeldaSong}
	 */
	public int getSongStrength(ItemStack stack) {
		return getInstrument(stack).getPower();
	}

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) {
		if (getInstrument(stack) == ItemInstrument.Instrument.OCARINA_FAIRY) {
			player.triggerAchievement(ZSSAchievements.ocarinaCraft);
		}
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		Block block = world.getBlock(x, y, z);
		return block instanceof BlockWarpStone || block instanceof BlockSongInscription;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) { // instruments have client-side only Guis
			// check if song to learn was set from entity interaction
			if (ZSSPlayerSongs.get(player).songToLearn != null) {
				player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, 0, 0, 0);
			} else {
				player.openGui(ZSSMain.instance, getInstrument(stack).getGuiId(), world, 0, 0, 0);
			}
		}
		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			if (isScarecrowAt(world, x, y, z) && ZSSPlayerSongs.get(player).canOpenScarecrowGui(true)) {
				player.openGui(ZSSMain.instance, GuiHandler.GUI_SCARECROW, world, x, y, z);
			} else {
				player.openGui(ZSSMain.instance, getInstrument(stack).getGuiId(), world, x, y, z);
			}
		}
		return true;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	public boolean onRightClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (entity instanceof EntityLiving) {
			return handleRightClickEntity(stack, player, (EntityLiving) entity);
		}
		return false;
	}

	private boolean handleRightClickEntity(ItemStack stack, EntityPlayer player, EntityLiving entity) {
		ISongTeacher.TeachingResult result = null;
		if (entity instanceof ISongTeacher) {
			result = ((ISongTeacher) entity).getTeachingResult(stack, player);
		} else if (!player.isSneaking() && entity.hasCustomNameTag() && teachersForClass.containsKey(entity.getClass())) {
			Map<String, AbstractZeldaSong> teacherSongs = teachersForClass.get(entity.getClass());
			result = new ISongTeacher.TeachingResult(teacherSongs.get(entity.getCustomNameTag()), true, true);
		}
		if (result != null) {
			if (player.worldObj.isRemote && result.songToLearn != null) {
				ZSSPlayerSongs.get(player).songToLearn = result.songToLearn;
				if (!result.displayChat) {
					// don't display default chat messages
				} else if (ZSSPlayerSongs.get(player).isSongKnown(result.songToLearn)) {
					PlayerUtils.sendFormattedChat(player, "chat.zss.npc.ocarina.review", new ChatComponentTranslation(result.songToLearn.getTranslationString()));
				} else {
					PlayerUtils.sendFormattedChat(player, "chat.zss.npc.ocarina.learn", new ChatComponentTranslation(result.songToLearn.getTranslationString()));
				}
			}
			return result.cancel; // If true, skips straight to Item#onItemRightClick
		}
		return false; // Entity#interact will determine if Item#onItemRightClick is called
	}

	/**
	 * Returns true if the blocks around x/y/z form a scarecrow figure,
	 * assuming that x/y/z is one of the central blocks (not the 'arms')
	 */
	private boolean isScarecrowAt(World world, int x, int y, int z) {
		int i = 0;
		while (i < 2 && world.getBlock(x, y, z) == Blocks.hay_block) {
			++i;
			++y;
		}
		// should now always have the head
		Block block = world.getBlock(x, y, z);
		if (block instanceof BlockPumpkin) {
			--y;
			for (int dy = i; dy < 2; ++dy) {
				if (world.getBlock(x, y - dy, z) != Blocks.hay_block) {
					return false;
				}
			}
			if (world.getBlock(x + 1, y, z) == Blocks.hay_block && world.getBlock(x - 1, y, z) == Blocks.hay_block) {
				return true;
			}
			if (world.getBlock(x, y, z + 1) == Blocks.hay_block && world.getBlock(x, y, z - 1) == Blocks.hay_block) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + Instrument.values()[stack.getItemDamage() % Instrument.values().length].unlocalizedName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return icons.get(damage % icons.size());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (Instrument instrument : Instrument.values()) {
			list.add(new ItemStack(item, 1, instrument.ordinal()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		icons = new ArrayList<IIcon>(Instrument.values().length);
		for (Instrument instrument : Instrument.values()) {
			icons.add(register.registerIcon(ModInfo.ID + ":" + instrument.getUnlocalizedName()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.instrument." + getInstrument(stack).getUnlocalizedName() + ".desc"));
	}

	static {
		Map<String, AbstractZeldaSong> teacherSongs = new HashMap<String, AbstractZeldaSong>();
		teacherSongs.put("Guru-Guru", ZeldaSongs.songStorms);
		teacherSongs.put("Impa", ZeldaSongs.songZeldasLullaby);
		teacherSongs.put("Malon", ZeldaSongs.songEpona);
		teacherSongs.put("Saria", ZeldaSongs.songSaria);
		teacherSongs.put("Zelda", ZeldaSongs.songTime);
		teachersForClass.put(EntityVillager.class, teacherSongs);
	}
}
