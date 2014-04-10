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

package zeldaswordskills.entity;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import zeldaswordskills.entity.EntityChu.ChuType;
import zeldaswordskills.handler.TradeHandler.EnumVillager;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;

/**
 * 
 * Additional data for handling extended villager trading system.
 *
 */
public class ZSSVillagerInfo implements IExtendedEntityProperties
{
	private final static String SAVE_KEY = "zssVillagerInfo";

	/** The villager to which these properties belong */
	private final EntityVillager villager;
	/** Additional save data for villager trading */
	private NBTTagCompound data;
	/** The type of treasure for which this villager will trade, if any */
	private int specialTrade = 1;
	/** The index of the mask that this villager wants, or -1 if none; initial value is masks.size() as a flag */
	private int desiredMask;

	/** Trade mapping for a single Treasure enum type key to the full trade recipe output */
	private static final Map<Treasures, MerchantRecipe> treasureTrades = new EnumMap<Treasures, MerchantRecipe>(Treasures.class);
	/** Maps the villager profession capable of trading for the treasure trade to the Treasure enum type key */
	private static final Map<Treasures, Integer> treasureVillager = new EnumMap<Treasures, Integer>(Treasures.class);
	/** Maps the custom villager name required for the treasure trade to the Treasure enum type key */
	private static final Map<Treasures, String> treasureCustom = new EnumMap<Treasures, String>(Treasures.class);

	/** Temporarily stores nearby village when needed for mating */
	private Village village;
	/** Temporarily stores this villager's current mate */
	private EntityVillager mate = null;
	/** Temporarily stores the time remaining before a child will be born */
	private int matingTime = 0;

	public ZSSVillagerInfo(EntityVillager villager) {
		this.villager = villager;
		data = new NBTTagCompound();
		desiredMask = EntityMaskTrader.getMaskMapSize();
	}

	public static final void register(EntityVillager villager) {
		villager.registerExtendedProperties(SAVE_KEY, new ZSSVillagerInfo(villager));
	}

	public static final ZSSVillagerInfo get(EntityVillager villager) {
		return (ZSSVillagerInfo) villager.getExtendedProperties(SAVE_KEY);
	}
	
	/**
	 * Returns the mask that this villager desires, or null if none
	 */
	public Item getMaskDesired() {
		if (desiredMask == EntityMaskTrader.getMaskMapSize()) {
			if (villager.worldObj.rand.nextFloat() < Config.getMaskBuyChance()) {
				desiredMask = villager.worldObj.rand.nextInt(EntityMaskTrader.getMaskMapSize());
			} else {
				desiredMask = -1;
			}
		}
		return (desiredMask > -1 ? EntityMaskTrader.getMask(desiredMask) : null);
	}
	
	/** Completes the mask trade, setting villager to no longer trade for masks */
	public void onMaskTrade() {
		desiredMask = -1;
	}

	/** Returns true if this villager is any type of Hunter */
	public boolean isHunter() {
		return villager.getProfession() == EnumVillager.BUTCHER.ordinal() && villager.getCustomNameTag() != null && villager.getCustomNameTag().contains("Hunter");
	}

	/** Returns true if this villager is a Monster Hunter */
	public boolean isMonsterHunter() {
		return isHunter() && villager.getCustomNameTag().equals("Monster Hunter");
	}

	/**
	 * Returns true if the villager is interested in the specified treasure
	 * @param stack the player's currently held item, which should be the treasure item
	 */
	public boolean isInterested(Treasures treasure, ItemStack stack) {
		if (treasureCustom.containsKey(treasure)) {
			boolean flag = treasureCustom.get(treasure).equals(villager.getCustomNameTag());
			if (flag && treasureCustom.get(treasure).equals("Biggoron")) {
				flag = villager instanceof EntityGoron;
			}
			if (treasure == Treasures.CLAIM_CHECK) {
				flag = stack.hasTagCompound() && stack.getTagCompound().hasKey("dateReceived") &&
						stack.getTagCompound().getInteger("dateReceived") > Calendar.DATE + 2;
			}
			return flag && specialTrade != -2 && (treasure != Treasures.TENTACLE || villager.isChild() &&
					(treasureVillager.get(treasure) == null || treasureVillager.get(treasure) == villager.getProfession()));
		}
		return (specialTrade > 0 && treasureVillager.get(treasure) == villager.getProfession());
	}

