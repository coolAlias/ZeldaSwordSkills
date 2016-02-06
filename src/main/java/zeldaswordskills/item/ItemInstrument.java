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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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

public class ItemInstrument extends BaseModItem implements IRightClickEntity
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

	public ItemInstrument() {
		super();
		setMaxDamage(0);
		setHasSubtypes(true);
		setMaxStackSize(1);
		setUnlocalizedName("instrument");
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
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
		Block block = world.getBlockState(pos).getBlock();
		return block instanceof BlockWarpStone || block instanceof BlockSongInscription;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) { // instruments have client-side only Guis
			// check if song to learn was set from entity interaction
			ZSSPlayerSongs songs = ZSSPlayerSongs.get(player);
			if (songs.preventSongGui) {
				songs.preventSongGui = false; // prevent opening GUI this time only
			} else if (songs.songToLearn != null) {
				player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, 0, 0, 0);
			} else {
				player.openGui(ZSSMain.instance, getInstrument(stack).getGuiId(), world, 0, 0, 0);
			}
		}
		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			if (isScarecrowAt(world, pos) && ZSSPlayerSongs.get(player).canOpenScarecrowGui(true)) {
				player.openGui(ZSSMain.instance, GuiHandler.GUI_SCARECROW, world, pos.getX(), pos.getY(), pos.getZ());
			} else {
				player.openGui(ZSSMain.instance, getInstrument(stack).getGuiId(), world, 0, 0, 0);
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
		ISongTeacher.TeachingResult result = null;
		if (entity instanceof ISongTeacher) {
			result = ((ISongTeacher) entity).getTeachingResult(stack, player);
		} else if (!player.isSneaking() && entity.hasCustomName() && teachersForClass.containsKey(entity.getClass())) {
			Map<String, AbstractZeldaSong> teacherSongs = teachersForClass.get(entity.getClass());
			result = new ISongTeacher.TeachingResult(teacherSongs.get(entity.getCustomNameTag()), true, true);
		}
		if (result != null) {
			if (player.worldObj.isRemote && result.songToLearn != null) {
				ZSSPlayerSongs.get(player).songToLearn = result.songToLearn;
				if (!result.displayChat) {
					// don't display default chat messages
				} else if (ZSSPlayerSongs.get(player).isSongKnown(result.songToLearn)) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.ocarina.review", new ChatComponentTranslation(result.songToLearn.getTranslationString()));
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.ocarina.learn", new ChatComponentTranslation(result.songToLearn.getTranslationString()));
				}
			}
			return result.cancel; // If true, skips straight to Item#onItemRightClick
		}
		return false; // Entity#interact will determine if Item#onItemRightClick is called
	}

	/**
	 * Returns true if the blocks around the position form a scarecrow figure,
	 * assuming that the given position is one of the central blocks (not the 'arms')
	 */
	private boolean isScarecrowAt(World world, BlockPos pos) {
		int i = 0;
		while (i < 2 && world.getBlockState(pos).getBlock() == Blocks.hay_block) {
			++i;
			pos = pos.up();
		}
		// should now always have the head
		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof BlockPumpkin) {
			pos = pos.down();
			for (int dy = i; dy < 2; ++dy) {
				if (world.getBlockState(pos.down(dy)).getBlock() != Blocks.hay_block) {
					return false;
				}
			}
			if (world.getBlockState(pos.east()).getBlock() == Blocks.hay_block && world.getBlockState(pos.west()).getBlock() == Blocks.hay_block) {
				return true;
			}
			if (world.getBlockState(pos.north()).getBlock() == Blocks.hay_block && world.getBlockState(pos.south()).getBlock() == Blocks.hay_block) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + Instrument.values()[stack.getItemDamage() % Instrument.values().length].unlocalizedName;
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[Instrument.values().length];
		for (Instrument type : Instrument.values()) {
			variants[type.ordinal()] = ModInfo.ID + ":" + type.unlocalizedName;
		}
		return variants;
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
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.instrument." + getInstrument(stack).getUnlocalizedName() + ".desc"));
	}

	static {
		Map<String, AbstractZeldaSong> teacherSongs = new HashMap<String, AbstractZeldaSong>();
		teacherSongs.put("Guru-Guru", ZeldaSongs.songStorms);
		teacherSongs.put("Impa", ZeldaSongs.songZeldasLullaby);
		teacherSongs.put("Malon", ZeldaSongs.songEpona);
		teacherSongs.put("Saria", ZeldaSongs.songSaria);
		teachersForClass.put(EntityVillager.class, teacherSongs);
	}
}
