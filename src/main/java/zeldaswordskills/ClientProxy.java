/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import zeldaswordskills.block.ICustomStateMapper;
import zeldaswordskills.block.ISpecialRenderer;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.client.ISwapModel;
import zeldaswordskills.client.TargetingTickHandler;
import zeldaswordskills.client.ZSSClientEvents;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.ZSSEntities;
import zeldaswordskills.item.IModItem;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.world.gen.AntiqueAtlasHelper;

import com.google.common.collect.Maps;

public class ClientProxy extends CommonProxy
{
	private final Minecraft mc = Minecraft.getMinecraft();
	/** Stores all models which need to be replaced during {@link ModelBakeEvent} */
	@SuppressWarnings("deprecation")
	public static final Map<ModelResourceLocation, Class<? extends net.minecraft.client.resources.model.IBakedModel>> smartModels = Maps.newHashMap();
	/** Accessible version of EffectRenderer's IParticleFactory map */
	public static Map<Integer, IParticleFactory> particleFactoryMap;

	/**
	 * All item / block variants must be registered at this time
	 */
	@Override
	public void preInit() {
		super.preInit();
		registerVariants();
		MinecraftForge.EVENT_BUS.register(new ZSSClientEvents());
		MinecraftForge.EVENT_BUS.register(new TargetingTickHandler());
		MinecraftForge.EVENT_BUS.register(new ZSSKeyHandler());
		UnpressKeyPacket.init();
	}

	/**
	 * All renderers must be registered at this time
	 */
	@Override
	public void init() {
		registerRenderers();
		ZSSEntities.registerRenderers();
		AntiqueAtlasHelper.registerTextures();
		Object o = ReflectionHelper.getPrivateValue(EffectRenderer.class, Minecraft.getMinecraft().effectRenderer, 6);
		if (o instanceof Map) {
			particleFactoryMap = (Map<Integer, IParticleFactory>) o;
		}
	}

	@Override
	public int addArmor(String armor) {
		return 0;// TODO is this even necessary any longer? RenderingRegistry.addNewArmourRendererPrefix(armor);
	}

	/**
	 * Automated variant and custom state mapper registration for blocks and items
	 * Utilizes {@link IModItem#registerVariants()} and {@link ICustomStateMapper#getCustomStateMap()}
	 * Call during FMLPreInitializationEvent after all Blocks and Items have been initialized
	 */
	private void registerVariants() {
		try {
			for (Field f: ZSSBlocks.class.getFields()) {
				if (Block.class.isAssignableFrom(f.getType())) {
					Block block = (Block) f.get(null);
					if (block != null) {
						if (block instanceof ICustomStateMapper) {
							ZSSMain.logger.debug("Setting custom state mapper for " + block.getUnlocalizedName());
							ModelLoader.setCustomStateMapper(block, ((ICustomStateMapper) block).getCustomStateMap());
						}
						String name = block.getUnlocalizedName();
						Item item = GameRegistry.findItem(ModInfo.ID, name.substring(name.lastIndexOf(".") + 1));
						if (item instanceof IModItem) {
							((IModItem) item).registerVariants();
						}
					}
				}
			}
		} catch(Exception e) {
			ZSSMain.logger.warn("Caught exception while registering block variants: " + e.toString());
			e.printStackTrace();
		}
		try {
			for (Field f: ZSSItems.class.getFields()) {
				if (Item.class.isAssignableFrom(f.getType())) {
					Item item = (Item) f.get(null);
					if (item instanceof IModItem) {
						((IModItem) item).registerVariants();
					}
				}
			}
		} catch(Exception e) {
			ZSSMain.logger.warn("Caught exception while registering item variants: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Automated block and item renderer registration using {@link IModItem#registerRenderers}
	 */
	private void registerRenderers() {
		try {
			for (Field f: ZSSBlocks.class.getFields()) {
				if (Block.class.isAssignableFrom(f.getType())) {
					Block block = (Block) f.get(null);
					if (block != null) {
						if (block instanceof ISpecialRenderer) {
							((ISpecialRenderer) block).registerSpecialRenderer();
						}
						if (block instanceof ISwapModel) {
							addModelToSwap((ISwapModel) block);
						}
						String name = block.getUnlocalizedName();
						Item item = GameRegistry.findItem(ModInfo.ID, name.substring(name.lastIndexOf(".") + 1));
						if (item instanceof IModItem) {
							((IModItem) item).registerRenderers(mc.getRenderItem().getItemModelMesher());
						}
						if (item instanceof ISwapModel) {
							addModelToSwap((ISwapModel) item);
						}
					}
				}
			}
		} catch(Exception e) {
			ZSSMain.logger.warn("Caught exception while registering block renderers: " + e.toString());
			e.printStackTrace();
		}
		try {
			for (Field f: ZSSItems.class.getFields()) {
				if (Item.class.isAssignableFrom(f.getType())) {
					Item item = (Item) f.get(null);
					if (item instanceof IModItem) {
						((IModItem) item).registerRenderers(mc.getRenderItem().getItemModelMesher());
					}
					if (item instanceof ISwapModel) {
						addModelToSwap((ISwapModel) item);
					}
				}
			}
		} catch(Exception e) {
			ZSSMain.logger.warn("Caught exception while registering item renderers: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Adds the model swap information to the map
	 */
	private void addModelToSwap(ISwapModel swap) {
		for (ModelResourceLocation resource : swap.getDefaultResources()) {
			if (smartModels.containsKey(resource)) {
				if (smartModels.get(resource) != swap.getNewModel()) {
					ZSSMain.logger.warn("Conflicting models for resource " + resource.toString() + ": models=[old: " + smartModels.get(resource).getSimpleName() + ", new: " + swap.getNewModel().getSimpleName());
				}
			} else {
				ZSSMain.logger.debug("Swapping model for " + resource.toString() + " to class " + swap.getNewModel().getSimpleName());
				smartModels.put(resource, swap.getNewModel());
			}
		}
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return (ctx.side.isClient() ? mc.thePlayer : super.getPlayerEntity(ctx));
	}

	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		return (ctx.side.isClient() ? mc : super.getThreadFromContext(ctx));
	}
}
