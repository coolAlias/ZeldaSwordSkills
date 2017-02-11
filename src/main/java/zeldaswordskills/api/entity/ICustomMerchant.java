package zeldaswordskills.api.entity;

import net.minecraft.entity.IMerchant;
import net.minecraft.village.MerchantRecipeList;

/**
 * Use this interface instead of vanilla IMerchant if you plan to have
 * your NPC merchant created via {@link NpcHelper#convertVillager}
 */
public interface ICustomMerchant extends IMerchant {
	/**
	 * Set the merchant's recipe (i.e. trade) list.
	 * Vanilla IMerchant#setRecipes is @SideOnly(Side.CLIENT)
	 * This method is used instead to avoid crashing dedicated servers.
	 */
	void setMerchantTrades(MerchantRecipeList trades);
}
