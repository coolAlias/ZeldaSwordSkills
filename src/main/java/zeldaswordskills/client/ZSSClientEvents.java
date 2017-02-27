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

package zeldaswordskills.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ClientProxy;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.api.item.IZoomHelper;
import zeldaswordskills.client.gui.ComboOverlay;
import zeldaswordskills.client.gui.GuiBuffBar;
import zeldaswordskills.client.gui.GuiEndingBlowOverlay;
import zeldaswordskills.client.gui.GuiItemModeOverlay;
import zeldaswordskills.client.gui.GuiMagicMeter;
import zeldaswordskills.client.gui.GuiMagicMeterText;
import zeldaswordskills.client.gui.IGuiOverlay;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.handler.ZSSCombatEvents;
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.TargetUtils;

/**
 * 
 * Handles all client-sided events, such as render events, mouse event, etc.
 *
 */
@SuppressWarnings("deprecation") // Stupid to deprecate IBakedModel when it is still perfectly valid to use
@SideOnly(Side.CLIENT)
public class ZSSClientEvents
{
	private final Minecraft mc;

	/** List of all GUI Overlays that may need rendering */
	private final List<IGuiOverlay> overlays = new ArrayList<IGuiOverlay>();

	/** List of GUI overlays that have rendered this tick */
	private final List<IGuiOverlay> rendered = new ArrayList<IGuiOverlay>();

	/** True when openGL matrix needs to be popped */
	private boolean needsPop;

	/** Store the current key code for mouse buttons */
	private int mouseKey;

	/** Whether the button during mouse event is Minecraft's keyBindAttack */
	private boolean isAttackKey;

	/** Whether the button during mouse event is Minecraft's keyBindUseItem*/
	private boolean isUseKey;

