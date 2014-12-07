package zeldaswordskills.entity.npc;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

public class EntityNpcBarnes extends EntityNpcBase implements IMerchant
{
	private static final MerchantRecipe standardBomb = new MerchantRecipe(new ItemStack(Items.emerald, 8), new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal()));
	private static final MerchantRecipe waterBomb = new MerchantRecipe(new ItemStack(Items.emerald, 12), new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()));
	private static final MerchantRecipe fireBomb = new MerchantRecipe(new ItemStack(Items.emerald, 16), new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FIRE.ordinal()));

	/** Barnes' current customer */
	private EntityPlayer customer;

	/** MerchantRecipeList of all currently available trades */
	private MerchantRecipeList trades;

	public EntityNpcBarnes(World world) {
		super(world);
	}

	@Override
	protected String getNameTagOnSpawn() {
		return "Barnes";
	}

	@Override
	protected String getLivingSound() {
		return Sounds.VILLAGER_HAGGLE;
	}

	@Override
	protected String getHurtSound() {
		return Sounds.VILLAGER_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.VILLAGER_DEATH;
	}

	@Override
	public void setCustomer(EntityPlayer player) {
		customer = player;
	}

	@Override
	public EntityPlayer getCustomer() {
		return customer;
	}

	@Override
	public MerchantRecipeList getRecipes(EntityPlayer player) {
		if (trades == null || trades.isEmpty()) {
			addDefaultTrades();
		}
		return trades;
	}

	@Override
	public void setRecipes(MerchantRecipeList trades) {
		this.trades = trades;
	}

	@Override
	public void useRecipe(MerchantRecipe trade) {
		trade.incrementToolUses();
		livingSoundTime = -getTalkInterval();
		playSound("mob.villager.yes", getSoundVolume(), getSoundPitch());
		/*
		if (trade.hasSameIDsAs((MerchantRecipe) trades.get(trades.size() - 1))) {
			this.timeUntilReset = 40;
			this.needsInitilization = true;
			if (getCustomer() != null) {
				this.lastBuyingPlayer = getCustomer().getCommandSenderName();
			} else {
				this.lastBuyingPlayer = null;
			}
		}
		 */
	}

	private void addDefaultTrades() {
		MerchantRecipeList newTrades = new MerchantRecipeList();
		newTrades.add(standardBomb);
		if (trades == null) {
			trades = newTrades;
		} else {
			// Add any previous trades after bomb-related trades
			for (int i = 0; i < trades.size(); ++i) {
				MerchantRecipeHelper.addToListWithCheck(newTrades, (MerchantRecipe) trades.get(i));
			}
			trades = newTrades;
		}
	}

	@Override
	public void func_110297_a_(ItemStack stack) {
		if (!worldObj.isRemote && livingSoundTime > -getTalkInterval() + 20) {
			livingSoundTime = -getTalkInterval();
			playSound((stack == null ? "mob.villager.no" : "mob.villager.yes"), getSoundVolume(), getSoundPitch());
		}
	}

	@Override
	public boolean interact(EntityPlayer player) {
		if (isEntityAlive() && getCustomer() == null && !player.isSneaking()) {
			ItemStack stack = player.getHeldItem();
			String chat = "chat.zss.npc.barnes.greeting";
			boolean openGui = true;
			if (stack != null && trades != null) {
				if (stack.getItem() == Items.fish) {
					if (insertBombTrade(waterBomb)) {
						--stack.stackSize;
						chat = "chat.zss.npc.barnes.trade.water";
						openGui = false;
					}
				} else if (stack.getItem() == Items.magma_cream) {
					if (insertBombTrade(fireBomb)) {
						--stack.stackSize;
						chat = "chat.zss.npc.barnes.trade.fire";
						openGui = false;
					}
				} else if (!MerchantRecipeHelper.hasSimilarTrade(trades, waterBomb)) {
					chat = "chat.zss.npc.barnes.material.water";
				} else if (!MerchantRecipeHelper.hasSimilarTrade(trades, fireBomb)) {
					chat = "chat.zss.npc.barnes.material.fire";
				}
			}
			if (!worldObj.isRemote) {
				PlayerUtils.sendChat(player, StatCollector.translateToLocal(chat));
				if (openGui) {
					setCustomer(player);
					player.displayGUIMerchant(this, getCustomNameTag());
				}
			}
			return true;
		} else {
			return super.interact(player);
		}
	}

	/**
	 * Returns true if a Bomb Bag trade was added (must be enabled in Config)
	 */
	public boolean addBombBagTrade() {
		return (Config.enableTradeBombBag() && insertBombTrade(new MerchantRecipe(new ItemStack(Items.emerald, Config.getBombBagPrice()), new ItemStack(ZSSItems.bombBag))));
	}

	/**
	 * Inserts any trade at the first available slot after any bombs already in stoc
	 * @return true if the trade was inserted, or false if a similar trade already existed
	 */
	private boolean insertBombTrade(MerchantRecipe trade) {
		if (trades == null) {
			return false;
		}
		if (MerchantRecipeHelper.hasSimilarTrade(trades, trade)) {
			return false;
		}
		for (int i = 0; i < trades.size(); ++i) {
			MerchantRecipe r = (MerchantRecipe) trades.get(i);
			if (r.getItemToSell().getItem() instanceof ItemBomb) {
				continue;
			} else {
				trades.add(i, trade);
				return true;
			}
		}
		trades.add(trade);
		return true;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		if (trades != null) {
			compound.setTag("Offers", trades.getRecipiesAsTags());
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("Offers", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound tradeTag = compound.getCompoundTag("Offers");
			trades = new MerchantRecipeList(tradeTag);
		}
	}

	/**
	 * Attempts to convert the given villager into Barnes, the NPC; whatever
	 * trades the villager may have had will continue to be available through Barnes
	 * @param villager	The original villager will be set to dead if converted
	 * @param stack		ItemStack that the player is holding, may be null
	 * @return			True if the villager was transformed into Barnes
	 */
	public static boolean convertFromVillager(EntityVillager villager, EntityPlayer player, ItemStack stack) {
		if (stack != null && stack.getItem() == Items.gunpowder) {
			EntityNpcBarnes barnes = new EntityNpcBarnes(villager.worldObj);
			barnes.setRecipes(villager.getRecipes(player));
			barnes.addDefaultTrades();
			barnes.setCustomNameTag(villager.getCustomNameTag());
			barnes.setLocationAndAngles(villager.posX, villager.posY + 1, villager.posZ, villager.rotationYaw, villager.rotationPitch);
			if (!villager.worldObj.isRemote) {
				villager.setDead();
				villager.worldObj.spawnEntityInWorld(barnes);
			}
			PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.npc.barnes.open"));
			return true;
		} else {
			PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.npc.barnes.hmph"));
		}
		return false;
	}
}