	/**
	 * Returns true if this is the final trade (the claim check) but the work
	 * is not yet finished; checks and initializes the stack's tag compound
	 * so Creative users can get the trade without running through the whole loop
	 */
	public boolean isFinalTrade(Treasures treasure, ItemStack stack) {
		if (treasure == Treasures.CLAIM_CHECK && treasureCustom.containsKey(treasure) && treasureCustom.get(treasure).equals(villager.getCustomNameTag())) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			if (!stack.getTagCompound().hasKey("dateReceived")) {
				stack.getTagCompound().setInteger("dateReceived", Calendar.DATE);
			}
			return true;
		}
		return false;
	}

	/**
	 * Called after the villager trades for a treasure to remove the trade and may set a new trade target
	 * @param stack the ItemStack to be given to the player
	 * @return true if a message should be displayed
	 */
	public boolean onTradedTreasure(Treasures treasure, ItemStack stack) {
		switch(treasure) {
		case COJIRO: specialTrade = Treasures.ODD_POTION.ordinal(); break;
		case GORON_SWORD: specialTrade = Treasures.EYE_DROPS.ordinal(); break;
		case EYE_DROPS: specialTrade = Treasures.CLAIM_CHECK.ordinal(); break;
		case CLAIM_CHECK:
			if (!stack.hasTagCompound()) { stack.setTagCompound(new NBTTagCompound()); }
			stack.getTagCompound().setInteger("dateReceived", Calendar.DATE);
			specialTrade = -2;
			break;
		default: specialTrade = (treasureCustom.containsKey(treasure) ? -2 : -1);
		}
		return specialTrade >= 0;
	}

	/** Adds the given amount of chu jellies to the current amount */
	public void addJelly(ChuType type, int amount) {
		data.setInteger(("jelliesReceived" + type.ordinal()), getJelliesReceived(type) + amount);
	}

	/** Returns the number of jellies of this type that the villager has received */
	public int getJelliesReceived(ChuType type) {
		return (data.hasKey("jelliesReceived" + type.ordinal()) ? data.getInteger("jelliesReceived" + type.ordinal()) : 0);
	}

	/** Returns whether this villager deals at all in Chu Jellies */
	public boolean isChuTrader() {
		return villager.getProfession() == EnumVillager.LIBRARIAN.ordinal() && villager.getCustomNameTag().contains("Doc");
	}

	/**
	 * Checks that this villager has received enough jellies to trade; if not, attempts
	 * to fulfill the quota from the stack provided
	 * @return true if the quota was met
	 */
	public boolean canSellType(ChuType type, ItemStack stack) {
		int jellies = getJelliesReceived(type);
		while (jellies < 15 && stack.stackSize > 0) {
			--stack.stackSize;
			++jellies;
		}
		data.setInteger(("jelliesReceived" + type.ordinal()), jellies);
		return jellies >= 15;
	}

	/** True if the villager is currently mating */
	private boolean isMating() {
		return matingTime > 0 && villager.getGrowingAge() == 0 && areSufficientDoors();
	}

	/**
	 * Sets this villager into forced mating mode if possible
	 */
	public void setMating() {
		if (villager.getGrowingAge() == 0 && !isMating()) {
			village = villager.worldObj.villageCollectionObj.findNearestVillage(MathHelper.floor_double(villager.posX), MathHelper.floor_double(villager.posY), MathHelper.floor_double(villager.posZ), 0);
			if (areSufficientDoors()) {
				Entity e = villager.worldObj.findNearestEntityWithinAABB(EntityVillager.class, villager.boundingBox.expand(8.0D, 3.0D, 8.0D), villager);
				if (e != null && ((EntityVillager) e).getGrowingAge() == 0) {
					mate = (EntityVillager) e;
					matingTime = 300;
					villager.setMating(true);
				}
			}
		}
	}

	private void updateMating() {
		if (isMating()) {
			--matingTime;
			villager.getLookHelper().setLookPositionWithEntity(mate, 10.0F, 30.0F);
			if (villager.getDistanceSqToEntity(mate) > 2.25D) {
				villager.getNavigator().tryMoveToEntityLiving(mate, 0.25D);
			} else if (matingTime == 0 && mate.isMating()) {
				giveBirth();
				resetMating();
			}
			if (villager.getRNG().nextInt(35) == 0) {
				villager.worldObj.setEntityState(villager, (byte) 12);
			}
		}
	}

	/** Returns true if there are sufficient doors, up to one villager per door (3x vanilla size) */
	private boolean areSufficientDoors() {
		return village != null ? (village.getNumVillagers() < village.getNumVillageDoors()) : false;
	}

	/** Resets the mating fields */
	private void resetMating() {
		villager.setMating(false);
		mate = null;
	}

	private void giveBirth() {
		EntityVillager baby = villager.func_90012_b(mate);
		mate.setGrowingAge(6000);
		villager.setGrowingAge(6000);
		baby.setGrowingAge(-24000);
		baby.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, 0.0F, 0.0F);
		villager.worldObj.spawnEntityInWorld(baby);
		villager.worldObj.setEntityState(baby, (byte) 12);
	}

	/**
	 * Call each update tick
	 */
	public void onUpdate() {
		updateMating();
	}

	@Override
	public void init(Entity entity, World world) {}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		compound.setTag(SAVE_KEY, data);
		compound.setInteger("specialTrade", specialTrade);
		compound.setInteger("desiredMask", desiredMask);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		data = (compound.hasKey(SAVE_KEY) ? compound.getCompoundTag(SAVE_KEY) : new NBTTagCompound());
		specialTrade = compound.getInteger("specialTrade");
		desiredMask = compound.getInteger("desiredMask");
	}

	/**
	 * Adds every component involved in a treasure trade
	 * @param treasure the treasure to be traded
	 * @param villager the profession of the villager allowed to have this trade, or null for no requirement
	 * @param required the itemstack required for the trade in addition to the Treasure
	 * @param output the itemstack to be traded for
	 */
	public static final void addTreasureTrade(Treasures treasure, EnumVillager villager, ItemStack required, ItemStack output) {
		treasureVillager.put(treasure, (villager != null ? villager.ordinal() : null));
		treasureTrades.put(treasure, new MerchantRecipe(new ItemStack(ZSSItems.treasure,1,treasure.ordinal()), required, output));
	}

	/**
	 * Adds every component involved in a treasure trade for a custom-named villager
	 * @param treasure the treasure to be traded
	 * @param name the name of the villager allowed to have this trade
	 * @param villager the profession of the villager allowed to have this trade, or null for no requirement
	 * @param required the itemstack required for the trade in addition to the Treasure
	 * @param output the itemstack to be traded for
	 */
	public static final void addTreasureTrade(Treasures treasure, String name, EnumVillager villager, ItemStack required, ItemStack output) {
		treasureCustom.put(treasure, name);
		addTreasureTrade(treasure, villager, required, output);
	}

	/** Returns the trade for this treasure */
	public static final MerchantRecipe getTreasureTrade(Treasures treasure) {
		return treasureTrades.get(treasure);
	}

	/**
	 * Initializes all custom trade maps for custom villager trades
	 */
	public static void initTrades() {
		addTreasureTrade(Treasures.EVIL_CRYSTAL,EnumVillager.PRIEST,new ItemStack(ZSSItems.arrowLight,16),new ItemStack(ZSSItems.crystalSpirit));

		addTreasureTrade(Treasures.TENTACLE,"Talon",EnumVillager.FARMER,null,new ItemStack(ZSSItems.treasure,1,Treasures.POCKET_EGG.ordinal()));
		addTreasureTrade(Treasures.POCKET_EGG,"Cucco Lady",EnumVillager.FARMER,null,new ItemStack(ZSSItems.treasure,1,Treasures.COJIRO.ordinal()));
		addTreasureTrade(Treasures.COJIRO,"Grog",EnumVillager.FARMER,null,new ItemStack(ZSSItems.treasure,1,Treasures.ODD_MUSHROOM.ordinal()));
		addTreasureTrade(Treasures.ODD_MUSHROOM,"Old Hag",EnumVillager.LIBRARIAN,null,new ItemStack(ZSSItems.treasure,1,Treasures.ODD_POTION.ordinal()));
		addTreasureTrade(Treasures.ODD_POTION,"Grog",EnumVillager.FARMER,null,new ItemStack(ZSSItems.treasure,1,Treasures.POACHER_SAW.ordinal()));
		addTreasureTrade(Treasures.POACHER_SAW,"Mutoh",EnumVillager.BLACKSMITH,null,new ItemStack(ZSSItems.treasure,1,Treasures.GORON_SWORD.ordinal()));
		addTreasureTrade(Treasures.GORON_SWORD,"Biggoron",null,null,new ItemStack(ZSSItems.treasure,1,Treasures.PRESCRIPTION.ordinal()));
		addTreasureTrade(Treasures.PRESCRIPTION,"King Zora",EnumVillager.PRIEST,null,new ItemStack(ZSSItems.treasure,1,Treasures.EYEBALL_FROG.ordinal()));
		addTreasureTrade(Treasures.EYEBALL_FROG,"Lake Scientist",EnumVillager.LIBRARIAN,null,new ItemStack(ZSSItems.treasure,1,Treasures.EYE_DROPS.ordinal()));
		addTreasureTrade(Treasures.EYE_DROPS,"Biggoron",null,null,new ItemStack(ZSSItems.treasure,1,Treasures.CLAIM_CHECK.ordinal()));
		addTreasureTrade(Treasures.CLAIM_CHECK,"Biggoron",null,null,new ItemStack(ZSSItems.swordBiggoron));
	}
}