	public ZSSClientEvents() {
		this.mc = Minecraft.getMinecraft();
		// Add overlays in order of rendering priority (generally bigger is higher priority)
		GuiMagicMeter meter = new GuiMagicMeter(mc);
		overlays.add(meter);
		overlays.add(new GuiMagicMeterText(mc, meter));
		overlays.add(new GuiBuffBar(mc));
		overlays.add(new GuiItemModeOverlay(mc));
		overlays.add(new ComboOverlay(mc));
		overlays.add(new GuiEndingBlowOverlay(mc));
	}

	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent.Post event) {
		if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
			return;
		}
		for (IGuiOverlay overlay : this.overlays) {
			if (overlay.shouldRender() && overlay.renderOverlay(event.resolution, this.rendered)) {
				this.rendered.add(overlay);
			}
		}
		this.rendered.clear();
	}

	@SubscribeEvent
	public void onBakeModel(ModelBakeEvent event) {
		for (ModelResourceLocation resource : ClientProxy.smartModels.keySet()) {
			Object object =  event.modelRegistry.getObject(resource);
			if (object instanceof IBakedModel) {
				Class<? extends IBakedModel> clazz = ClientProxy.smartModels.get(resource);
				try {
					IBakedModel customRender = clazz.getConstructor(IBakedModel.class).newInstance((IBakedModel) object);
					event.modelRegistry.putObject(resource, customRender);
					ZSSMain.logger.debug("Registered new renderer for resource " + resource + ": " + customRender.getClass().getSimpleName());
				} catch (NoSuchMethodException e) {
					ZSSMain.logger.warn("Failed to swap model: class " + clazz.getSimpleName() + " is missing a constructor that takes an IBakedModel");
				} catch (Exception e) {
					ZSSMain.logger.warn("Failed to swap model with exception: " + e.getMessage());
				}
			} else {
				ZSSMain.logger.warn("Resource is not a baked model! Failed resource: " + resource.toString());
			}
		}
	}

	@SubscribeEvent
	public void onStitchTexture(TextureStitchEvent.Pre event) {
		String digit = "items/digits/";
		for (int i = 0; i < 10; ++i) {
			event.map.registerSprite(new ResourceLocation(ModInfo.ID, digit + i));
		}
	}

	/**
	 * Attacks current target if player not currently using an item and {@link ICombo#onAttack}
	 * doesn't return false (i.e. doesn't miss)
	 * @param skill must implement BOTH {@link ILockOnTarget} AND {@link ICombo}
	 */
	@SideOnly(Side.CLIENT)
	public static void performComboAttack(Minecraft mc, ILockOnTarget skill) {
		if (!mc.thePlayer.isUsingItem()) {
			mc.thePlayer.swingItem();
			ZSSCombatEvents.setPlayerAttackTime(mc.thePlayer);
			if (skill instanceof ICombo && ((ICombo) skill).onAttack(mc.thePlayer)) {
				Entity entity = TargetUtils.getMouseOverEntity();
				mc.playerController.attackEntity(mc.thePlayer, (entity != null ? entity : skill.getCurrentTarget()));
			}
		}
	}

	/**
	 * FOV is determined initially in EntityPlayerSP; fov is recalculated for
	 * the vanilla bow only in the case that zoom-enhancing gear is worn
	 */
	@SubscribeEvent
	public void updateFOV(FOVUpdateEvent event) {
		ItemStack stack = (event.entity.isUsingItem() ? event.entity.getItemInUse() : null);
		if (stack != null) {
			boolean flag = stack.getItem() instanceof IZoom;
			if (flag || stack.getItem() == Items.bow) {
				float magify = 1.0F;
				for (ItemStack armor : event.entity.inventory.armorInventory) {
					if (armor != null && armor.getItem() instanceof IZoomHelper) {
						magify += ((IZoomHelper) armor.getItem()).getMagnificationFactor();
					}
				}
				if (flag || magify != 1.0F) {
					float maxTime = (flag ? ((IZoom) stack.getItem()).getMaxZoomTime() : 20.0F);
					float factor = (flag ? ((IZoom) stack.getItem()).getZoomFactor() : 0.15F);
					float charge = (float) event.entity.getItemInUseDuration() / maxTime;
					IAttributeInstance iattributeinstance = event.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
					float fov = (event.entity.capabilities.isFlying ? 1.1F : 1.0F);
					fov *= (iattributeinstance.getAttributeValue() / (double) event.entity.capabilities.getWalkSpeed() + 1.0D) / 2.0D;
					if (event.entity.capabilities.getWalkSpeed() == 0.0F || Float.isNaN(fov) || Float.isInfinite(fov)) {
						fov = 1.0F;
					}
					if (charge > 1.0F) {
						charge = 1.0F;
					} else {
						charge *= charge;
					}
					event.newfov = fov * (1.0F - charge * factor * magify);
				}
			}
		}
	}

	/**
	 * Handles mouse clicks for skills, canceling where appropriate; note that while the player
	 * is locked on with a targeting skill, keyBindAttack will ALWAYS be canceled, as the attack
	 * is passed to {@link #performComboAttack}; if the event were left uncanceled, the attack
	 * would process again in the vanilla system, doubling durability damage to weapons
	 * @buttons no button clicked -1, left button 0, right click 1, middle click 2, possibly 3+ for other buttons
	 * @notes Corresponding key codes for the mouse in Minecraft are (event.button -100)
	 */
	@SubscribeEvent
	public void onMouseChanged(MouseEvent event) {
		mouseKey = event.button - 100;
		isAttackKey = (mouseKey == mc.gameSettings.keyBindAttack.getKeyCode());
		isUseKey = (mouseKey == mc.gameSettings.keyBindUseItem.getKeyCode());
		if (event.dwheel == 0) {
			// no wheel, no button: just moving the mouse around
			if (event.button == -1) {
				return;
			} else if (!isAttackKey && !isUseKey) {
				// no wheel, unchecked key bound to mouse: pass input to custom key handler, as KeyInputEvent no longer receives these automatically
				if (event.buttonstate) {
					ZSSKeyHandler.onKeyPressed(mc, mouseKey);
				}
				return;
			}
		}
		ZSSPlayerSkills skills = ZSSPlayerSkills.get(mc.thePlayer);
		// check pre-conditions for attacking and item use (not stunned, etc.):
		if (event.buttonstate || event.dwheel != 0) {
			if (isAttackKey) {
				// hack for spin attack: allows key press information to be received while animating
				if (skills.isSkillActive(SkillBase.spinAttack) && skills.getActiveSkill(SkillBase.spinAttack).isAnimating()) {
					skills.getActiveSkill(SkillBase.spinAttack).keyPressed(mc, mc.gameSettings.keyBindAttack, mc.thePlayer);
					event.setCanceled(true);
				} else if (skills.isSkillActive(SkillBase.backSlice) && skills.getActiveSkill(SkillBase.backSlice).isAnimating()) {
					skills.getActiveSkill(SkillBase.backSlice).keyPressed(mc, mc.gameSettings.keyBindAttack, mc.thePlayer);
					event.setCanceled(true);
				} else if (!skills.canInteract() || ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.STUN)) {
					//LogHelper.info("Skills could not interact during left click - canceling");
					event.setCanceled(true);
				} else {
					Item heldItem = (mc.thePlayer.getHeldItem() != null ? mc.thePlayer.getHeldItem().getItem() : null);
					event.setCanceled(heldItem instanceof ItemHeldBlock || (!ZSSPlayerInfo.get(mc.thePlayer).canAttack() && (Config.affectAllSwings() || heldItem instanceof ISwingSpeed)));
				}
			} else if (isUseKey) {
				event.setCanceled(!skills.canInteract() || ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.STUN));
			} else { // cancel mouse wheel while animations are in progress
				event.setCanceled(!skills.canInteract());
			}
		}
		if (event.isCanceled() || !event.buttonstate) {
			return;
		}
		ILockOnTarget skill = skills.getTargetingSkill();
		if (skill != null && skill.isLockedOn()) {
			if (isAttackKey) {
				// mouse attack will always be canceled while locked on, as the click has been handled
				if (Config.allowVanillaControls) {
					if (!skills.onKeyPressed(mc, mc.gameSettings.keyBindAttack)) {
						// no skill activated - perform a 'standard' attack
						performComboAttack(mc, skill);
					}
					// hack for Armor Break: allows charging to begin without having to press attack key a second time
					if (skills.hasSkill(SkillBase.armorBreak)) {
						skills.getActiveSkill(SkillBase.armorBreak).keyPressed(mc, mc.gameSettings.keyBindAttack, mc.thePlayer);
					}
				}
				// if vanilla controls not enabled, mouse click is ignored (i.e. canceled)
				// if vanilla controls enabled, mouse click was already handled - cancel
				event.setCanceled(true);
			} else if (isUseKey && Config.allowVanillaControls) {
				if (!skills.canInteract()) {
					event.setCanceled(true);
				}
			}
		} else  if (isAttackKey) { // not locked on to a target, normal item swing: set attack time only
			ZSSCombatEvents.setPlayerAttackTime(mc.thePlayer);
		}
	}

	@SubscribeEvent
	public void getFogDensity(EntityViewRenderEvent.FogDensity event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ItemStack helm = player.getCurrentArmor(ArmorIndex.WORN_HELM);
			if (helm != null && !player.isPotionActive(Potion.blindness)) {
				if (event.block.getMaterial() == Material.lava && (helm.getItem() == ZSSItems.tunicGoronHelm || helm.getItem() == ZSSItems.maskGoron)) {
					event.density = 0.85F;
					GlStateManager.setFog(2048);
					event.setCanceled(true);
				}
			}
		}
	}

	/*
	// TODO
	@SubscribeEvent
	public void onRenderPlayer(RenderPlayerEvent.Pre event) {
		ItemStack mask = event.entityPlayer.getCurrentArmor(ArmorIndex.WORN_HELM);
		if (mask != null && mask.getItem() == ZSSItems.maskGiants) {
			GlStateManager.pushMatrix();
			needsPop = true;
			if (event.entityPlayer == mc.thePlayer) {
				if (mc.inGameHasFocus) {
					GlStateManager.translate(0.0F, -6.325F, 0.0F);
					GlStateManager.scale(3.0F, 3.0F, 3.0F);
				}
			} else {
				GlStateManager.scale(3.0F, 3.0F, 3.0F);
			}
			/*
			if (fakePlayer == null) {
				fakePlayer = new FakeClientPlayer(event.entityPlayer.worldObj);
			}
			Vec3 vec3 = event.entityPlayer.getLookVec();
			double dx = (Minecraft.getMinecraft().gameSettings.thirdPersonView > 0 ? 0.0D : vec3.xCoord);
			double dz = (Minecraft.getMinecraft().gameSettings.thirdPersonView > 0 ? 0.0D : vec3.zCoord);
			fakePlayer.setLocationAndAngles(event.entityPlayer.posX + dx, event.entityPlayer.posY, event.entityPlayer.posZ + dz, event.entityPlayer.rotationYaw, event.entityPlayer.rotationPitch);
			fakePlayer.renderYawOffset = event.entityPlayer.renderYawOffset;
			//fakePlayer.prevCameraPitch = event.entityPlayer.prevCameraPitch;
			//fakePlayer.prevRotationPitch = event.entityPlayer.prevRotationPitch;
			fakePlayer.prevRotationYaw = event.entityPlayer.prevRotationYaw;
			fakePlayer.rotationYawHead = event.entityPlayer.rotationYawHead;
			fakePlayer.prevPosX = event.entityPlayer.prevPosX + dx;
			fakePlayer.prevPosY = event.entityPlayer.prevPosY;
			fakePlayer.prevPosZ = event.entityPlayer.prevPosZ + dz;
			Minecraft.getMinecraft().renderViewEntity = fakePlayer;
		}

		else if (fakePlayer != null) {
			fakePlayer = null;
		}*
		}
	}

	@SubscribeEvent
	public void onRenderPlayer(RenderPlayerEvent.Post event) {
		if (needsPop) {
			GlStateManager.popMatrix();
			needsPop = false;
		}
	}
	 */
}
